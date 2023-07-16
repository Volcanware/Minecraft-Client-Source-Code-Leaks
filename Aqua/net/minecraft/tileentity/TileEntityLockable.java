package net.minecraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public abstract class TileEntityLockable
extends TileEntity
implements IInteractionObject,
ILockableContainer {
    private LockCode code = LockCode.EMPTY_CODE;

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.code = LockCode.fromNBT((NBTTagCompound)compound);
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.code != null) {
            this.code.toNBT(compound);
        }
    }

    public boolean isLocked() {
        return this.code != null && !this.code.isEmpty();
    }

    public LockCode getLockCode() {
        return this.code;
    }

    public void setLockCode(LockCode code) {
        this.code = code;
    }

    public IChatComponent getDisplayName() {
        return this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatComponentTranslation(this.getName(), new Object[0]);
    }
}
