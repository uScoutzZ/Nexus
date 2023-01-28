package de.uscoutz.nexus.utilities;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LocaleManager {

    private NexusPlugin plugin;
    private Map<String, File> localFiles;
    private List<File> files;
    @Getter
    private List<String> languageKeys;

    public LocaleManager(NexusPlugin plugin) {
        this.plugin = plugin;
        this.localFiles = new HashMap<>();
        this.files = new ArrayList<>();
        languageKeys = new ArrayList<>();
    }

    public void assignFiles(File path) {
        File[] files = path.listFiles();

        if(files == null) return;

        for (File file : files) {
            this.localFiles.put(file.getName(), file);
            languageKeys.add(file.getName().split("\\.")[0]);
            this.files.add(file);
        }
    }

    public String translate(String languageKey, String translationKey, Object... variables) {
        File file;
        if(localFiles.containsKey(languageKey + ".properties")) {
            file = localFiles.get(languageKey + ".properties");
        } else {
            file = localFiles.get("en_US.properties");
        }

        if (file == null) {
            return "N/A";
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            Properties propertie = new Properties();
            propertie.load(new InputStreamReader(fileInputStream, UTF_8));

            if(propertie.getProperty(translationKey) == null) {
                return "N/A";
            }
            String message = propertie.getProperty(translationKey);

            if(message.contains("%prefix%")) {
                message = message.replace("%prefix%", translate(languageKey, "prefix"));
            }

            message = message.replace(" Nexus", plugin.getConfig().getString("nexus-color") + " Nexus");
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String color = matcher.group();
                //message = message.replace(color, TextColor.fromHexString(color) +"");
                message = message.replace(color, ChatColor.of(color) + "");
                matcher = pattern.matcher(message);
            }

            message = ChatColor.translateAlternateColorCodes('&', message);

            int i = 0;
            for(Object argument : variables) {
                try {
                    int number = Integer.parseInt(argument.toString());
                    NumberFormat anotherFormat;
                    if(languageKey.equals("de_DE")) {
                        anotherFormat = NumberFormat.getNumberInstance(Locale.GERMAN);
                    } else if(languageKey.equals("fr_FR")) {
                        anotherFormat = NumberFormat.getNumberInstance(Locale.FRENCH);
                    } else {
                        anotherFormat = NumberFormat.getNumberInstance(Locale.US);
                    }

                    if(anotherFormat instanceof DecimalFormat) {
                        DecimalFormat anotherDFormat = (DecimalFormat) anotherFormat;
                        anotherDFormat.setGroupingUsed(true);
                        anotherDFormat.setGroupingSize(3);
                        variables[i] = anotherDFormat.format(number);
                    }
                } catch (Exception ignored) {
                }
                message = message.replace("{" + i + "}", String.valueOf(variables[i]));
                i++;
            }
            return message;
        } catch (IOException e) {
            return "N/A";
        }
    }

    public List<String> split(String message) {
        List<String> messages = new ArrayList<>();

        String prefix;
        if(message.startsWith(plugin.getConfig().getString("villager-name") + "§7: ")) {
            prefix = plugin.getConfig().getString("villager-name") + "§7: §f";
            messages.addAll(List.of(message.replace(prefix, "").split("\\. ")));
            for(String string : messages) {
                messages.set(messages.indexOf(string), prefix + string);
            }
        } else {
            prefix = "\n";
        }

        return messages;
    }
}
