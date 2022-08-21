package de.uscoutz.nexus.networking.packet.packets.player;

import de.uscoutz.nexus.networking.packet.Packet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketPlayerChat extends Packet {

    private String playerName;
    private String message;

    public PacketPlayerChat(String password, String playerName, String message) {
        super(password);
        this.playerName = playerName;
        this.message = message;
    }

    @Override
    public Object execute() {
        for(Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage(playerName + "§7: §f" + message);
        }
        return this;
    }
}
