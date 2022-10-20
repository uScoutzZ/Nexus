package de.uscoutz.nexus.skills.rewards;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.profile.ProfilePlayer;
import lombok.Setter;
import org.bukkit.entity.Player;

public class MoneyReward implements SkillReward{

    private String display;
    private int rewardMoney;

    public MoneyReward(int rewardMoney) {
        this.rewardMoney = rewardMoney;
        display = "§e" + rewardMoney + "§7 Coins";
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public void addReward(Player player) {
        Profile profile = NexusPlugin.getInstance().getWorldManager().getWorldProfileMap().get(player.getWorld());
        ProfilePlayer profilePlayer = profile.getMembers().get(player.getUniqueId());
        profilePlayer.addMoney(rewardMoney);
    }
}
