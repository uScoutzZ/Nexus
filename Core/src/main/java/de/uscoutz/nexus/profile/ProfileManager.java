package de.uscoutz.nexus.profile;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProfileManager {

    private NexusPlugin plugin;

    @Getter
    private Map<UUID, Profile> profilesMap;

    public ProfileManager(NexusPlugin plugin) {
        this.plugin = plugin;
        profilesMap = new HashMap<>();
    }
}
