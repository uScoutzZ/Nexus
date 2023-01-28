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

    public NexusScoreboard(NexusPlugin plugin, NexusPlayer nexusPlayer) {
        this.plugin = plugin;
        entriesByType = new HashMap<>();
        scoresByType = new HashMap<>();
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("abcd", "abcd");

        int maxScore = ScoreboardUpdateType.values().length*3;

        objective.displayName(Component.text("§lAPOTOX.NET"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("§1").setScore(maxScore--);

        int i = 0;
        for(ScoreboardUpdateType type : ScoreboardUpdateType.values()) {
            objective.getScore(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), "scoreboard_" +
                    type.toString().toLowerCase() + "-title")).setScore(maxScore--);
            Team team = scoreboard.registerNewTeam(type.toString().toLowerCase());
            team.addEntry("§" + i + "§a");
            team.setSuffix("§cLoading...");
            team.setPrefix("§7");
            objective.getScore("§" + i + "§a").setScore(maxScore--);
            entriesByType.put(type, "§" + i + "§a");
            scoresByType.put(type, maxScore+1);

            objective.getScore("§" + i + "§b").setScore(maxScore--);
            i++;
        }
    }

    public void setup(Player player) {
        this.player = player;
        this.nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
        this.profile = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld());
        player.setScoreboard(scoreboard);
    }

    public void update(ScoreboardUpdateType type) {
        Team team = scoreboard.getTeam(type.toString().toLowerCase());
        String translationKey = "scoreboard_" + type.toString().toLowerCase() + "-display";
        if(type == ScoreboardUpdateType.BIOME) {
            if(nexusPlayer.getBiome() == null) {
                team.setSuffix(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), translationKey,
                        plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), "biome_travelling")));
            } else {
                team.setSuffix(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), translationKey,
                        plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), nexusPlayer.getBiome().getLocaleKey())));
            }
        } else if(type == ScoreboardUpdateType.NEXUSLEVEL) {
            team.setSuffix(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), translationKey,profile.getNexusLevel()));
        } else if(type == ScoreboardUpdateType.MONEY) {
            team.setSuffix(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), translationKey,
                    profile.getMembers().get(player.getUniqueId()).getMoney()));
        } /*else if(type == ScoreboardUpdateType.SOULS) {
            team.setSuffix(plugin.getLocaleManager().translate(nexusPlayer.getLanguage(), translationKey,
                    profile.getMembers().get(player.getUniqueId()).getSouls()));
        }*/
        objective.getScore(entriesByType.get(type)).setScore(scoresByType.get(type));
    }

    public enum ScoreboardUpdateType {

        NEXUSLEVEL,
        MONEY,
        SOULS,
        BIOME;
    }
}
