package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CommandSaveOn
extends CommandBase {
    public String getCommandName() {
        return "save-on";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "commands.save-on.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        boolean flag = false;
        for (int i = 0; i < minecraftserver.worldServers.length; ++i) {
            if (minecraftserver.worldServers[i] == null) continue;
            WorldServer worldserver = minecraftserver.worldServers[i];
            if (!worldserver.disableLevelSaving) continue;
            worldserver.disableLevelSaving = false;
            flag = true;
        }
        if (!flag) {
            throw new CommandException("commands.save-on.alreadyOn", new Object[0]);
        }
        CommandSaveOn.notifyOperators((ICommandSender)sender, (ICommand)this, (String)"commands.save.enabled", (Object[])new Object[0]);
    }
}
