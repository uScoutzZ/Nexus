package de.uscoutz.nexus.listeners.block;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.biomes.Biome;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.profile.ProfilePlayer;
import de.uscoutz.nexus.skills.Skill;
import de.uscoutz.nexus.worlds.NexusWorld;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BlockBreakListener implements Listener {

    private NexusPlugin plugin;
    private List<Location> destroyed;

    public BlockBreakListener(NexusPlugin plugin) {
        this.plugin = plugin;
        destroyed = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld());
        ProfilePlayer profilePlayer = profile.getMembers().get(player.getUniqueId());

        if(event.isCancelled()) {
            return;
        }

        if(player.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(false);
            return;
        }

        ItemMeta itemMeta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        int blockResistance, breakingPower = 0;
        Biome biome = plugin.getBiomeManager().getBiome(event.getBlock().getLocation());
        if(biome == null || !biome.getBlockResistance().containsKey(event.getBlock().getType())) {
            event.setCancelled(true);
            return;
        } else {
            blockResistance = biome.getBlockResistance().get(event.getBlock().getType());
            if(itemMeta != null && plugin.getToolManager().isTool(itemMeta)) {
                breakingPower = plugin.getToolManager().getBreakingPower(itemMeta);
            }
        }

        if(breakingPower >= blockResistance) {
            int addedXP = 0;
            Skill skill = null;
            event.setCancelled(false);
            Material material = plugin.getToolManager().getBlockDrop().getOrDefault(event.getBlock().getType(), event.getBlock().getType());
            profilePlayer.getBrokenBlocks().put(material, profilePlayer.getBrokenBlocks().getOrDefault(material, 0) + 1);
            int rangeMin = plugin.getConfig().getInt("respawn-range-min");
            int rangeMax = plugin.getConfig().getInt("respawn-range-max");
            long respawnAfter = new Random().nextLong(rangeMax-rangeMin)+rangeMin;
            if(event.getBlock().getType().toString().contains("_LOG") || event.getBlock().getType().toString().contains("_WOOD")
                    || event.getBlock().getType().toString().contains("_LEAVES")) {
                if(!event.getBlock().getType().toString().contains("_LEAVES")) {
                    addedXP = 6;
                    skill = Skill.WOODCUTTING;
                }
                NexusWorld nexusWorld = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld()).getWorld();
                boolean inList = false;
                Location clonedLocation = event.getBlock().getLocation().clone();
                for(Location location : nexusWorld.getBrokenBlocks().keySet()) {
                    clonedLocation.setY(location.getY());
                    if(location.distance(clonedLocation) < 5) {
                        inList = true;
                        nexusWorld.getBrokenBlocks().get(location).put(event.getBlock().getLocation(), event.getBlock().getBlockData());
                        break;
                    }
                }
                if(!inList) {
                    nexusWorld.getBrokenBlocks().put(event.getBlock().getLocation(), new HashMap<>());
                    nexusWorld.getBrokenBlocks().get(event.getBlock().getLocation()).put(event.getBlock().getLocation(), event.getBlock().getBlockData().clone());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for(Location location : nexusWorld.getBrokenBlocks().get(event.getBlock().getLocation()).keySet()) {
                                Block block = event.getBlock().getWorld().getBlockAt(location);
                                BlockData blockData = nexusWorld.getBrokenBlocks().get(event.getBlock().getLocation()).get(location);
                                block.setType(blockData.getMaterial());
                                block.setBlockData(blockData);
                            }

                            nexusWorld.getBrokenBlocks().remove(event.getBlock().getLocation());
                        }
                    }.runTaskLater(plugin, respawnAfter*20);
                }
            } else {
                List<ItemStack> drops = new ArrayList<>(event.getBlock().getDrops());
                if(event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                    drops.clear();
                    drops.add(new ItemStack(event.getBlock().getType()));
                }
                event.setDropItems(false);
                for(ItemStack drop : drops) {
                    Item item = event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().clone().add(0, 0.75, 0), drop);
                    //item.setVelocity(player.getLocation().toVector().subtract(item.getLocation().toVector())/*.normalize().multiply(0.5)*/);
                }
                BlockData blockData = event.getBlock().getBlockData().clone();
                if(!destroyed.contains(event.getBlock().getLocation())) {
                    destroyed.add(event.getBlock().getLocation());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            event.getBlock().setType(blockData.getMaterial());
                            event.getBlock().setBlockData(blockData);
                            destroyed.remove(event.getBlock().getLocation());
                        }
                    }.runTaskLater(plugin, respawnAfter*20);
                }
                List<Material> toStone = Arrays.asList(Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,
                        Material.COAL_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_DIAMOND_ORE,
                        Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COAL_ORE);
                if(blockData.getMaterial().isSolid()) {
                    if(blockResistance != 0) {
                        skill = Skill.MINING;
                        if(toStone.contains(blockData.getMaterial())) {
                            addedXP = 4;
                        } else {
                            addedXP = 2;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(toStone.contains(blockData.getMaterial())) {
                                    event.getBlock().setType(Material.STONE);
                                } else {
                                    event.getBlock().setType(Material.BEDROCK);
                                }
                            }
                        }.runTaskLater(plugin, 1);
                    }
                } else {
                    skill = Skill.FARMING;
                    addedXP = 4;
                }
            }
            Skill finalSkill = skill;
            int finalAddedXP = addedXP;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(finalSkill != null && (finalSkill == Skill.MINING || event.getBlock().getLocation().getBlock().getType() == Material.AIR)) {
                        profilePlayer.addSkillXP(finalSkill, finalAddedXP);
                    }
                }
            }.runTaskLater(plugin, 1);

        } else {
            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "tool-break_too-high-resistance"));
            event.setCancelled(true);
        }
    }
}
