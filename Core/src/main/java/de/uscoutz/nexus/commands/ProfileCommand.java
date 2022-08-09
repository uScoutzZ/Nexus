package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.localization.Message;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.profile.ProfilePlayer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ProfileCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public ProfileCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player);
            SimpleInventory inventory = InventoryBuilder.create(3*9, plugin.getMessage().get("profiles-title"));

            int[] slots = new int[]{11, 12, 14, 15};
            int currentSlot = 0;
            for(int i : slots) {
                Material material;
                if(nexusPlayer.getProfilesMap().containsKey(currentSlot)) {
                    material = Material.GOLDEN_PICKAXE;
                } else {
                    material = Material.WOODEN_PICKAXE;
                }
                ItemBuilder itemBuilder = ItemBuilder.create(material);
                itemBuilder.name(plugin.getMessage().get("profile-slot", String.valueOf((currentSlot+1))));
                if(nexusPlayer.getCurrentProfileSlot() == currentSlot) {
                    itemBuilder.enchant(Enchantment.LUCK, 1).flag(ItemFlag.HIDE_ENCHANTS);
                }
                itemBuilder.flag(ItemFlag.HIDE_ATTRIBUTES);

                int finalCurrentSlot = currentSlot;
                inventory.setItem(i, itemBuilder, leftClick -> {
                    SimpleInventory simpleInventory = InventoryBuilder.create(3*9, plugin.getMessage().get("profiles_members-title"));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                    for(ProfilePlayer profilePlayer : nexusPlayer.getProfilesMap().get(finalCurrentSlot).getMembers().values()) {
                        simpleInventory.addItem(ItemBuilder.skull().owner(profilePlayer.getGameProfile()).name("§7" + profilePlayer.getGameProfile().getName()).lore(
                                plugin.getMessage().get("profiles_members_joined", sdf.format(new Date(profilePlayer.getJoinedProfile()))),
                                plugin.getMessage().get("profiles_members_playtime", profilePlayer.getOnlineTime())));
                    }

                    simpleInventory.open(player);
                }, rightClick -> {
                    if(material == Material.GOLDEN_PICKAXE) {
                        if(nexusPlayer.getCurrentProfileSlot() == finalCurrentSlot) {
                            player.sendMessage("§cAlready on this profile");
                        } else {
                            nexusPlayer.switchProfile(finalCurrentSlot);
                        }
                    } else {
                        Profile profile = new Profile(UUID.randomUUID(), plugin);
                        profile.create(player.getUniqueId(), finalCurrentSlot);
                        profile.prepare();
                        nexusPlayer.getProfilesMap().put(finalCurrentSlot, profile);
                        nexusPlayer.switchProfile(finalCurrentSlot);
                    }
                });
                currentSlot++;
            }

            inventory.open(player);
        }
        return false;
    }
}
