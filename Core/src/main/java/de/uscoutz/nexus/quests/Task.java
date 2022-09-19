package de.uscoutz.nexus.quests;

import lombok.Getter;
import lombok.Setter;

public enum Task {

    TALK_TO_GEORGE(true),
    COLLECT_LOG(false, 4);

    private Task(boolean chronological) {
        this.chronological = chronological;
    }

    Task(boolean chronological, long goal) {
        this.chronological = chronological;
        this.goal = goal;
    }

    @Getter @Setter
    private boolean chronological;
    @Getter @Setter
    private String text;
    @Getter
    private long goal;
    private static Task[] vals = values();

    public Task next() {
        return vals[(this.ordinal()+1) % vals.length];
    }
}
