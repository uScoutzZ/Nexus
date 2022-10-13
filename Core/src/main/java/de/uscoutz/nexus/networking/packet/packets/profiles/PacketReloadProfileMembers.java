package de.uscoutz.nexus.networking.packet.packets.profiles;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.profile.Profile;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PacketReloadProfileMembers extends Packet {

    private UUID profileId;

    public PacketReloadProfileMembers(String password, UUID profileId) {
        super(password);
        this.profileId = profileId;
    }

    @Override
    public Object execute() {
        if(NexusPlugin.getInstance().getProfileManager().getProfilesMap().containsKey(profileId)) {
            Profile profile = NexusPlugin.getInstance().getProfileManager().getProfilesMap().get(profileId);
            profile.loadMembers();
        }
        return this;
    }
}
