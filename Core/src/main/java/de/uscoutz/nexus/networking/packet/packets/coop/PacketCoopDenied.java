package de.uscoutz.nexus.networking.packet.packets.coop;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.player.NexusPlayer;

import java.util.UUID;

public class PacketCoopDenied extends Packet {

    private UUID profileId;
    private String sender;

    public PacketCoopDenied(String password, UUID profileId, String sender) {
        super(password);
        this.profileId = profileId;
        this.sender = sender;
    }

    @Override
    public Object execute() {
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(
                NexusPlugin.getInstance().getProfileManager().getProfilesMap().get(profileId).getOwner());

        nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate(nexusPlayer.getLanguage(), "command_coop_deny", sender));

        return this;
    }
}
