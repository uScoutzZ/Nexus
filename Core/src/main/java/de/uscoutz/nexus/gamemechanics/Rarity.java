package de.uscoutz.nexus.gamemechanics;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import org.bukkit.entity.Player;

public enum Rarity {

    COMMON(),
    UNCOMMON(),
    RARE(),
    EPIC(),
    LEGENDARY(),
    MYTHIC();

    public String toString(String language) {
        return NexusPlugin.getInstance().getLocaleManager().translate(language, "rarity_" + name().toLowerCase());
    }

    public String toString(Player player) {
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId());
        return toString(nexusPlayer.getLanguage());
    }
}
