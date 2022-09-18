package de.uscoutz.nexus.scoreboards;

import com.google.common.collect.Multimap;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.biomes.BiomeManager;
import de.uscoutz.nexus.player.NexusPlayer;
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
    private Map<ScoreboardUpdateType, String> entriesByType;
    private Map<ScoreboardUpdateType, Integer> scoresByType;

    public NexusScoreboard(NexusPlugin plugin) {
        this.plugin = plugin;
        entriesByType = new HashMap<>();
        scoresByType = new HashMap<>();
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("abcd", "abcd");

        int maxScore = 4;

        objective.displayName(Component.text("§lAPOTOX.NET"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("§1").setScore(maxScore--);

        objective.getScore(plugin.getLocaleManager().translate("de_DE", "scoreboard_biome")).setScore(maxScore--);
        Team biome = scoreboard.registerNewTeam("biome");
        biome.addEntry("§1§a");
        biome.setSuffix("§cLoading...");
        biome.setPrefix("§7⏣ ");
        objective.getScore("§1§a").setScore(maxScore--);
        entriesByType.put(ScoreboardUpdateType.BIOME, "§1§a");
        scoresByType.put(ScoreboardUpdateType.BIOME, maxScore+1);

        objective.getScore("§2").setScore(maxScore--);
    }

    public void setup(Player player) {
        this.player = player;
        this.nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
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
        }
        objective.getScore(entriesByType.get(type)).setScore(scoresByType.get(type));
    }
}
