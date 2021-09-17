package me.luxtix.haybale.features.modules.movement;

import me.luxtix.haybale.features.modules.Module;
import me.luxtix.haybale.features.setting.Setting;
import me.luxtix.haybale.util.EntityUtil2;
import me.luxtix.haybale.util.PlayerUtil2;
import me.luxtix.haybale.util.Timer;

public class Yport extends Module {
    public Yport() {
        super("Yport", "YPort Speed.", Category.MOVEMENT, false, false, false);
    }

    Setting<Double> yPortSpeed = this.register(new Setting<Double>("YPort Speed", 0.6, 0.5, 1.5));

    private double playerSpeed;
    private final Timer timer = new Timer();

    @Override
    public void onEnable() {
        playerSpeed = PlayerUtil2.getBaseMoveSpeed();
    }

    @Override
    public void onDisable() {
        EntityUtil2.resetTimer();
        this.timer.reset();
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) {
            this.disable();
            return;
        }

        if (!PlayerUtil2.isMoving(mc.player) || mc.player.isInWater() && mc.player.isInLava() || mc.player.collidedHorizontally) {
            return;
        }
        if (mc.player.onGround) {
            EntityUtil2.setTimer(1.15f);
            mc.player.jump();
            PlayerUtil2.setSpeed(mc.player, PlayerUtil2.getBaseMoveSpeed() + yPortSpeed.getValue() / 10);
        } else {
            mc.player.motionY = -1;
            EntityUtil2.resetTimer();
        }
    }
}