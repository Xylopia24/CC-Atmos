package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Optional;

public class SpeakerPeripheral extends BangbooPeripheral {
    public SpeakerPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "speaker"; }

    @LuaFunction(mainThread = true)
    public final void playSound(String name, Optional<Double> volume, Optional<Double> pitch) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        ResourceLocation id;
        try { id = ResourceLocation.parse(name); } catch (Exception e) {
            throw new LuaException("Invalid sound name: " + name);
        }
        level.playSound(null, bangboo.getX(), bangboo.getY(), bangboo.getZ(),
            SoundEvent.createVariableRangeEvent(id), SoundSource.NEUTRAL,
            volume.orElse(1.0).floatValue(), pitch.orElse(1.0).floatValue());
    }

    @LuaFunction(mainThread = true)
    public final void playNote(String instrument, Optional<Double> volume, Optional<Double> pitch) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        var sound = switch (instrument.toLowerCase()) {
            case "bass"           -> SoundEvents.NOTE_BLOCK_BASS;
            case "basedrum"       -> SoundEvents.NOTE_BLOCK_BASEDRUM;
            case "snare"          -> SoundEvents.NOTE_BLOCK_SNARE;
            case "hat"            -> SoundEvents.NOTE_BLOCK_HAT;
            case "guitar"         -> SoundEvents.NOTE_BLOCK_GUITAR;
            case "flute"          -> SoundEvents.NOTE_BLOCK_FLUTE;
            case "bell"           -> SoundEvents.NOTE_BLOCK_BELL;
            case "chime"          -> SoundEvents.NOTE_BLOCK_CHIME;
            case "xylophone"      -> SoundEvents.NOTE_BLOCK_XYLOPHONE;
            case "iron_xylophone" -> SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE;
            case "cow_bell"       -> SoundEvents.NOTE_BLOCK_COW_BELL;
            case "didgeridoo"     -> SoundEvents.NOTE_BLOCK_DIDGERIDOO;
            case "bit"            -> SoundEvents.NOTE_BLOCK_BIT;
            case "banjo"          -> SoundEvents.NOTE_BLOCK_BANJO;
            case "pling"          -> SoundEvents.NOTE_BLOCK_PLING;
            default               -> SoundEvents.NOTE_BLOCK_HARP;
        };
        level.playSound(null, bangboo.getX(), bangboo.getY(), bangboo.getZ(),
            sound, SoundSource.NEUTRAL,
            volume.orElse(1.0).floatValue(), pitch.orElse(1.0).floatValue());
    }
}
