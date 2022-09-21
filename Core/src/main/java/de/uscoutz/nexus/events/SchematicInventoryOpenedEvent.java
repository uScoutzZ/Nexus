package de.uscoutz.nexus.events;

import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SchematicInventoryOpenedEvent extends Event {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    @Getter
    private SimpleInventory simpleInventory;
    @Getter
    private Player player;

    public SchematicInventoryOpenedEvent(SimpleInventory simpleInventory, Player player) {
        this.simpleInventory = simpleInventory;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
