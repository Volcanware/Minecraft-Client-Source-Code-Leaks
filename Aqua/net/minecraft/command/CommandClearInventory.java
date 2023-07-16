package net.minecraft.command;

import java.util.Collection;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class CommandClearInventory
extends CommandBase {
    public String getCommandName() {
        return "clear";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "commands.clear.usage";
    }

    public int getRequiredPermissionLevel() {
        return 2;
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP entityplayermp = args.length == 0 ? CommandClearInventory.getCommandSenderAsPlayer((ICommandSender)sender) : CommandClearInventory.getPlayer((ICommandSender)sender, (String)args[0]);
        Item item = args.length >= 2 ? CommandClearInventory.getItemByText((ICommandSender)sender, (String)args[1]) : null;
        int i = args.length >= 3 ? CommandClearInventory.parseInt((String)args[2], (int)-1) : -1;
        int j = args.length >= 4 ? CommandClearInventory.parseInt((String)args[3], (int)-1) : -1;
        NBTTagCompound nbttagcompound = null;
        if (args.length >= 5) {
            try {
                nbttagcompound = JsonToNBT.getTagFromJson((String)CommandClearInventory.buildString((String[])args, (int)4));
            }
            catch (NBTException nbtexception) {
                throw new CommandException("commands.clear.tagError", new Object[]{nbtexception.getMessage()});
            }
        }
        if (args.length >= 2 && item == null) {
            throw new CommandException("commands.clear.failure", new Object[]{entityplayermp.getName()});
        }
        int k = entityplayermp.inventory.clearMatchingItems(item, i, j, nbttagcompound);
        entityplayermp.inventoryContainer.detectAndSendChanges();
        if (!entityplayermp.capabilities.isCreativeMode) {
            entityplayermp.updateHeldItem();
        }
        sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, k);
        if (k == 0) {
            throw new CommandException("commands.clear.failure", new Object[]{entityplayermp.getName()});
        }
        if (j == 0) {
            sender.addChatMessage((IChatComponent)new ChatComponentTranslation("commands.clear.testing", new Object[]{entityplayermp.getName(), k}));
        } else {
            CommandClearInventory.notifyOperators((ICommandSender)sender, (ICommand)this, (String)"commands.clear.success", (Object[])new Object[]{entityplayermp.getName(), k});
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? CommandClearInventory.getListOfStringsMatchingLastWord((String[])args, (String[])this.func_147209_d()) : (args.length == 2 ? CommandClearInventory.getListOfStringsMatchingLastWord((String[])args, (Collection)Item.itemRegistry.getKeys()) : null);
    }

    protected String[] func_147209_d() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }
}
