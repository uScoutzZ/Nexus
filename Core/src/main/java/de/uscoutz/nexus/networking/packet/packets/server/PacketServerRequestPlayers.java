package de.uscoutz.nexus.networking.packet.packets.server;

import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.networking.packet.packets.player.PacketAddTablistPlayer;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import net.animalshomeland.bukkit.piglin.Piglin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PacketServerRequestPlayers extends Packet {

    private String cloudService;

    public PacketServerRequestPlayers(String password, String cloudService) {
        super(password);
        this.cloudService = cloudService;
    }

    @Override
    public Object execute() {
        for(Player players : Bukkit.getOnlinePlayers()) {
            Piglin.getInstance().getPermissionManager().getPermissionPlayer(players.getUniqueId()).ifPresentOrElse(permissionPlayer -> {
                Piglin.getInstance().getGroupManager().getGroup(permissionPlayer.getRang().toLowerCase()).ifPresentOrElse(group -> {
                    new PacketAddTablistPlayer("123", players.getUniqueId(), players.getName(), players.getPlayerListName(), group.getJoinPower() + "", group.getPrefix(), group.getColor(), group.getJoinPower().toString()).send(Objects.requireNonNull(CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName(cloudService)));
                }, () -> Bukkit.getConsoleSender().sendMessage("Group of player " + players.getName() + " not found!"));
            }, () -> Bukkit.getConsoleSender().sendMessage("Data of player " + players.getName() + " not found!"));
        }
        return null;
    }
}
