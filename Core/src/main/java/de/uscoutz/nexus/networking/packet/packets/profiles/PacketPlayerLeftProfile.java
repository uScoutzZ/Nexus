package de.uscoutz.nexus.networking.packet.packets.profiles;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import eu.thesimplecloud.api.service.ICloudService;
import lombok.Getter;

import java.util.UUID;

public class PacketPlayerLeftProfile extends Packet {

    @Getter
    private UUID profileId, player;

    public PacketPlayerLeftProfile(String password, UUID player, UUID profileId) {
        super(password);
        this.player = player;
        this.profileId = profileId;
    }

    @Override
    public Object execute() {
        ICloudPlayer playerLeft = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(player).getBlockingOrNull();
        if(playerLeft != null) {
            Profile profile = NexusPlugin.getInstance().getProfileManager().getProfilesMap().get(profileId);
            profile.broadcast("profiles_left-users-profile", playerLeft.getName());
        }

        return this;
    }
}
