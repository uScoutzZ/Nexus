package de.uscoutz.nexus.coop;

import lombok.Getter;

import java.util.UUID;

public class CoopInvitation {

    @Getter
    private UUID receiver, profileId;
    @Getter
    private String sender;

    public CoopInvitation(String sender, UUID receiver, UUID profileId) {
        this.sender = sender;
        this.receiver = receiver;
        this.profileId = profileId;
    }
}
