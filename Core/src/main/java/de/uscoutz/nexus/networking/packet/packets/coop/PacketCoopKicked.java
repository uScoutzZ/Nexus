package de.uscoutz.nexus.networking.packet.packets.coop;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PacketCoopKicked extends Packet {

    private UUID player, profileId;
    private boolean kicked;

    public PacketCoopKicked(String password, UUID player, UUID profileId, boolean kicked) {
        super(password);
        this.player = player;
        this.profileId = profileId;
        this.kicked = kicked;
    }

    @Override
    public Object execute() {
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player);
        int maxProfiles = NexusPlugin.getInstance().getConfig().getInt("profile-slots");
        if(nexusPlayer.getPlayer().hasPermission("nexus.profile.unlimited")) {
            maxProfiles = 45;
        }
        if(nexusPlayer.getCurrentProfile().getProfileId().equals(profileId)) {
            nexusPlayer.getCurrentProfile().kickPlayer(player);
            int finalMaxProfiles = maxProfiles;
            new BukkitRunnable() {
                @Override
                public void run() {
                    nexusPlayer.loadProfiles();
                    if(kicked) {
                        nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "command_coop_kicked_other-profiles"));
                    }
                    final int[] lowestSlot = {-1};
                    for(int i = 0; i < finalMaxProfiles; i++) {
                        if(nexusPlayer.getProfilesMap().get(i) != null) {
                            if(!nexusPlayer.getProfilesMap().get(i).getProfileId().equals(profileId)) {
                                lowestSlot[0] = i;
                                break;
                            }
                        }
                    }
                    if(lowestSlot[0] == -1) {
                        Profile profile = new Profile(UUID.randomUUID(), NexusPlugin.getInstance());
                        profile.create(player, 0);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                profile.prepare();
                                nexusPlayer.loadProfiles();
                                lowestSlot[0] = 0;
                                nexusPlayer.switchProfile(lowestSlot[0]);
                            }
                        }.runTaskLater(NexusPlugin.getInstance(), 2);
                    } else {
                        nexusPlayer.switchProfile(lowestSlot[0]);
                    }
                }
            }.runTaskLater(NexusPlugin.getInstance(), 2);
        } else {
            for (int i = 0; i < maxProfiles; i++) {
                if (nexusPlayer.getProfilesMap().containsKey(i)) {
                    Profile profile = nexusPlayer.getProfilesMap().get(i);
                    if(profile.getProfileId().equals(profileId)) {
                        profile.kickPlayer(player);
                        int finalI = i;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                nexusPlayer.loadProfiles();
                                if(kicked) {
                                    nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "command_coop__kick_kicked", (finalI +1)));
                                }
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