package xylopia.core.menu;

import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu;
import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xylopia.core.backpack.BackpackAccessors;
import xylopia.core.entity.BangbooEntity;
import xylopia.core.item.plugins.BangbooPluginItem;
import xylopia.core.item.plugins.ConfigCoreItem;
import xylopia.core.item.plugins.PluginHubItem;

import java.util.function.Predicate;

public class BangbooMenu extends ComputerMenuWithoutInventory {

    // ── Layout constants shared with BangbooScreen ─────────────────────────
    public static final int BORDER             = 4;
    public static final int SIDEBAR_WIDTH      = AbstractComputerMenu.SIDEBAR_WIDTH; // 17
    public static final int TERMINAL_X         = SIDEBAR_WIDTH + BORDER;             // 21
    public static final int TERMINAL_Y         = BORDER;                             // 4
    public static final int TERMINAL_W         = 51 * 6 + 4;                        // 310
    public static final int TERMINAL_H         = 19 * 9 + 4;                        // 175
    public static final int PLUGIN_SLOT_X      = TERMINAL_X + TERMINAL_W + BORDER;  // 335
    public static final int PLUGIN_SLOT_Y0     = BORDER;                            // 4
    public static final int PLUGIN_SPACING     = 22;
    public static final int BASE_PLUGIN_COUNT  = 4;
    public static final int TOTAL_PLUGIN_COUNT = 8;

    public static final int IMAGE_WIDTH  = PLUGIN_SLOT_X + 18 + BORDER;             // 357
    public static final int BACKPACK_SLOT_Y = PLUGIN_SLOT_Y0 + TOTAL_PLUGIN_COUNT * PLUGIN_SPACING + BORDER * 2; // 188

    // ── Player inventory + internal inventory side by side below terminal ──
    // Player: 9×18=162px  |  gap: BORDER*2=8px  |  Internal: 4×18=72px  → total 242px
    public static final int INT_INV_COLS = 4;
    public static final int INT_INV_ROWS = 4;
    public static final int INV_X        = (IMAGE_WIDTH - (9 * 18 + BORDER * 2 + INT_INV_COLS * 18)) / 2; // 57
    public static final int INV_MAIN_Y   = TERMINAL_Y + TERMINAL_H + BORDER * 2;        // 187
    public static final int INV_HOT_Y    = INV_MAIN_Y + 3 * 18 + 4;                   // 241
    public static final int INT_INV_X    = INV_X + 9 * 18 + BORDER * 2;              // 227
    public static final int INT_INV_Y    = INV_MAIN_Y;                                 // 183
    public static final int IMAGE_HEIGHT = INV_HOT_Y + 18 + BORDER;                   // 263

    private final BangbooEntity entity;
    private final Container pluginContainer;
    private final Container internalInventory;
    private final Container backpackContainer;
    private int pluginSlotOffset;
    private int intInvSlotOffset;
    private int backpackSlotOffset;
    private int invSlotOffset;
    private int syncedEnergyPermille = 0;

    // ── Server constructor ────────────────────────────────────────────────
    public BangbooMenu(MenuType<? extends AbstractComputerMenu> type, int id, Inventory inventory,
                       Predicate<Player> canUse, ServerComputer computer,
                       Container pluginContainer, Container internalInventory, BangbooEntity entity) {
        super(type, id, inventory, canUse, computer);
        this.entity = entity;
        this.pluginContainer = pluginContainer;
        this.internalInventory = internalInventory;
        this.backpackContainer = entity.getBackpackSlot();
        addPluginSlots();
        addInternalInventorySlots();
        addBackpackSlot();
        addInventorySlots(inventory);
        addEnergyData();
    }

    // ── Client constructor (called via IContainerFactory) ─────────────────
    public BangbooMenu(MenuType<? extends AbstractComputerMenu> type, int id, Inventory inventory,
                       ComputerContainerData data) {
        super(type, id, inventory, data);
        this.entity = null;
        this.pluginContainer = new SimpleContainer(TOTAL_PLUGIN_COUNT);
        this.internalInventory = new SimpleContainer(INT_INV_ROWS * INT_INV_COLS);
        this.backpackContainer = new SimpleContainer(1);
        addPluginSlots();
        addInternalInventorySlots();
        addBackpackSlot();
        addInventorySlots(inventory);
        addEnergyData();
    }

    private void addEnergyData() {
        addDataSlots(new net.minecraft.world.inventory.ContainerData() {
            @Override public int get(int i) {
                return (i == 0 && entity != null) ? entity.getEnergy() * 1000 / BangbooEntity.maxEnergy() : 0;
            }
            @Override public void set(int i, int v) { if (i == 0) syncedEnergyPermille = v; }
            @Override public int getCount() { return 1; }
        });
    }

    public boolean hasConfigPlugin() {
        for (int i = 0; i < TOTAL_PLUGIN_COUNT; i++) {
            if (pluginContainer.getItem(i).getItem() instanceof ConfigCoreItem) return true;
        }
        return false;
    }

    public float getEnergyFraction() { return syncedEnergyPermille / 1000f; }

    /** True if a Plug-In Hub occupies any base slot. */
    public boolean isHubActive() {
        for (int i = 0; i < BASE_PLUGIN_COUNT; i++) {
            if (pluginContainer.getItem(i).getItem() instanceof PluginHubItem) return true;
        }
        return false;
    }

