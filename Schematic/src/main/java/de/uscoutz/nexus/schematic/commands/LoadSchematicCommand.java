package de.uscoutz.nexus.schematic.commands;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoadSchematicCommand implements CommandExecutor {

    private NexusSchematicPlugin plugin;

    public LoadSchematicCommand(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());
            if (player.hasPermission("nexus.schematics")) {
                if(args.length == 2) {
                    try {
                        SchematicType schematicType = SchematicType.valueOf(args[0]);
                        int level = Integer.parseInt(args[1]);
                        plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(level)
                                .build(player.getLocation(), 0);
                    } catch (IllegalArgumentException exception) {
                        sendHelp(player);
                    }
                } else {
                    sendHelp(player);
                }
            }
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§cSyntax: /loadschematic <SchematicType> <Level>");
    }
}
