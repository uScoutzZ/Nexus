package de.uscoutz.nexus.schematic.commands;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
                if(args.length >= 2) {
                    try {
                        SchematicType schematicType = SchematicType.valueOf(args[0]);
                        Condition condition = Condition.INTACT;
                        if(args.length == 4) {
                            try {
                                condition = Condition.valueOf(args[3]);
                            } catch (Exception exception) {
                                player.sendMessage("§cCondition does not exist");
                            }
                        }
                        int level = Integer.parseInt(args[1]);
                        int rotation = 0;
                        if(args.length == 3) {
                            try {
                                rotation = Integer.parseInt(args[2]);
                            } catch (Exception exception) {
                                player.sendMessage("§cRotation not a number");
                                try {
                                    condition = Condition.valueOf(args[2]);
                                } catch (Exception exception2) {
                                    player.sendMessage("§cCondition does not exist");
                                }
                            }
                        }
                        Schematic schematic = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(condition).get(level);
                        player.sendMessage("§ePasting " + schematic.getBlocks().size() +" blocks");
                        schematic.build(player.getLocation(), rotation, UUID.randomUUID(), 0, false);
                    } catch (IllegalArgumentException exception) {
                        sendHelp(player);
                        exception.printStackTrace();
                    }
                } else {
                    sendHelp(player);
                }
            } else {
                player.sendMessage(plugin.getNO_PERMISSION());
            }
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§cSyntax: /loadschematic <SchematicType> <Level> [Rotation] [Condition]");
    }
}
