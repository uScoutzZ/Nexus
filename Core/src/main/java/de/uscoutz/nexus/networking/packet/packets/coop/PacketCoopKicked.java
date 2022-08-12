package de.uscoutz.nexus.networking.packet.packets.coop;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PacketCoopKicked extends Packet {

    private UUID player, profileId;

    public PacketCoopKicked(String password, UUID player, UUID profileId) {
        super(password);
        this.player = player;
        this.profileId = profileId;
    }

    @Override
    public Object execute() {
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player);
        if(nexusPlayer.getCurrentProfile().getProfileId().equals(profileId)) {
            nexusPlayer.getCurrentProfile().kickPlayer(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    nexusPlayer.loadProfiles();
                    nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "command_coop_kicked_other-profiles"));
                    nexusPlayer.switchProfile(0);
                    /*if(nexusPlayer.getProfilesMap().size() > 1) {
                        for(int i = 0; i < NexusPlugin.getInstance().getConfig().getInt("profile-slots"); i++) {
                            if(nexusPlayer.getProfilesMap().containsKey(i)) {
                                Profile profile = nexusPlayer.getProfilesMap().get(i);
                                if(profile != nexusPlayer.getCurrentProfile()) {

                                    break;
                                }
                            }
                        }
                    } else {
                        nexusPlayer.getPlayer().kick(Component.text(NexusPlugin.getInstance().getLocaleManager().tranlate("de_DE", "command_coop_kicked_no-profiles")));
                    }*/
                }
            }.runTaskLater(NexusPlugin.getInstance(), 2);
        } else {
            Bukkit.broadcastMessage("other profile " + nexusPlayer.getCurrentProfile().getProfileId());
            for (int i = 0; i < NexusPlugin.getInstance().getConfig().getInt("profile-slots"); i++) {
                if (nexusPlayer.getProfilesMap().containsKey(i)) {
                    Profile profile = nexusPlayer.getProfilesMap().get(i);
                    if(profile.getProfileId().equals(profileId)) {
                        profile.kickPlayer(player);
                        int finalI = i;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                nexusPlayer.loadProfiles();
                                nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "command_coop__kick_kicked", (finalI +1)));
                            }
                        }.runTaskLater(NexusPlugin.getInstance(), 2);

                        break;
                    }
                }
            }
        }
        return this;
    }
}
