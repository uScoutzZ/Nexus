package de.uscoutz.nexus.broadcasts;

import lombok.Getter;

public class BroadcastMessage {

    @Getter
    private String localeKey;

    public BroadcastMessage(String localeKey) {
        this.localeKey = localeKey;
    }
}
