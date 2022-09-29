package me.luxtix.haybale.features.modules.misc;

import me.luxtix.haybale.DiscordPresence;
import me.luxtix.haybale.features.modules.Module;
import me.luxtix.haybale.features.setting.Setting;

public class RPC
        extends Module {
    public static RPC INSTANCE;
    public Setting<Boolean> showIP = this.register(new Setting<Boolean>("ShowIP", Boolean.valueOf(true), "Shows the server IP in your discord presence."));
    public Setting<String> state = this.register(new Setting<String>("State", "Winning with HayBale Reborn 1.0.1", "Sets the state of the DiscordRPC."));

    public RPC() {
        super("RPC", "Discord rich presence", Module.Category.MISC, false, false, false);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        DiscordPresence.start();
    }

    @Override
    public void onDisable() {
        DiscordPresence.stop();
    }
}
