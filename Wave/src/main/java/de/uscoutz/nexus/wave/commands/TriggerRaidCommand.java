package de.uscoutz.nexus.wave.commands;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.profile.RaidProfile;
import de.uscoutz.nexus.wave.raids.Raid;
import de.uscoutz.nexus.wave.raids.RaidType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
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
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player) {
            if(player.hasPermission("nexus.command.triggerraid")) {
                Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                RaidProfile raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());
                RaidType raidType = null;
                if(args.length != 0) {
                    if(args[0].equalsIgnoreCase("list")) {
                        for(int i : plugin.getRaidManager().getRaidTypesByNexuslevel().keySet()) {
                            for(RaidType raidType1 : plugin.getRaidManager().getRaidTypesByNexuslevel().get(i)) {
                                TextComponent message = new TextComponent("- " + raidType1.getRaidTypeId());
                                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/triggerraid " + raidType1.getRaidTypeId()));
                                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to trigger " + raidType1.getRaidTypeId())));
                                player.sendMessage(message);
                            }
                        }
                        return false;
                    } else {
                        for(int i : plugin.getRaidManager().getRaidTypesByNexuslevel().keySet()) {
                            for(RaidType raidType1 : plugin.getRaidManager().getRaidTypesByNexuslevel().get(i)) {
                                if(raidType1.getRaidTypeId().equals(args[0])) {
                                    raidType = raidType1;
                                }
                            }
                        }
                    }
                } else {
                    List<RaidType> raidTypes = plugin.getRaidManager().getRaidTypesByNexuslevel().get(profile.getNexusLevel());

                    try {
                        raidType = raidTypes.get((int)(Math.random() * raidTypes.size())).clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(raidType != null) {
                    if(raidProfile.getRaid() != null) {
                        raidProfile.getRaid().end(false, false);
                        player.sendMessage("§aActive raid cancelled");
                    }
                    if(raidProfile.getTask() != null) {
                        raidProfile.getTask().cancel();
                        player.sendMessage("§aScheduled raid cancelled");
                    }

                    Raid raid = new Raid(raidType, profile, plugin);
                    raidProfile.setRaid(raid);
                    raid.schedule(30);
                } else {
                    player.sendMessage("§cRaid not found");
                }
            }
        }

        return false;
    }
}
