package me.luxtix.haybale.features.command.commands;

import me.luxtix.haybale.Phobos;
import me.luxtix.haybale.features.command.Command;

public class ReloadCommand
        extends Command {
    public ReloadCommand() {
        super("reload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Phobos.reload();
    }
}

