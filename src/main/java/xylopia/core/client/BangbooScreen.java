package xylopia.core.client;

import dan200.computercraft.client.gui.AbstractComputerScreen;
import dan200.computercraft.client.gui.widgets.TerminalWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xylopia.core.menu.BangbooMenu;

public class BangbooScreen extends AbstractComputerScreen<BangbooMenu> {

    public BangbooScreen(BangbooMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 0);
        this.imageWidth  = BangbooMenu.IMAGE_WIDTH;   // 357
        this.imageHeight = BangbooMenu.IMAGE_HEIGHT;  // 263
    }

    @Override
    protected TerminalWidget createTerminal() {
        return new TerminalWidget(
                terminalData, computerInput, computerActions,
                leftPos + BangbooMenu.TERMINAL_X,
                topPos  + BangbooMenu.TERMINAL_Y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;

        // ── Main background panel ──────────────────────────────────────────
        graphics.fill(x,     y,     x + w,     y + h,     0xFF2B2B2B);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF1E1E1E);

        // ── Inset around the terminal area ─────────────────────────────────
        int tx = x + BangbooMenu.TERMINAL_X - 2;
        int ty = y + BangbooMenu.TERMINAL_Y - 2;
        int tw = BangbooMenu.TERMINAL_W + 4;
        int th = BangbooMenu.TERMINAL_H + 4;
        graphics.fill(tx,     ty,     tx + tw,     ty + th,     0xFF111111);
        graphics.fill(tx + 1, ty + 1, tx + tw - 1, ty + th - 1, 0xFF000000);

        // ── Plug-in slot panel — full-height right column ─────────────────
        boolean hubActive = menu.isHubActive();
        int panelX = x + BangbooMenu.PLUGIN_SLOT_X - BangbooMenu.BORDER;
        int panelY = y;
        int panelW = 18 + BangbooMenu.BORDER * 2;
        int panelH = BangbooMenu.IMAGE_HEIGHT;
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF1E1E1E);

        // ── Individual plug-in slot backgrounds ────────────────────────────
        for (int i = 0; i < BangbooMenu.TOTAL_PLUGIN_COUNT; i++) {
            int sx = x + BangbooMenu.PLUGIN_SLOT_X - 1;
            int sy = y + BangbooMenu.PLUGIN_SLOT_Y0 + i * BangbooMenu.PLUGIN_SPACING - 1;
            boolean locked = i >= BangbooMenu.BASE_PLUGIN_COUNT && !hubActive;
            if (locked) {
                graphics.fill(sx,     sy,     sx + 18, sy + 18, 0xFF1A1A1A);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF222222);
            } else {
                graphics.fill(sx,     sy,     sx + 18, sy + 18, 0xFF373737);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
            }
        }

        // ── Backpack slot ──────────────────────────────────────────────────
        if (menu.hasBackpackPlugin()) {
            int bx = x + BangbooMenu.PLUGIN_SLOT_X - 1;
            int by = y + BangbooMenu.BACKPACK_SLOT_Y - 1;
            graphics.fill(bx,     by,     bx + 18, by + 18, 0xFF373737);
            graphics.fill(bx + 1, by + 1, bx + 17, by + 17, 0xFF8B8B8B);
        }

        // ── Internal inventory slots (16, 4×4) ────────────────────────────
        if (menu.hasInventoryPlugin()) {
            renderInternalInventorySlots(graphics, x, y);
        }

        // ── Player inventory slots (3×9 main + hotbar) ─────────────────────
        renderInventorySlots(graphics, x, y, false);  // main inventory
        renderInventorySlots(graphics, x, y, true);   // hotbar
    }

    private void renderInternalInventorySlots(GuiGraphics graphics, int x, int y) {
        for (int row = 0; row < BangbooMenu.INT_INV_ROWS; row++) {
            for (int col = 0; col < BangbooMenu.INT_INV_COLS; col++) {
                int sx = x + BangbooMenu.INT_INV_X + col * 18 - 1;
                int sy = y + BangbooMenu.INT_INV_Y + row * 18 - 1;
                graphics.fill(sx,     sy,     sx + 18, sy + 18, 0xFF373737);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
            }
        }
    }

    private void renderInventorySlots(GuiGraphics graphics, int x, int y, boolean hotbar) {
        int rows   = hotbar ? 1 : 3;
        int startY = y + (hotbar ? BangbooMenu.INV_HOT_Y : BangbooMenu.INV_MAIN_Y);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + BangbooMenu.INV_X + col * 18 - 1;
                int sy = startY + row * 18 - 1;
                graphics.fill(sx,     sy,     sx + 18, sy + 18, 0xFF373737);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
            }
        }
    }
}
