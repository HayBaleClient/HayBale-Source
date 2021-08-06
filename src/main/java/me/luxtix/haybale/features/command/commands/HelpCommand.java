package me.luxtix.haybale.features.command.commands;

import me.luxtix.haybale.Phobos;
import me.luxtix.haybale.features.command.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("commands");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("You can use following commands: ");
        for (Command command : Phobos.commandManager.getCommands()) {
            HelpCommand.sendMessage(Phobos.commandManager.getPrefix() + command.getName());
        }
    }
}

