package me.luxtix.haybale.event.events;

import me.luxtix.haybale.event.EventStage;

import javax.sound.midi.ShortMessage;

public class TickEvent extends EventStage {
    public static ShortMessage Phase;
    private final int stage;

    public TickEvent(int stage) {
        this.stage = stage;
    }

    public final int getStage() {
        return this.stage;
    }
}