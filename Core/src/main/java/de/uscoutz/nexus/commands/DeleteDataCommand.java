package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeleteDataCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public DeleteDataCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender.hasPermission("nexus.command.deletedata")) {
            if(args.length == 1) {
                List<String> playerProfiles = new ArrayList<>();

                ResultSet resultSet = plugin.getDatabaseAdapter().get("profiles", "owner", args[0]);
                try {
                    while(resultSet.next()) {
                        playerProfiles.add(resultSet.getString("profileId"));
                    }
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }

                for(String profileId : playerProfiles) {
                    plugin.getDatabaseAdapter().delete("playerProfiles", "profileId", profileId);
                    plugin.getDatabaseAdapter().delete("schematics", "profileId", profileId);
                    plugin.getDatabaseAdapter().delete("quests", "profileId", profileId);
                    plugin.getDatabaseAdapter().delete("raids", "profileId", profileId);
                    plugin.getDatabaseAdapter().delete("storages", "profileId", profileId);
                }

                plugin.getDatabaseAdapter().delete("players", "player", args[0]);
                plugin.getDatabaseAdapter().delete("profiles", "owner", args[0]);
                sender.sendMessage("§aData von dem Spieler bereinigt");
            } else {
                sender.sendMessage("§c/deletedata <UUID>");
            }
        }
        return false;
    }
}
