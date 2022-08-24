package de.uscoutz.nexus.networking.packet.packets.profiles;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;

public class PacketRequestProfilesCount extends Packet {

    @Getter
    private String serverName;

    public PacketRequestProfilesCount(String password, String serverName) {
        super(password);
        this.serverName = serverName;
    }

    @Override
    public Object execute() {
        int count = 0;
        for(Profile profile : NexusPlugin.getInstance().getProfileManager().getProfilesMap().values()) {
            if(profile.loaded()) {
                count++;
            }
        }

        new PacketSendProfilesCount("123", count,
                NexusPlugin.getInstance().getNexusServer().getThisServiceName()).send(
                        NexusPlugin.getInstance().getNexusServer().getServiceByName(serverName));

        return this;
    }
}