    private void addPluginSlots() {
        for (int i = 0; i < TOTAL_PLUGIN_COUNT; i++) {
            final int slotIndex = i;
            final boolean isExtra = i >= BASE_PLUGIN_COUNT;
            Slot slot = addSlot(new Slot(pluginContainer, i,
                    PLUGIN_SLOT_X, PLUGIN_SLOT_Y0 + i * PLUGIN_SPACING) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (!(stack.getItem() instanceof BangbooPluginItem)) return false;
                    if (isExtra) {
                        return !(stack.getItem() instanceof PluginHubItem) && isHubActive();
                    }
                    // Base slots: only one hub allowed at a time
                    if (stack.getItem() instanceof PluginHubItem) {
                        for (int j = 0; j < BASE_PLUGIN_COUNT; j++) {
                            if (pluginContainer.getItem(j).getItem() instanceof PluginHubItem) return false;
                        }
                    }
                    return true;
                }

                @Override
                public boolean isActive() {
                    return !isExtra || isHubActive();
                }

                @Override
                public void set(ItemStack newStack) {
                    ItemStack old = getItem().copy();
                    super.set(newStack);
                    // Skip if both are empty — shift-click mutates the stack before set(EMPTY) is
                    // called, so old would be empty too; quickMoveStack fires the callback instead.
                    if (entity != null && (!old.isEmpty() || !newStack.isEmpty()))
                        entity.onPluginSlotChanged(slotIndex, old, newStack);
                }

                // Covers regular click-pickup and Q-throw, which call remove()+onTake() not set()
                @Override
                public void onTake(Player player, ItemStack taken) {
                    super.onTake(player, taken);
                    if (entity != null && getItem().isEmpty())
                        entity.onPluginSlotChanged(slotIndex, taken, ItemStack.EMPTY);
                }
            });
            if (i == 0) pluginSlotOffset = slot.index;
        }
    }

    public boolean hasBackpackPlugin() {
        for (int i = 0; i < TOTAL_PLUGIN_COUNT; i++) {
            var item = pluginContainer.getItem(i).getItem();
            if (item instanceof BangbooPluginItem p && p.providesBackpack()) return true;
        }
        return false;
    }

    private void addBackpackSlot() {
        Slot slot = addSlot(new Slot(backpackContainer, 0, PLUGIN_SLOT_X, BACKPACK_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BackpackAccessors.isValidBackpack(stack);
            }
            @Override
            public boolean isActive() { return hasBackpackPlugin(); }
        });
        backpackSlotOffset = slot.index;
    }

    public boolean hasInventoryPlugin() {
        for (int i = 0; i < TOTAL_PLUGIN_COUNT; i++) {
            var item = pluginContainer.getItem(i).getItem();
            if (item instanceof BangbooPluginItem p && p.providesInventory()) return true;
        }
        return false;
    }

    private void addInternalInventorySlots() {
        for (int row = 0; row < INT_INV_ROWS; row++) {
            for (int col = 0; col < INT_INV_COLS; col++) {
                final int i = row * INT_INV_COLS + col;
                Slot slot = addSlot(new Slot(internalInventory, i,
                        INT_INV_X + col * 18,
                        INT_INV_Y + row * 18) {
                    @Override
                    public boolean isActive() { return hasInventoryPlugin(); }
                });
                if (row == 0 && col == 0) intInvSlotOffset = slot.index;
            }
        }
    }

    private void addInventorySlots(Inventory inventory) {
        // Main inventory (rows 1-3, item indices 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                Slot slot = addSlot(new Slot(inventory,
                        col + row * 9 + 9,
                        INV_X + col * 18,
                        INV_MAIN_Y + row * 18));
                if (row == 0 && col == 0) invSlotOffset = slot.index;
            }
        }
        // Hotbar (item indices 0-8)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, INV_X + col * 18, INV_HOT_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        int pluginEnd   = pluginSlotOffset  + TOTAL_PLUGIN_COUNT;
        int intInvEnd   = intInvSlotOffset  + INT_INV_ROWS * INT_INV_COLS;
        int backpackEnd = backpackSlotOffset + 1;
        int invEnd      = invSlotOffset     + 36;
        int hotStart    = invSlotOffset     + 27;

        if (index >= invSlotOffset && index < invEnd) {
            // From player inventory: plug-ins → plug-in slots, valid backpacks → backpack slot, rest → internal
            if (stack.getItem() instanceof BangbooPluginItem) {
                if (!moveItemStackTo(stack, pluginSlotOffset, pluginEnd, false))
                    return ItemStack.EMPTY;
            } else if (BackpackAccessors.isValidBackpack(stack)) {
                if (!moveItemStackTo(stack, backpackSlotOffset, backpackEnd, false))
                    return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, intInvSlotOffset, intInvEnd, false))
                    return ItemStack.EMPTY;
            }
        } else if (index >= pluginSlotOffset && index < pluginEnd) {
            // From plug-in slot → hotbar, then main
            if (!moveItemStackTo(stack, hotStart, invEnd, false) &&
                !moveItemStackTo(stack, invSlotOffset, hotStart, false))
                return ItemStack.EMPTY;
        } else if (index >= intInvSlotOffset && index < intInvEnd) {
            // From internal inventory → hotbar, then main
            if (!moveItemStackTo(stack, hotStart, invEnd, false) &&
                !moveItemStackTo(stack, invSlotOffset, hotStart, false))
                return ItemStack.EMPTY;
        } else if (index >= backpackSlotOffset && index < backpackEnd) {
            // From backpack slot → hotbar, then main
            if (!moveItemStackTo(stack, hotStart, invEnd, false) &&
                !moveItemStackTo(stack, invSlotOffset, hotStart, false))
                return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY); // set() skips EMPTY→EMPTY; fire callback manually with correct old
            int pluginRel = index - pluginSlotOffset;
            if (entity != null && pluginRel >= 0 && pluginRel < TOTAL_PLUGIN_COUNT)
                entity.onPluginSlotChanged(pluginRel, original, ItemStack.EMPTY);
        } else slot.setChanged();
        return original;
    }
}
