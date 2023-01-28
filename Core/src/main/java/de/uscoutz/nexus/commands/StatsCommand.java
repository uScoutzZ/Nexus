package de.uscoutz.nexus.commands;

import com.mojang.authlib.GameProfile;
import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.inventory.InventoryBuilder;
import de.uscoutz.nexus.inventory.PaginatedInventory;
import de.uscoutz.nexus.inventory.SimpleInventory;
import de.uscoutz.nexus.item.ItemBuilder;
import de.uscoutz.nexus.utilities.DateUtilities;
import de.uscoutz.nexus.utilities.GameProfileSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

public class StatsCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public StatsCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender.hasPermission("nexus.command.stats")) {
            if(sender instanceof Player player) {
                int limit = 6969;
                if(args.length == 1) {
                    try {
                        limit = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    player.sendMessage("§cUm Lags zu vermeiden, kann auch ein Limit angegeben werden: /stats <limit>");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                PaginatedInventory paginatedInventory = InventoryBuilder.createPaginated(5*9, "Players");
                String language = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage();
                paginatedInventory.addDynamicSlots(IntStream.range(0, 42).toArray());
                ResultSet resultSet = plugin.getDatabaseAdapter().get("players", "firstlogin DESC", limit);

                try {
                    while(resultSet.next()) {
                        GameProfile gameProfile = GameProfileSerializer.fromString(resultSet.getString("gameprofile"));
                        long playtime = resultSet.getLong("playtime");
                        long firstlogin = resultSet.getLong("firstLogin");
                        paginatedInventory.addItem(ItemBuilder.skull().owner(gameProfile).name("§7" + gameProfile.getName()).lore(
                                "§7Erster Join: §6" + sdf.format(firstlogin),
                                plugin.getLocaleManager().translate(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "profiles_members_playtime",
                                        DateUtilities.getTime(0, playtime, plugin, language))), inventoryClickEvent -> {
                            SimpleInventory simpleInventory = InventoryBuilder.create(3*9, "§7" + gameProfile.getName() + "'s Profile");

                            ResultSet profiles = plugin.getDatabaseAdapter().query("SELECT DISTINCT * FROM playerProfiles, profiles, profileStats, playerStats WHERE " +
                                    "playerProfiles.player = '" + gameProfile.getId() + "' AND profiles.profileId = playerProfiles.profileId" +
                                    " AND playerProfiles.player = playerStats.player AND profiles.profileId = playerStats.profileId" +
                                    " AND profiles.profileId = profileStats.profileId");

                            try {
                                while(profiles.next()) {
                                    List<String> lore = new ArrayList<>();
                                    String profileId = profiles.getString("profileId");
                                    lore.add("§7Erstellt am: §6" + sdf.format(profiles.getLong("start")));
                                    lore.add("§7Letze Aktivität des Profiles: §6" + sdf.format(profiles.getLong("lastActivity")));
                                    lore.add("§7Spielzeit des Spielers: §6" + DateUtilities.getTime(0, profiles.getLong("playtime"), plugin, language));
                                    lore.add("§7Tode: §6" + profiles.getInt("deaths"));
                                    lore.add("§7Kills: §6" + profiles.getInt("kills"));
                                    lore.add("§7Gewonnene Raids: §6" + profiles.getInt("wonRaids"));
                                    lore.add("§7Verlorene Raids: §6" + profiles.getInt("lostRaids"));
                                    lore.add(" ");
                                    lore.add("§7§lQuests:");

                                    ResultSet quests = plugin.getDatabaseAdapter().get("quests", "profileId", profileId);
                                    try {
                                        while(quests.next()) {
                                            if(quests.getLong("finished") == 0) {
                                                lore.add("§7- " + quests.getString("task") + " §6(" + quests.getInt("progress") + ")");
                                            }
                                        }
                                    } catch (SQLException sqlException) {
                                        sqlException.printStackTrace();
                                    }

                                    lore.add(" ");
                                    lore.add("§7§lGekaufte Items:");
                                    ResultSet boughtItems = plugin.getDatabaseAdapter().get("boughtItems", "profileId", profileId);
                                    try {
                                        while(boughtItems.next()) {
                                            lore.add("§7- " + boughtItems.getInt("amount") + "x §6" + boughtItems.getString("item"));
                                        }
                                    } catch (SQLException sqlException) {
                                        sqlException.printStackTrace();
                                    }

                                    simpleInventory.addItem(ItemBuilder.create(Material.GOLDEN_PICKAXE)
                                            .name("§eProfil #" + profiles.getInt("slot"))
                                            .lore(lore));
                                }
                            } catch (SQLException sqlException) {

                            }

                            simpleInventory.open(player);
                        });
                    }
                } catch (SQLException exception) {
                    player.sendMessage("§cAn error occurred processing your request");
                }

                paginatedInventory.open(player);
            }
        }

        return false;
    }
}
