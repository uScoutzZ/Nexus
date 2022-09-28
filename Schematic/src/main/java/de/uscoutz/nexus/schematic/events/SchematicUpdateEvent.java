package de.uscoutz.nexus.schematic.events;

import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SchematicUpdateEvent extends Event {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    @Getter
    private Profile profile;
    @Getter
    private BuiltSchematic schematic;

    public SchematicUpdateEvent(Profile profile, BuiltSchematic schematic) {
        this.profile = profile;
        this.schematic = schematic;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
