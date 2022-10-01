package de.uscoutz.nexus.events;

import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SchematicItemBoughtEvent extends Event {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    @Getter
    private String key;
    @Getter
    private Profile profile;

    public SchematicItemBoughtEvent(String key, Profile profile) {
        this.key = key;
        this.profile = profile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
