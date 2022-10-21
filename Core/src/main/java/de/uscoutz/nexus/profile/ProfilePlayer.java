package de.uscoutz.nexus.profile;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.scoreboards.NexusScoreboard;
import de.uscoutz.nexus.skills.Skill;
import de.uscoutz.nexus.skills.rewards.SkillReward;
import de.uscoutz.nexus.utilities.BiMap;
import de.uscoutz.nexus.utilities.DateUtilities;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import de.uscoutz.nexus.utilities.InventoryManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfilePlayer {

    private NexusPlugin plugin;

    @Getter
    private Profile profile;
    @Getter @Setter
    private long playtime, joinedProfile, money;
    @Getter
    private int kills, deaths;
    @Getter
    private UUID playerUUID;
    @Getter @Setter
    private String inventoryBase64;
    @Getter
    private GameProfile gameProfile;
    @Getter
    private Map<Material, Integer> brokenBlocks;
    @Getter
    private BiMap<Skill, Integer, Integer> skillMap;

    public ProfilePlayer(Profile profile, UUID playerUUID, long playtime, long joinedProfile, String inventoryBase64, long money, NexusPlugin plugin) {
        this.profile = profile;
        this.plugin = plugin;
        this.joinedProfile = joinedProfile;
        this.playtime = playtime;
        this.inventoryBase64 = inventoryBase64;
        this.playerUUID = playerUUID;
        this.money = money;
        brokenBlocks = new HashMap<>();
        skillMap = new BiMap<>();

        for(Skill skill : Skill.values()) {
            skillMap.put(skill, 0, 0);
        }

        ResultSet skillsResultSet = plugin.getDatabaseAdapter().getTwo("skills", "player", String.valueOf(playerUUID), "profileId", String.valueOf(profile.getProfileId()));
        try {
            while(skillsResultSet.next()) {
                Skill skill = Skill.valueOf(skillsResultSet.getString("skill"));
                int level = skillsResultSet.getInt("level");
                int xp = skillsResultSet.getInt("xp");
                skillMap.put(skill, level, xp);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ResultSet resultSet = plugin.getDatabaseAdapter().getTwo("playerStats", "player", String.valueOf(playerUUID), "profileId", String.valueOf(profile.getProfileId()));
        try {
            if(resultSet.next()) {
                deaths = resultSet.getInt("deaths");
                kills = resultSet.getInt("kills");
            } else {
                deaths = 0;
                kills = 0;
                plugin.getDatabaseAdapter().setAsync("playerStats", playerUUID, profile.getProfileId(), 0, 0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ResultSet gameProfileResultSet = plugin.getDatabaseAdapter().get("players", "player", String.valueOf(playerUUID));
        try {
            if(gameProfileResultSet.next()) {
                gameProfile = GameProfileSerializer.fromString(gameProfileResultSet.getString("gameprofile"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        profile.getMembers().put(playerUUID, this);
    }

    public boolean addSkillXP(Skill skill, int xp) {
        boolean levelUp = false;
        if(skill.getSkillLevels().length > skillMap.getValues1().get(skill) &&  skillMap.getValues2().get(skill)+xp >= skill.getSkillLevels()[skillMap.getValues1().get(skill)].getNeededXP()) {
            skillMap.put(skill, skillMap.getValues1().get(skill)+1, skillMap.getValues2().get(skill)+xp - skill.getSkillLevels()[skillMap.getValues1().get(skill)].getNeededXP());
            levelUp = true;
        } else {
            skillMap.getValues2().replace(skill, skillMap.getValues2().get(skill) + xp);
        }
        Player player = Bukkit.getPlayer(playerUUID);
        if(player != null) {
            if(skill.getSkillLevels().length > skillMap.getValues1().get(skill)) {
                player.sendActionBar(Component.text(plugin.getLocaleManager().translate("de_DE", "skill_xp-added",
                        xp, skill.getTitle(), skillMap.getValues2().get(skill), skill.getSkillLevels()[skillMap.getValues1().get(skill)].getNeededXP())));
            } else {
                player.sendActionBar(Component.text(plugin.getLocaleManager().translate("de_DE", "skill_xp-added-maximum",
                        xp, skill.getTitle())));
            }

            if(levelUp) {
                player.sendMessage(plugin.getLocaleManager().translate("de_DE", "skill_level-up", skill.getTitle(), skillMap.getValues1().get(skill)));
                for(SkillReward skillReward : skill.getSkillLevels()[skillMap.getValues1().get(skill)-1].getRewards()) {
                    player.sendMessage("§f    " + skillReward.getDisplay());
                    skillReward.addReward(player);
                }
                player.sendMessage("§f ");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4F, 1.0F);
            }
        }

        return true;
    }

    public void checkout(long joined) {
        Player player = Bukkit.getPlayer(playerUUID);
        inventoryBase64 = InventoryManager.toBase64(player.getInventory());
        Inventory equipment = Bukkit.createInventory(null, 9, "Equipment");
        equipment.setContents(player.getInventory().getArmorContents());
        playtime = playtime + (System.currentTimeMillis()-joined);
        plugin.getDatabaseAdapter().updateTwo("playerProfiles", "profileId", profile.getProfileId(),
                "player", playerUUID, new DatabaseUpdate("playtime", playtime),
                new DatabaseUpdate("inventory", inventoryBase64));
        plugin.getDatabaseAdapter().updateTwo("playerStats", "player", playerUUID,
                "profileId", profile.getProfileId(),
                new DatabaseUpdate("deaths", deaths),
                new DatabaseUpdate("kills", kills));
        for(Material material : brokenBlocks.keySet()) {
            if(!plugin.getDatabaseAdapter().keyExistsThreeAsync("brokenBlocks", "player", playerUUID, "profileId", profile.getProfileId(), "material", material.toString())) {
                plugin.getDatabaseAdapter().setAsync("brokenBlocks", playerUUID, profile.getProfileId(), material.name(), brokenBlocks.get(material));
            } else {
                try {
                    ResultSet resultSet = plugin.getDatabaseAdapter().getThreeAsync("brokenBlocks", "player", playerUUID.toString(), "profileId", profile.getProfileId().toString(), "material", material.toString());
                    if(resultSet.next()) {
                        int currentAmount = resultSet.getInt("amount");
                        plugin.getDatabaseAdapter().updateThreeAsync("brokenBlocks", "player", playerUUID, "profileId", profile.getProfileId(), "material", material.toString(), new DatabaseUpdate("amount", brokenBlocks.get(material)+currentAmount));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for(Skill skill : skillMap.getValues1().keySet()) {
            if(!plugin.getDatabaseAdapter().keyExistsThreeAsync("skills", "player", playerUUID, "profileId", profile.getProfileId(), "skill", skill.toString())) {
                plugin.getDatabaseAdapter().setAsync("skills", profile.getProfileId(), playerUUID, skill.toString(), skillMap.getValues1().get(skill), skillMap.getValues2().get(skill));
            } else {
                plugin.getDatabaseAdapter().updateThreeAsync("skills", "player", playerUUID,
                        "profileId", profile.getProfileId(), "skill", skill.toString(),
                        new DatabaseUpdate("level", skillMap.getValues1().get(skill)),
                        new DatabaseUpdate("xp", skillMap.getValues2().get(skill)));
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                brokenBlocks.clear();
                if(profile.getWorld() != null && profile.getWorld().getWorld() != null && profile.getActivePlayers().size() == 0) {
                    profile.scheduleCheckout();
                }
            }
        }.runTaskLater(plugin, 20);
    }

    public void addMoney(int money) {
        this.money += money;
        plugin.getPlayerManager().getPlayersMap().get(playerUUID).getNexusScoreboard().update(NexusScoreboard.ScoreboardUpdateType.MONEY);
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public String getOnlineTime() {
        return DateUtilities.getTime(0, playtime, plugin);
    }
}
