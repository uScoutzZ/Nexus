package de.uscoutz.nexus.schematic.events;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SchematicDamagedEvent extends Event {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    @Getter
    private Profile profile;
    @Getter
    private BuiltSchematic schematic;
    @Getter
    private boolean conditionChanged;

    public SchematicDamagedEvent(Profile profile, BuiltSchematic schematic, boolean conditionChanged) {
        this.profile = profile;
        this.schematic = schematic;
        this.conditionChanged = conditionChanged;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
