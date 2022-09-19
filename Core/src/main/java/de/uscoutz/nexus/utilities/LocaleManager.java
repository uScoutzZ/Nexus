package de.uscoutz.nexus.utilities;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;

import java.io.*;
import java.util.*;

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
                message = message.replace("%prefix%", translate("de_DE", "prefix"));
            }

            int i = 0;
            for(Object argument : variables) {
                message = message.replace("{" + i + "}", String.valueOf(variables[i]));
                i++;
            }
            return message;
        } catch (IOException e) {
            return "N/A";
        }
    }

    public void assignLocal(String local) {
        this.files.forEach(propFile -> {
            if(propFile.getName().endsWith(".properties")) {
                Properties properties = new Properties();
                try (FileInputStream inputStream = new FileInputStream(propFile)){
                    properties.load(new InputStreamReader(inputStream, UTF_8));

                    properties.setProperty(local, "§c" + local + " (translation not found)");

                    try(FileOutputStream outputStream = new FileOutputStream(propFile)) {
                        properties.store(new OutputStreamWriter(outputStream, UTF_8), "Edited by Felix");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
