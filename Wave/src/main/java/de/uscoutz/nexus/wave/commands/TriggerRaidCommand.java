package de.uscoutz.nexus.wave.commands;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import de.uscoutz.nexus.wave.raids.Raid;
import de.uscoutz.nexus.wave.raids.RaidType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TriggerRaidCommand implements CommandExecutor {

    private NexusWavePlugin plugin;

    public TriggerRaidCommand(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player player) {
            if(player.hasPermission("nexus.command.triggerraid")) {
                Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());
                if(raidProfile.getRaid() != null) {
                    raidProfile.getRaid().end(false, false);
                    player.sendMessage("§aActive raid cancelled");
                }
                if(raidProfile.getTask() != null) {
                    raidProfile.getTask().cancel();
                    player.sendMessage("§aScheduled raid cancelled");
                }

                List<RaidType> raidTypes = plugin.getRaidManager().getRaidTypesByNexuslevel().get(profile.getNexusLevel());
                RaidType raidType;
                try {
                    raidType = raidTypes.get((int)(Math.random() * raidTypes.size())).clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }

                Raid raid = new Raid(raidType, profile, plugin);
                raid.schedule(30);
            }
        }

        return false;
    }
}
