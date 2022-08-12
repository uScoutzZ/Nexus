package de.uscoutz.nexus.networking.packet.packets.profiles;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;

import java.util.UUID;

public class PacketDeleteProfile extends Packet {

    @Getter
    private UUID profileId;

    public PacketDeleteProfile(String password, UUID profileId) {
        super(password);
        this.profileId = profileId;
    }

    @Override
    public Object execute() {
        Profile profile = NexusPlugin.getInstance().getProfileManager().getProfilesMap().get(profileId);
        profile.delete();
        return this;
    }
}
