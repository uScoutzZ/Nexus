package de.uscoutz.nexus.networking.packet.packets;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.profile.Profile;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PacketKillSessions extends Packet {

    private UUID player, profileId;

    public PacketKillSessions(String password) {
        super(password);
    }

    @Override
    public Object execute() {
        for(Profile profile : NexusPlugin.getInstance().getProfileManager().getProfilesMap().values()) {
            profile.checkout();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if(NexusPlugin.getInstance().getProfileManager().getProfilesMap().size() == 0) {
                    for(UUID uuid : NexusPlugin.getInstance().getNexusServer().getProfilesServerMap().keySet()) {
                        if(NexusPlugin.getInstance().getNexusServer().getProfilesServerMap().get(uuid).equals(
                                NexusPlugin.getInstance().getNexusServer().getThisServiceName())) {
                            NexusPlugin.getInstance().getNexusServer().getProfilesServerMap().remove(uuid);
                        }
                    }
                }
            }
        }.runTaskLater(NexusPlugin.getInstance(), 10);

        return this;
    }
}
