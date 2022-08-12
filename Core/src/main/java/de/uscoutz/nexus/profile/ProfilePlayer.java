package de.uscoutz.nexus.profile;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import de.uscoutz.nexus.utilities.InventorySerializer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProfilePlayer {

    private NexusPlugin plugin;

    @Getter
    private Profile profile;
    @Getter
    private long playtime, joinedProfile;
    @Getter
    private UUID playerUUID;
    @Getter
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

        ResultSet resultSet = plugin.getDatabaseAdapter().get("players", "player", String.valueOf(playerUUID));
        try {
            if(resultSet.next()) {
                gameProfile = GameProfileSerializer.fromString(resultSet.getString("gameprofile"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        profile.getMembers().put(playerUUID, this);
    }

    public void checkout(long joined) {
        Player player = Bukkit.getPlayer(playerUUID);
        inventoryBase64 = InventorySerializer.toBase64(player.getInventory());
        playtime = playtime + (System.currentTimeMillis()-joined);
        plugin.getDatabaseAdapter().updateTwoAsync("playerProfiles", "profileId", profile.getProfileId(),
                "player", playerUUID, new DatabaseUpdate("playtime", playtime),
                new DatabaseUpdate("inventory", inventoryBase64));
    }

    public String getOnlineTime() {
        long different = playtime;

        int seconds = 0;
        int minuts = 0;
        int hours = 0;

        while (different > 1000) {
            different-=1000;
            seconds++;
        }
        while (seconds > 60) {
            seconds-=60;
            minuts++;
        }
        while (minuts > 60) {
            minuts-=60;
            hours++;
        }

        if(hours == 0) {
            return plugin.getLocaleManager().translate("de_DE", "minutes", String.valueOf(minuts));
        } else if(hours == 1) {
            return plugin.getLocaleManager().translate("de_DE", "hour");
        } else {
            return plugin.getLocaleManager().translate("de_DE", "hours", String.valueOf(hours));
        }
    }
}
