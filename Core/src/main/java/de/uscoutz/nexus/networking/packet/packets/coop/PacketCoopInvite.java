package de.uscoutz.nexus.networking.packet.packets.coop;

import de.uscoutz.nexus.networking.packet.Packet;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PacketCoopInvite extends Packet {

    private UUID player, profileId;

    public PacketCoopInvite(String password, UUID player, UUID profileId) {
        super(password);
        this.player = player;
        this.profileId = profileId;
    }

    @Override
    public Object execute() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tg Online");

        return this;
    }
}
