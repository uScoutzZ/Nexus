package de.uscoutz.nexus.gamemechanics.shops;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import org.bukkit.entity.Player;

public class MoneyPrice implements NexusPrice{

    private NexusPlugin plugin;

    private String localeKey;
    private int money;

    public MoneyPrice(int money, NexusPlugin plugin) {
        this.money = money;
        this.plugin = plugin;
        this.localeKey = "price_coins";
    }

    @Override
    public String getDisplay(Player player) {
        return plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), localeKey, money);
    }

    @Override
    public void remove(Player player) {
        Profile profile = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld());
        profile.getMembers().get(player.getUniqueId()).addMoney(-money);
    }

    @Override
    public boolean containsNeeded(Player player) {
        Profile profile = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld());
        return profile.getMembers().get(player.getUniqueId()).getMoney() >= money;
    }
}
