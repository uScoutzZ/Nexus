package de.uscoutz.nexus.schematic.commands;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GetSchematicItemCommand implements CommandExecutor {

    private NexusSchematicPlugin plugin;

    public GetSchematicItemCommand(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender.hasPermission("nexus.command.getschematicitem")) {
            Player player = (Player) sender;
            if (args.length >= 2) {
                try {
                    SchematicType schematicType = SchematicType.valueOf(args[0]);
                    int level = Integer.parseInt(args[1]);
                    Schematic schematic = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(Condition.INTACT).get(level);
                    SchematicItem schematicItem = plugin.getSchematicItemManager().getSchematicItemBySchematic().get(schematic);
                    if(schematicItem != null) {
                        player.getInventory().addItem(schematicItem.getItemStack());
                    } else {
                        player.sendMessage("§cThis schematic has no schematic item");
                    }
                } catch (IllegalArgumentException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return false;
    }
}
