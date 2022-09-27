package de.uscoutz.nexus.profile;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.utilities.DateUtilities;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import de.uscoutz.nexus.utilities.InventoryManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProfilePlayer {

    private NexusPlugin plugin;

    @Getter
    private Profile profile;
    @Getter @Setter
    private long playtime, joinedProfile;
    @Getter
    private int kills, deaths;
    @Getter
    private UUID playerUUID;
    @Getter @Setter
    private String inventoryBase64;
    @Getter
    private GameProfile gameProfile;

    public ProfilePlayer(Profile profile, UUID playerUUID, long playtime, long joinedProfile, String inventoryBase64, NexusPlugin plugin) {
        this.profile = profile;
        this.plugin = plugin;
        this.joinedProfile = joinedProfile;
        this.playtime = playtime;
        this.inventoryBase64 = inventoryBase64;
        this.playerUUID = playerUUID;

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

    public void checkout(long joined) {
        Player player = Bukkit.getPlayer(playerUUID);
        inventoryBase64 = InventoryManager.toBase64(player.getInventory());
        playtime = playtime + (System.currentTimeMillis()-joined);
        plugin.getDatabaseAdapter().updateTwo("playerProfiles", "profileId", profile.getProfileId(),
                "player", playerUUID, new DatabaseUpdate("playtime", playtime),
                new DatabaseUpdate("inventory", inventoryBase64));
        plugin.getDatabaseAdapter().updateTwo("playerStats", "player", playerUUID,
                "profileId", profile.getProfileId(),
                new DatabaseUpdate("deaths", deaths),
                new DatabaseUpdate("kills", kills));
        new BukkitRunnable() {
            @Override
            public void run() {
                if(profile.getWorld() != null && profile.getWorld().getWorld() != null && profile.getActivePlayers().size() == 0) {
                    profile.scheduleCheckout();
                }
            }
        }.runTaskLater(plugin, 10);
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
