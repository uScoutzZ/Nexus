package de.uscoutz.nexus.schematic.commands;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.DestroyAnimation;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemoveSchematicCommand implements CommandExecutor {

    private NexusSchematicPlugin plugin;

    public RemoveSchematicCommand(NexusSchematicPlugin nexusSchematicPlugin) {
        plugin = nexusSchematicPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player) {
            SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());
            Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
            if(schematicPlayer.getBreaking() != null && args.length != 0 && UUID.fromString(args[0]).equals(schematicPlayer.getBreaking().getSchematicId())) {
                BuiltSchematic builtSchematic = schematicPlayer.getBreaking();
                SchematicItem schematicItem = plugin.getSchematicItemManager().getSchematicItemBySchematic().get(builtSchematic.getSchematic());
                Schematic.destroy(profile, builtSchematic.getSchematicId(), plugin, DestroyAnimation.PLAYER, builtSchematic.getSchematic().getSchematicType());
                player.getInventory().addItem(schematicItem.getItemStack(builtSchematic.getSchematicId()));
            } else {
                player.sendMessage("§cThe request expired");
            }
        }

        return false;
    }
}
