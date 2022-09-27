package de.uscoutz.nexus.wave.commands;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CancelRaidCommand implements CommandExecutor {

    private NexusWavePlugin plugin;

    public CancelRaidCommand(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player player) {
            if(player.hasPermission("nexus.command.cancelraid")) {
                Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());
                if(raidProfile.getRaid() != null) {
                    raidProfile.getRaid().end(false, false);
                    player.sendMessage("§aRaid cancelled");
                } else {
                    player.sendMessage("§cNo raid running");
                }
            }
        }

        return false;
    }
}
