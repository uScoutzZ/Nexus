package de.uscoutz.nexus.networking.packet.packets.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.player.NexusPlayer;

import java.util.UUID;

public class PacketPlayerChangeServer extends Packet {

    private String player;
    private int profileToLoad;

    public PacketPlayerChangeServer(String password, String player, int profileToLoad) {
        super(password);
        this.player = player;
        this.profileToLoad = profileToLoad;
    }

    @Override
    public Object execute() {
        NexusPlugin.getInstance().getNexusServer().getProfileToLoad().put(UUID.fromString(player), profileToLoad);
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(UUID.fromString(player));
        if(nexusPlayer != null) {
            nexusPlayer.setCurrentProfileSlot(profileToLoad);
        }

        return this;
    }
}
