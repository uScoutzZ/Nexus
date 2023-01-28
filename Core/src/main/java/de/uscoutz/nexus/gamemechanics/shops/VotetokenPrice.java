package de.uscoutz.nexus.gamemechanics.shops;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import net.apotox.gameapi.user.User;
import org.bukkit.entity.Player;

public class VotetokenPrice implements NexusPrice{

    private NexusPlugin plugin;

    private String localeKey;
    private int votetokens;

    public VotetokenPrice(int money, NexusPlugin plugin) {
        this.votetokens = money;
        this.plugin = plugin;
        if(votetokens == 1) {
            this.localeKey = "price_votetokens-singular";
        } else {
            this.localeKey = "price_votetokens-plural";
        }
    }

    @Override
    public String getDisplay(Player player) {
        return plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), localeKey, votetokens);
    }

    @Override
    public void remove(Player player) {
        User.getFromPlayer(player).removeVotetokens(votetokens);
    }

    @Override
    public boolean containsNeeded(Player player) {
        return User.getFromPlayer(player).getVotetokens() >= votetokens;
    }
}
