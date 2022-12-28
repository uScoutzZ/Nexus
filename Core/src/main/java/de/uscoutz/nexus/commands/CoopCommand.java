package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.coop.CoopInvitation;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.PaginatedInventory;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopDenied;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopInvite;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopKicked;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import eu.thesimplecloud.api.player.IOfflineCloudPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.stream.IntStream;

public class CoopCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public CoopCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
            Profile profile = nexusPlayer.getCurrentProfile();

            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("invite")) {
                    if(profile.getOwner().equals(player.getUniqueId())) {
                        ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(args[1]).getBlockingOrNull();
                        if(cloudPlayer != null) {
                            UUID uuid = cloudPlayer.getUniqueId();
                            if(args[0].equalsIgnoreCase("invite")) {
                                if(cloudPlayer.isOnline() && cloudPlayer.getConnectedServer().getName().startsWith(plugin.getConfig().getString("cloudtype"))) {
                                    if(!nexusPlayer.getCurrentProfile().getMembers().containsKey(uuid)) {
                                        if(plugin.getDatabaseAdapter().keyExistsTwoAsync("coopInvitations",
                                                "profileId", profile.getProfileId(), "receiver", uuid)) {
                                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_already-invited"));
                                        } else {
                                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop-request-sent", args[1]));
                                            new PacketCoopInvite("123", uuid, profile.getProfileId(), player.getName()).send(cloudPlayer.getConnectedServer());
                                        }

                                    } else {
                                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_already-in-coop"));
                                    }
                                } else {
                                    IOfflineCloudPlayer offlineCloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getOfflineCloudPlayer(args[1]).getBlockingOrNull();
                                    NexusPlugin.getInstance().getDatabaseAdapter().setAsync("coopInvitations", profile.getProfileId(), player.getName(), offlineCloudPlayer.getUniqueId());
                                    player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop-request-sent", args[1]));
                                }
                            } else if(args[0].equalsIgnoreCase("kick")) {
                                if(!uuid.equals(player.getUniqueId())) {
                                    if(!nexusPlayer.getCurrentProfile().getMembers().containsKey(uuid)) {
                                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop__kick_player-not-in-coop"));
                                    } else {
                                        if(cloudPlayer.isOnline() && cloudPlayer.getConnectedServer().getGroupName().equals(plugin.getConfig().getString("cloudtype"))) {
                                            new PacketCoopKicked("123", uuid, nexusPlayer.getCurrentProfile().getProfileId(), true).send(cloudPlayer.getConnectedServer());
                                        } else {
                                            nexusPlayer.getCurrentProfile().kickPlayer(uuid);
                                        }
                                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop__kick_player-kicked", args[1]));
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                nexusPlayer.getCurrentProfile().getMembers().remove(uuid);
                                                nexusPlayer.getCurrentProfile().loadMembers();
                                            }
                                        }.runTaskLater(plugin, 20);
                                    }
                                } else {
                                    player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_kick-self"));
                                }
                            }
                        } else {
                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_kick_player-not-found"));
                        }
                    } else {
                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_not-owner"));
                        return false;
                    }

                } else {
                    sendHelp(player);
                }
            } else if(args.length == 3) {
                if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny")) {
                    try {
                        UUID profileId = UUID.fromString(args[1]);
                        CoopInvitation coopInvitation = null;
                        for(CoopInvitation invitation : nexusPlayer.getCoopInvitations()) {
                            if (invitation.getSender().equals(args[2]) && invitation.getProfileId().equals(profileId)) {
                                coopInvitation = invitation;
                            }
                        }

                        if(coopInvitation != null) {
                            ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(args[2]).getBlockingOrNull();

                            if (args[0].equalsIgnoreCase("accept")) {
                                if (nexusPlayer.getProfilesMap().size() >= plugin.getConfig().getInt("profile-slots")) {
                                    player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_accept_no-slot"));
                                } else {
                                    nexusPlayer.openProfiles(profileId.toString(), true);
                                }
                            } else if (args[0].equalsIgnoreCase("deny")) {
                                plugin.getDatabaseAdapter().deleteTwoAsync("coopInvitations", "profileId", coopInvitation.getProfileId().toString(), "receiver", player.getUniqueId().toString());
                                nexusPlayer.getCoopInvitations().remove(coopInvitation);
                                player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_denied"));
                                if(cloudPlayer != null) {
                                    new PacketCoopDenied("123", profileId, player.getName()).send(cloudPlayer.getConnectedServer());
                                }
                            }
                        } else {
                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_no-invitation"));
                        }
                    } catch (IllegalArgumentException exception) {
                        player.sendMessage("§cAn error occurred");
                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_no-invitation"));
                        exception.printStackTrace();
                    }
                } else {
                    sendHelp(player);
                }
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("requests")) {
                    PaginatedInventory paginatedInventory = InventoryBuilder.createPaginated(4*9,
                            plugin.getLocaleManager().translate("de_DE", "coop_requests-title"));
                    paginatedInventory.addDynamicSlots(IntStream.range(0, 2*9).toArray());
                    for(CoopInvitation coopInvitation : plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getCoopInvitations()) {
                        paginatedInventory.addItem(ItemBuilder.create(Material.WRITABLE_BOOK)
                                .name(plugin.getLocaleManager().translate("de_DE",
                                        "coop_requests-display", coopInvitation.getSender())).build(), inventoryClickEvent -> {
                            SimpleInventory inventory = InventoryBuilder.create(3*9,
                                    plugin.getLocaleManager().translate("de_DE", "coop_requests_confirm-title", coopInvitation.getSender()));

                            inventory.setItem(12, ItemBuilder.create(Material.LIME_CONCRETE)
                                    .name(plugin.getLocaleManager().translate("de_DE", "accept"))
                                    .build(), inventoryClickEvent1 -> {
                                player.performCommand("coop accept " + coopInvitation.getProfileId() + " " + coopInvitation.getSender());
                            });
                            inventory.setItem(14, ItemBuilder.create(Material.RED_CONCRETE)
                                    .name(plugin.getLocaleManager().translate("de_DE", "deny"))
                                    .build(), inventoryClickEvent1 -> {
                                player.performCommand("coop deny " + coopInvitation.getProfileId() + " " + coopInvitation.getSender());
                                player.closeInventory();
                                player.performCommand("coop requests");
                            });

                            inventory.open(player);
                        });
                    }

                    paginatedInventory.setPageSwitcherForwardSlot(paginatedInventory.getInventory().getSize()-1);
                    paginatedInventory.setPageSwitcherBackSlot(paginatedInventory.getInventory().getSize()-2);
                    paginatedInventory.fill(paginatedInventory.getInventory().getSize()-18, paginatedInventory.getInventory().getSize()-9, ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE).name(" "));

                    paginatedInventory.open(player);
                }
            } else {
                sendHelp(player);
            }
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_help"));
    }
}
