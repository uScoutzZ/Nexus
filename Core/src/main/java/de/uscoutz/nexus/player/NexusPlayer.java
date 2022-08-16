package de.uscoutz.nexus.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.gamemechanics.tools.Tool;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopAccepted;
import de.uscoutz.nexus.networking.packet.packets.player.PacketPlayerChangeServer;
import de.uscoutz.nexus.networking.packet.packets.profiles.PacketDeleteProfile;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.profile.ProfilePlayer;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import de.uscoutz.nexus.utilities.InventorySerializer;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NexusPlayer {

    private NexusPlugin plugin;

    @Getter
    private UUID uuid;
    @Getter @Setter
    private Player player;
    @Getter
    private Map<Integer, Profile> profilesMap;
    @Getter
    private long firstLogin, generalPlaytime, joined, joinedProfile;
    @Getter @Setter
    private int currentProfileSlot;

    public NexusPlayer(UUID uuid, NexusPlugin plugin) {
        this.plugin = plugin;
        this.uuid = uuid;
        joined = System.currentTimeMillis();
        joinedProfile = System.currentTimeMillis();
        profilesMap = new HashMap<>();
        if(registered()) {
            load();
            loadProfiles();
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getDatabaseAdapter().setAsync("players", uuid, 0, System.currentTimeMillis(),
                            0, GameProfileSerializer.toString(((CraftPlayer) player).getProfile()));
                    load();
                    Profile profile = new Profile(UUID.randomUUID(), plugin);
                    profile.create(uuid, 0);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            loadProfiles();
                        }
                    }.runTaskLater(plugin, 2);
                }
            }.runTaskLater(plugin, 5);
        }
        plugin.getPlayerManager().getPlayersMap().put(uuid, this);
    }

    public void switchProfile(int profileSlot) {
        if(getCurrentProfile() != null && getCurrentProfile().getMembers().containsKey(uuid)) {
            getCurrentProfile().getMembers().get(uuid).checkout(joined);
        }
        //player.getInventory().clear();
        currentProfileSlot = profileSlot;
        setActiveProfile(profileSlot, false);
    }

    public boolean setActiveProfile(int profileSlot, boolean join) {
        Profile profile;
        if(plugin.getNexusServer().getProfileToLoad().containsKey(uuid)) {
            currentProfileSlot = plugin.getNexusServer().getProfileToLoad().remove(uuid);
            profile = profilesMap.get(currentProfileSlot);
        } else {
            currentProfileSlot = profileSlot;
            profile = profilesMap.get(profileSlot);
        }
        joinedProfile = System.currentTimeMillis();
        //ICloudService emptiestServer = plugin.getNexusServer().getEmptiestServer();
        if(profile.isPrepared()) {
            if(!profile.loaded()) {
                if(plugin.getNexusServer().getProfilesServerMap().containsKey(profile.getProfileId())) {
                    String server = plugin.getNexusServer().getProfilesServerMap().get(profile.getProfileId());
                    if(!server.equals(plugin.getNexusServer().getThisServiceName())) {
                        ICloudService iCloudService = plugin.getNexusServer().getServiceByName(server);
                        if(iCloudService != null && iCloudService.isOnline()) {
                            new PacketPlayerChangeServer("123", uuid.toString(), profileSlot).send(iCloudService);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    CloudAPI.getInstance().getCloudPlayerManager().connectPlayer(CloudAPI.getInstance().getCloudPlayerManager().getCachedCloudPlayer(uuid),
                                            iCloudService);
                                }
                            }.runTaskLater(plugin, 5);
                        } else {
                            plugin.getNexusServer().getProfilesServerMap().remove(profile.getProfileId());
                            return setActiveProfile(profileSlot, join);
                        }
                    } else {
                        plugin.getNexusServer().getProfilesServerMap().remove(profile.getProfileId());
                        return setActiveProfile(profileSlot, join);
                    }
                } else {
                    profile.load();
                    /*if(CloudPlugin.getInstance().getThisServiceName().equals(emptiestServer.getName())) {
                        profile.load(player);
                    } else {
                        player.sendMessage("§dOther server is more empty, sending");
                        CloudAPI.getInstance().getCloudPlayerManager().connectPlayer(CloudAPI.getInstance().getCloudPlayerManager().getCachedCloudPlayer(player.getUniqueId()), emptiestServer);
                    }*/
                }
            }
        }

        if(profile.loaded()) {
            profile.cancelCheckout();
            if(join) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        finishProfileLoading(false, profile);
                    }
                }.runTaskLater(plugin, 10);
            } else {
                finishProfileLoading(join, profile);
            }
            return true;
        } else {
            return false;
        }
    }

    public void finishProfileLoading(boolean join, Profile profile) {
        if(!join) {
            player.teleport(plugin.getLocationManager().getLocation("base-spawn", profile.getWorld().getWorld()));
        }
        player.getInventory().clear();

        if(!profile.getMembers().get(uuid).getInventoryBase64().equals("empty")) {
            player.getInventory().setContents(InventorySerializer.fromBase64(profile.getMembers().get(
                    player.getUniqueId()).getInventoryBase64()).getContents());
            for(ItemStack itemStack : player.getInventory().getContents()) {
                if(itemStack != null && itemStack.getItemMeta() != null) {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if(plugin.getToolManager().isTool(itemMeta)) {
                        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                        NamespacedKey namespacedKey = new NamespacedKey(plugin.getName().toLowerCase(), "breakingpower");
                        int breakingPower = dataContainer.get(namespacedKey, PersistentDataType.INTEGER);
                        String key = dataContainer.get(new NamespacedKey(plugin.getName().toLowerCase(), "key"), PersistentDataType.STRING);
                        Tool tool = plugin.getToolManager().getToolMap().get(key);
                        int toolBreakingPower = tool.getBreakingPower();
                        if(breakingPower != toolBreakingPower) {
                            Objects.requireNonNull(itemMeta.lore()).clear();
                            itemMeta.lore(tool.getItemStack().lore());
                            itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, toolBreakingPower);
                        }

                        if(tool.getLocale() != null) {
                            String displayName = plugin.getLocaleManager().translate("de_DE", tool.getLocale());
                            if(itemMeta.hasDisplayName() && !itemMeta.getDisplayName().equals(displayName)) {
                                itemMeta.displayName(Component.text(displayName));
                            }
                        } else {
                            itemMeta.displayName(Component.text(""));
                        }

                        itemStack.setItemMeta(itemMeta);
                    }
                }
            }
        }

        for(Tool tool : plugin.getToolManager().getToolMap().values()) {
            player.getInventory().addItem(tool.getItemStack());
        }
    }

    public void checkout() {
        plugin.getDatabaseAdapter().updateAsync("players", "player", uuid,
                new DatabaseUpdate("playtime", generalPlaytime + (System.currentTimeMillis()-joined)),
                new DatabaseUpdate("gameprofile", GameProfileSerializer.toString(((CraftPlayer) player).getProfile())),
                new DatabaseUpdate("currentProfile", currentProfileSlot));
        if(getCurrentProfile().loaded() && getCurrentProfile().getMembers().containsKey(uuid)) {
            getCurrentProfile().getMembers().get(uuid).checkout(joined);
        }
        plugin.getPlayerManager().getPlayersMap().remove(uuid);
    }

    public Profile getCurrentProfile() {
        return profilesMap.get(currentProfileSlot);
    }

    public boolean registered() {
        return plugin.getDatabaseAdapter().keyExistsAsync("players", "player", uuid);
    }

    private void load() {
        ResultSet resultSet = plugin.getDatabaseAdapter().getAsync("players", "player", String.valueOf(uuid));
        try {
            if(resultSet.next()) {
                firstLogin = resultSet.getLong("firstLogin");
                generalPlaytime = resultSet.getLong("playtime");
                currentProfileSlot = resultSet.getInt("currentProfile");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadProfiles() {
        profilesMap.clear();
        ResultSet resultSet = plugin.getDatabaseAdapter().get("playerProfiles", "player", String.valueOf(uuid));
        try {
            while(resultSet.next()) {
                UUID profileId = UUID.fromString(resultSet.getString("profileId"));
                int slot = resultSet.getInt("slot");
                Profile profile;
                if(plugin.getProfileManager().getProfilesMap().containsKey(profileId)) {
                    profile = plugin.getProfileManager().getProfilesMap().get(profileId);
                } else {
                    profile = new Profile(profileId, plugin);
                }
                profilesMap.put(slot, profile);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void openProfiles() {
        openProfiles(null);
    }

    public void openProfiles(String coopInvitation) {
        SimpleInventory inventory = InventoryBuilder.create(3*9, plugin.getLocaleManager().translate("de_DE", "profiles-title"));

        int[] slots = new int[]{11, 12, 14, 15};
        int currentSlot = 0;
        for(int i : slots) {
            Material material;
            int finalCurrentSlot = currentSlot;
            List<String> lore = Arrays.asList(" ", plugin.getLocaleManager().translate("de_DE", "profiles_click-to-create"));
            Profile profile = profilesMap.get(finalCurrentSlot);
            if(profilesMap.containsKey(currentSlot)) {
                if(coopInvitation == null) {
                    material = Material.GOLDEN_PICKAXE;
                    StringBuilder members = new StringBuilder();
                    int memberCount = 0;
                    for(ProfilePlayer profilePlayer : profile.getMembers().values()) {
                        memberCount++;
                        if(memberCount == profile.getMembers().size()) {
                            members.append(profilePlayer.getGameProfile().getName());
                        } else {
                            members.append(profilePlayer.getGameProfile().getName() + "§7, §6");
                        }
                    }
                    lore = Arrays.asList(" ", plugin.getLocaleManager().translate("de_DE", "profiles_owner", profile.getMembers().get(profile.getOwner()).getGameProfile().getName()),
                            plugin.getLocaleManager().translate("de_DE", "profiles_members", members), " ",
                            plugin.getLocaleManager().translate("de_DE", "profiles_nexus-level", profile.getNexusLevel()));
                } else {
                    material = Material.BARRIER;
                }
            } else {
                material = Material.WOODEN_PICKAXE;
            }
            ItemBuilder itemBuilder = ItemBuilder.create(material);
            itemBuilder.name(plugin.getLocaleManager().translate("de_DE", "profile-slot", String.valueOf((currentSlot+1))));
            itemBuilder.lore(lore);
            if(coopInvitation == null && currentProfileSlot == currentSlot) {
                itemBuilder.enchant(Enchantment.LUCK, 1).flag(ItemFlag.HIDE_ENCHANTS);
            }
            itemBuilder.flag(ItemFlag.HIDE_ATTRIBUTES);

            if(coopInvitation == null) {
                inventory.setItem(i, itemBuilder, leftClick -> {
                    SimpleInventory simpleInventory = InventoryBuilder.create(3*9, plugin.getLocaleManager().translate("de_DE", "profiles_members-title"));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                    simpleInventory.fill(0, 9, ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE).name(" "));
                    simpleInventory.setItem(4, ItemBuilder.skull()
                            .skinURL("https://textures.minecraft.net/texture/b056bc1244fcff99344f12aba42ac23fee6ef6e3351d27d273c1572531f")
                            .name(plugin.getLocaleManager().translate("de_DE", "profiles_coop"))
                            .lore(plugin.getLocaleManager().translate("de_DE", "profiles_coop_lore")));
                    simpleInventory.setItem(8, ItemBuilder.create(Material.BARRIER)
                            .name(plugin.getLocaleManager().translate("de_DE", "profiles_delete-profile"))
                            .lore(plugin.getLocaleManager().translate("de_DE", "profiles_delete-profile_lore")), deleteLeftClick -> {
                        if(finalCurrentSlot != 0) {
                            SimpleInventory deleteInventory = InventoryBuilder.create(3*9, plugin.getLocaleManager().translate("de_DE", "profiles_delete-profile"));
                            deleteInventory.setItem(13, ItemBuilder.skull()
                                    .skinURL("https://textures.minecraft.net/texture/a92e31ffb59c90ab08fc9dc1fe26802035a3a47c42fee63423bcdb4262ecb9b6")
                                    .name(plugin.getLocaleManager().translate("de_DE", "profiles_delete-profile-confirm"))
                                    .lore(plugin.getLocaleManager().translate("de_DE", "profiles_delete-profile-confirm_lore")), confirmLeftClick -> {
                                if(plugin.getNexusServer().getProfilesServerMap().containsKey(profile.getProfileId())) {
                                    new PacketDeleteProfile("123", profile.getProfileId())
                                            .send(CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName(
                                                    plugin.getNexusServer().getProfilesServerMap().get(profile.getProfileId())));
                                } else {
                                    profile.delete();
                                }
                                player.closeInventory();
                                player.sendMessage(plugin.getLocaleManager().translate("de_DE", "profile-deleted", (finalCurrentSlot+1)));
                            });
                            deleteInventory.open(player);
                        } else {
                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_profile_delete_not-deletable"));
                        }
                    });

                    if(profile.getMembers() != null) {
                        for(ProfilePlayer profilePlayer : profile.getMembers().values()) {
                            if(profilePlayer != null && profilePlayer.getGameProfile() != null) {
                                simpleInventory.addItem(ItemBuilder.skull().owner(profilePlayer.getGameProfile()).name("§7" + profilePlayer.getGameProfile().getName()).lore(
                                        plugin.getLocaleManager().translate("de_DE", "profiles_members_joined", sdf.format(new Date(profilePlayer.getJoinedProfile()))),
                                        plugin.getLocaleManager().translate("de_DE", "profiles_members_playtime", profilePlayer.getOnlineTime())));
                            }
                        }
                    }
                    simpleInventory.open(player);
                }, rightClick -> {
                    if(material == Material.GOLDEN_PICKAXE) {
                        if(currentProfileSlot == finalCurrentSlot) {
                            player.sendMessage("§cAlready on this profile");
                        } else {
                            switchProfile(finalCurrentSlot);
                        }
                    } else {
                        Profile newProfile = new Profile(UUID.randomUUID(), plugin);
                        newProfile.create(player.getUniqueId(), finalCurrentSlot);
                        newProfile.prepare();
                        profilesMap.put(finalCurrentSlot, newProfile);
                        switchProfile(finalCurrentSlot);
                    }
                });
            } else {
                inventory.setItem(i, itemBuilder, rightClick -> {
                    if(material == Material.WOODEN_PICKAXE) {
                        UUID profileId = UUID.fromString(coopInvitation);
                        ICloudService iCloudService = CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName(
                                plugin.getNexusServer().getProfilesServerMap().get(profileId));
                        plugin.getProfileManager().getCoopInvitations().get(player.getUniqueId()).remove(profileId);
                        new PacketCoopAccepted("123", profileId, uuid, player.getName(), finalCurrentSlot, plugin.getNexusServer().getThisServiceName()).send(iCloudService);
                    }
                });
            }
            currentSlot++;
        }

        inventory.open(player);
    }
}
