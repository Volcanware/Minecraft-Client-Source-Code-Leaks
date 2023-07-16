package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSettings;

public class CommandPublishLocalServer
extends CommandBase {
    public String getCommandName() {
        return "publish";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "commands.publish.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String s = MinecraftServer.getServer().shareToLAN(WorldSettings.GameType.SURVIVAL, false);
        if (s != null) {
            CommandPublishLocalServer.notifyOperators((ICommandSender)sender, (ICommand)this, (String)"commands.publish.started", (Object[])new Object[]{s});
        } else {
            CommandPublishLocalServer.notifyOperators((ICommandSender)sender, (ICommand)this, (String)"commands.publish.failed", (Object[])new Object[0]);
        }
    }
}
