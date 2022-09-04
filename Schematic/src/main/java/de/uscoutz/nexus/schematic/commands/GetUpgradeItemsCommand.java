package de.uscoutz.nexus.schematic.commands;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GetUpgradeItemsCommand implements CommandExecutor {

    private NexusSchematicPlugin plugin;

    public GetUpgradeItemsCommand(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender.hasPermission("nexus.command.getupgradeitems")) {
            Player player = (Player) sender;
            if(args.length >= 2) {
                try {
                    SchematicType schematicType = SchematicType.valueOf(args[0]);
                    int level = Integer.parseInt(args[1]);
                    List<ItemStack> neededItems = new ArrayList<>(plugin.getCollectorManager().getCollectorNeededMap().get(schematicType).get(Condition.INTACT).get(level));

                    for(ItemStack itemStack : neededItems) {
                        player.getInventory().addItem(itemStack);
                    }
                } catch (IllegalArgumentException exception) {
                    sendHelp(player);
                    exception.printStackTrace();
                }
            } else {
                sendHelp(player);
            }
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§cSyntax: /getupgradeitems <SchematicType> <Level>");
    }
}
