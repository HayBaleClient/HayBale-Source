package me.luxtix.haybale.event.events;

import me.luxtix.haybale.event.EventStage;
import net.minecraft.entity.player.EntityPlayer;

public class DeathEvent
        extends EventStage {
    public EntityPlayer player;

    public DeathEvent(EntityPlayer player) {
        this.player = player;
    }
}

