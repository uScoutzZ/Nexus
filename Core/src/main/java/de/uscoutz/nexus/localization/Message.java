package de.uscoutz.nexus.localization;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Message {

    private NexusPlugin plugin;
    private Map<String, String> messages;

    public Message(NexusPlugin plugin) {
        this.plugin = plugin;
        messages = new HashMap<>();
        InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream("de_DE.yml");
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        br.lines().forEach(line -> messages.put(line.split(": ")[0], line.split(": ")[1]));
    }

    public String get(String key, String... args) {

        String message = messages.get(key);

        if(message != null) {
            if(message.contains("%prefix%")) {
                message = message.replace("%prefix%", get("prefix"));
            }

            int i = 0;
            for(Object argument : args) {
                message = message.replace("{" + i + "}", String.valueOf(args[i]));
                i++;
            }
        } else {
            message = "N/A (" + key + ")";
        }
        return message;
    }
}
