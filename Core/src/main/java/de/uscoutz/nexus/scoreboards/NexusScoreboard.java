package de.uscoutz.nexus.scoreboards;

import com.google.common.collect.Multimap;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.biomes.BiomeManager;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class NexusScoreboard {

    private NexusPlugin plugin;
    private Scoreboard scoreboard;
    private Objective objective;
    private Player player;
    private NexusPlayer nexusPlayer;
    private Profile profile;
    private Map<ScoreboardUpdateType, String> entriesByType;
    private Map<ScoreboardUpdateType, Integer> scoresByType;

    public NexusScoreboard(NexusPlugin plugin) {
        this.plugin = plugin;
        entriesByType = new HashMap<>();
        scoresByType = new HashMap<>();
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("abcd", "abcd");

        int maxScore = 10;

        objective.displayName(Component.text("§lAPOTOX.NET"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("§1").setScore(maxScore--);

        objective.getScore(plugin.getLocaleManager().translate("de_DE", "scoreboard_nexuslevel")).setScore(maxScore--);
        Team nexusLevel = scoreboard.registerNewTeam("nexuslevel");
        nexusLevel.addEntry("§2§a");
        nexusLevel.setSuffix("§cLoading...");
        nexusLevel.setPrefix(plugin.getLocaleManager().translate("de_DE", "scoreboard_nexuslevel-level"));
        objective.getScore("§2§a").setScore(maxScore--);
        entriesByType.put(ScoreboardUpdateType.NEXUSLEVEL, "§2§a");
        scoresByType.put(ScoreboardUpdateType.NEXUSLEVEL, maxScore+1);

        objective.getScore("§2").setScore(maxScore--);

        objective.getScore(plugin.getLocaleManager().translate("de_DE", "scoreboard_money")).setScore(maxScore--);
        Team money = scoreboard.registerNewTeam("money");
        money.addEntry("§3§a");
        money.setSuffix("§cLoading...");
        money.setPrefix("");
        objective.getScore("§3§a").setScore(maxScore--);
        entriesByType.put(ScoreboardUpdateType.MONEY, "§3§a");
        scoresByType.put(ScoreboardUpdateType.MONEY, maxScore+1);

        objective.getScore("§2").setScore(maxScore--);

        objective.getScore(plugin.getLocaleManager().translate("de_DE", "scoreboard_biome")).setScore(maxScore--);
        Team biome = scoreboard.registerNewTeam("biome");
        biome.addEntry("§1§a");
        biome.setSuffix("§cLoading...");
        biome.setPrefix("§7⏣ ");
        objective.getScore("§1§a").setScore(maxScore--);
        entriesByType.put(ScoreboardUpdateType.BIOME, "§1§a");
        scoresByType.put(ScoreboardUpdateType.BIOME, maxScore+1);

        objective.getScore("§3").setScore(maxScore--);
    }

    public void setup(Player player) {
        this.player = player;
        this.nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
        this.profile = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld());
        player.setScoreboard(scoreboard);
    }

    public void update(ScoreboardUpdateType type) {
        if(type == ScoreboardUpdateType.BIOME) {
            Team biome = scoreboard.getTeam("biome");
            if(nexusPlayer.getBiome() == null) {
                biome.setSuffix(plugin.getLocaleManager().translate("de_DE", "biome_travelling"));
            } else {
                biome.setSuffix(plugin.getLocaleManager().translate("de_DE", nexusPlayer.getBiome().getLocaleKey()));
            }
        } else if(type == ScoreboardUpdateType.NEXUSLEVEL) {
            Team nexusLevel = scoreboard.getTeam("nexuslevel");
            nexusLevel.setSuffix("§b" + profile.getNexusLevel());
        } else if(type == ScoreboardUpdateType.MONEY) {
            Team money = scoreboard.getTeam("money");
            money.setSuffix("§a" + profile.getMembers().get(player.getUniqueId()).getMoney() + "§2$");
        }
        objective.getScore(entriesByType.get(type)).setScore(scoresByType.get(type));
    }

    public enum ScoreboardUpdateType {

        BIOME,
        NEXUSLEVEL,
        MONEY;
    }
}
