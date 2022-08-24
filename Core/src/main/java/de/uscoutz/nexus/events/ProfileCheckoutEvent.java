package de.uscoutz.nexus.events;

import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ProfileCheckoutEvent extends Event {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    @Getter
    private Profile profile;

    public ProfileCheckoutEvent(Profile profile) {
        this.profile = profile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
