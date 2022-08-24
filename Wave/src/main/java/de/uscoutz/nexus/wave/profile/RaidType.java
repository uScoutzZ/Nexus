package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.wave.NexusWavePlugin;
import lombok.Getter;

public class RaidType implements Cloneable {

    private NexusWavePlugin plugin;

    @Getter
    private int nexusLevel;

    public RaidType(int nexusLevel, NexusWavePlugin plugin) {
        this.plugin = plugin;
        this.nexusLevel = nexusLevel;
    }
}
