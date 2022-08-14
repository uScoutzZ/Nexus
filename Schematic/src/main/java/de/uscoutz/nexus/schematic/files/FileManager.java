package de.uscoutz.nexus.schematic.files;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private File nexusFolder;
    @Getter
    private File schematicFolder;
    @Getter
    private Map<SchematicType, File> schematicFilesMap;

    public FileManager(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        schematicFilesMap = new HashMap<>();
        nexusFolder = new File("/home/networksync/nexus");
        schematicFolder = new File("/home/networksync/nexus/schematics/");
        schematicFolder.mkdirs();
    }

    public void loadSchematicFiles() {
        for(SchematicType schematicType : SchematicType.values()) {
            File typeFile = new File(schematicFolder.getAbsolutePath() + "/" + schematicType.toString().toLowerCase() + ".yml");
            if(!typeFile.exists()) {
                try {
                    typeFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            schematicFilesMap.put(schematicType, typeFile);
            schematicType.loadFile();
        }
    }
}
