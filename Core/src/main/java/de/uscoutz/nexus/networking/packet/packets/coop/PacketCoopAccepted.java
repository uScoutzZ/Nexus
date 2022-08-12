package de.uscoutz.nexus.networking.packet.packets.coop;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.networking.packet.packets.profiles.PacketPlayerReloadProfiles;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;

import java.util.UUID;

public class PacketCoopAccepted extends Packet {

    private UUID profileId, acceptor;
    private String acceptorName, serverName;
    private int profileSlot;

    public PacketCoopAccepted(String password, UUID profileId, UUID acceptor, String acceptorName, int profileSlot, String serverName) {
        super(password);
        this.profileId = profileId;
        this.acceptor = acceptor;
        this.acceptorName = acceptorName;
        this.profileSlot = profileSlot;
        this.serverName = serverName;
    }

    @Override
    public Object execute() {
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(
                NexusPlugin.getInstance().getProfileManager().getProfilesMap().get(profileId).getOwner());

        nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate(
                "de_DE", "command_coop_accept_success", acceptorName));
        Profile profile = NexusPlugin.getInstance().getProfileManager().getProfilesMap().get(profileId);
        profile.addPlayer(profileSlot, acceptor);
        new PacketPlayerReloadProfiles("123", acceptor).send(
                CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName(serverName));
        return this;
    }
}
