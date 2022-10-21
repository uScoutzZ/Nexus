package de.uscoutz.nexus.gamemechanics.shops;

import org.bukkit.entity.Player;

public interface NexusPrice {

    String getDisplay();

    void remove(Player player);

    boolean containsNeeded(Player player);

}
