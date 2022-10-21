package de.uscoutz.nexus.gamemechanics.shops;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import org.bukkit.entity.Player;

public class MoneyPrice implements NexusPrice{

    private NexusPlugin plugin;

    private String display;
    private int money;

    public MoneyPrice(int money, NexusPlugin plugin) {
        this.money = money;
        this.plugin = plugin;
        this.display = "§e" + money + "§7 Coins";
    }

    @Override
    public String getDisplay() {
        return display;
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
