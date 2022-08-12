package de.uscoutz.nexus.networking.packet.packets.profiles;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.player.NexusPlayer;

import java.util.UUID;

public class PacketPlayerReloadProfiles extends Packet {

    private UUID player;

    public PacketPlayerReloadProfiles(String password, UUID player) {
        super(password);
        this.player = player;
    }

    @Override
    public Object execute() {
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player);
        nexusPlayer.getPlayer().closeInventory();
        nexusPlayer.loadProfiles();
        return this;
    }
}
