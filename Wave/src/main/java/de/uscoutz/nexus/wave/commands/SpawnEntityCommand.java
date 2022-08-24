package de.uscoutz.nexus.wave.commands;

import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.customentities.NexusZombie;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

public class SpawnEntityCommand implements CommandExecutor {

    private NexusWavePlugin plugin;

    public SpawnEntityCommand(NexusWavePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();
            NexusZombie nexusZombie = new NexusZombie(player.getLocation(), plugin);
            world.tryAddFreshEntityWithPassengers(nexusZombie);
        }
        return false;
    }
}
