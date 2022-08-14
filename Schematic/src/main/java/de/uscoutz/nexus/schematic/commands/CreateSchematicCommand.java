package de.uscoutz.nexus.schematic.commands;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class CreateSchematicCommand implements CommandExecutor {

    private NexusSchematicPlugin plugin;

    public CreateSchematicCommand(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());
            if(player.hasPermission("nexus.schematics")) {
                if(schematicPlayer.getLocations().size() > 1) {
                    if(args.length == 2) {
                        try {
                            int zDistance = Integer.parseInt(args[1]);
                            SchematicType schematicType = SchematicType.valueOf(args[0]);
                            File typeFile = plugin.getFileManager().getSchematicFilesMap().get(schematicType);

                            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(typeFile);
                            for(int i = 0; i < 2; i++) {
                                fileConfiguration.set("corner" + i + ".world", schematicPlayer.getLocations().get(i).getWorld().getName());
                                fileConfiguration.set("corner" + i + ".x", schematicPlayer.getLocations().get(i).getBlockX());
                                fileConfiguration.set("corner" + i + ".y", schematicPlayer.getLocations().get(i).getBlockY());
                                fileConfiguration.set("corner" + i + ".z", schematicPlayer.getLocations().get(i).getBlockZ());
                            }
                            fileConfiguration.set("zDistance", zDistance);
                            try {
                                fileConfiguration.save(typeFile);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            player.sendMessage("§aThe values were set into the config");
                        } catch (IllegalArgumentException exception) {
                            sendHelp(player);
                        }
                    } else {
                        sendHelp(player);
                    }
                } else {
                    player.sendMessage("§cYou didn't set location 1 and 2");
                }
            } else {
                player.sendMessage(plugin.getNO_PERMISSION());
            }
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§cSyntax: /createschematic <SchematicType> <zDistance>");
        StringBuilder schematicTypes = new StringBuilder();
        for(SchematicType type : SchematicType.values()) {
            schematicTypes.append(type).append(", ");
        }
        player.sendMessage("§cSchematicTypes: " + schematicTypes);
    }
}
