package net.minecraft.tileentity;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityBrewingStand
extends TileEntityLockable
implements ITickable,
ISidedInventory {
    private static final int[] inputSlots = new int[]{3};
    private static final int[] outputSlots = new int[]{0, 1, 2};
    private ItemStack[] brewingItemStacks = new ItemStack[4];
    private int brewTime;
    private boolean[] filledSlots;
    private Item ingredientID;
    private String customName;

    public String getName() {
        return this.hasCustomName() ? this.customName : "container.brewing";
    }

    public boolean hasCustomName() {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setName(String name) {
        this.customName = name;
    }

    public int getSizeInventory() {
        return this.brewingItemStacks.length;
    }

    public void update() {
        boolean[] aboolean;
        if (this.brewTime > 0) {
            --this.brewTime;
            if (this.brewTime == 0) {
                this.brewPotions();
                this.markDirty();
            } else if (!this.canBrew()) {
                this.brewTime = 0;
                this.markDirty();
            } else if (this.ingredientID != this.brewingItemStacks[3].getItem()) {
                this.brewTime = 0;
                this.markDirty();
            }
        } else if (this.canBrew()) {
            this.brewTime = 400;
            this.ingredientID = this.brewingItemStacks[3].getItem();
        }
        if (!this.worldObj.isRemote && !Arrays.equals((boolean[])(aboolean = this.func_174902_m()), (boolean[])this.filledSlots)) {
            this.filledSlots = aboolean;
            IBlockState iblockstate = this.worldObj.getBlockState(this.getPos());
            if (!(iblockstate.getBlock() instanceof BlockBrewingStand)) {
                return;
            }
            for (int i = 0; i < BlockBrewingStand.HAS_BOTTLE.length; ++i) {
                iblockstate = iblockstate.withProperty((IProperty)BlockBrewingStand.HAS_BOTTLE[i], (Comparable)Boolean.valueOf((boolean)aboolean[i]));
            }
            this.worldObj.setBlockState(this.pos, iblockstate, 2);
        }
    }

    private boolean canBrew() {
        if (this.brewingItemStacks[3] != null && this.brewingItemStacks[3].stackSize > 0) {
            ItemStack itemstack = this.brewingItemStacks[3];
            if (!itemstack.getItem().isPotionIngredient(itemstack)) {
                return false;
            }
            boolean flag = false;
            for (int i = 0; i < 3; ++i) {
                if (this.brewingItemStacks[i] == null || this.brewingItemStacks[i].getItem() != Items.potionitem) continue;
                int j = this.brewingItemStacks[i].getMetadata();
                int k = this.getPotionResult(j, itemstack);
                if (!ItemPotion.isSplash((int)j) && ItemPotion.isSplash((int)k)) {
                    flag = true;
                    break;
                }
                List list = Items.potionitem.getEffects(j);
                List list1 = Items.potionitem.getEffects(k);
                if (j > 0 && list == list1 || list != null && (list.equals((Object)list1) || list1 == null) || j == k) continue;
                flag = true;
                break;
            }
            return flag;
        }
        return false;
    }

    private void brewPotions() {
        if (this.canBrew()) {
            ItemStack itemstack = this.brewingItemStacks[3];
            for (int i = 0; i < 3; ++i) {
                if (this.brewingItemStacks[i] == null || this.brewingItemStacks[i].getItem() != Items.potionitem) continue;
                int j = this.brewingItemStacks[i].getMetadata();
                int k = this.getPotionResult(j, itemstack);
                List list = Items.potionitem.getEffects(j);
                List list1 = Items.potionitem.getEffects(k);
                if (j > 0 && list == list1 || list != null && (list.equals((Object)list1) || list1 == null)) {
                    if (ItemPotion.isSplash((int)j) || !ItemPotion.isSplash((int)k)) continue;
                    this.brewingItemStacks[i].setItemDamage(k);
                    continue;
                }
                if (j == k) continue;
                this.brewingItemStacks[i].setItemDamage(k);
            }
            if (itemstack.getItem().hasContainerItem()) {
                this.brewingItemStacks[3] = new ItemStack(itemstack.getItem().getContainerItem());
            } else {
                --this.brewingItemStacks[3].stackSize;
                if (this.brewingItemStacks[3].stackSize <= 0) {
                    this.brewingItemStacks[3] = null;
                }
            }
        }
    }

    private int getPotionResult(int meta, ItemStack stack) {
        return stack == null ? meta : (stack.getItem().isPotionIngredient(stack) ? PotionHelper.applyIngredient((int)meta, (String)stack.getItem().getPotionEffect(stack)) : meta);
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        this.brewingItemStacks = new ItemStack[this.getSizeInventory()];
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            byte j = nbttagcompound.getByte("Slot");
            if (j < 0 || j >= this.brewingItemStacks.length) continue;
            this.brewingItemStacks[j] = ItemStack.loadItemStackFromNBT((NBTTagCompound)nbttagcompound);
        }
        this.brewTime = compound.getShort("BrewTime");
        if (compound.hasKey("CustomName", 8)) {
            this.customName = compound.getString("CustomName");
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setShort("BrewTime", (short)this.brewTime);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.brewingItemStacks.length; ++i) {
            if (this.brewingItemStacks[i] == null) continue;
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.brewingItemStacks[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag((NBTBase)nbttagcompound);
        }
        compound.setTag("Items", (NBTBase)nbttaglist);
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
    }

    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.brewingItemStacks.length ? this.brewingItemStacks[index] : null;
    }

    public ItemStack decrStackSize(int index, int count) {
        if (index >= 0 && index < this.brewingItemStacks.length) {
            ItemStack itemstack = this.brewingItemStacks[index];
            this.brewingItemStacks[index] = null;
            return itemstack;
        }
        return null;
    }

    public ItemStack removeStackFromSlot(int index) {
        if (index >= 0 && index < this.brewingItemStacks.length) {
            ItemStack itemstack = this.brewingItemStacks[index];
            this.brewingItemStacks[index] = null;
            return itemstack;
        }
        return null;
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= 0 && index < this.brewingItemStacks.length) {
            this.brewingItemStacks[index] = stack;
        }
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.5, (double)this.pos.getZ() + 0.5) <= 64.0;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 3 ? stack.getItem().isPotionIngredient(stack) : stack.getItem() == Items.potionitem || stack.getItem() == Items.glass_bottle;
    }

    public boolean[] func_174902_m() {
        boolean[] aboolean = new boolean[3];
        for (int i = 0; i < 3; ++i) {
            if (this.brewingItemStacks[i] == null) continue;
            aboolean[i] = true;
        }
        return aboolean;
    }

    public int[] getSlotsForFace(EnumFacing side) {
        return side == EnumFacing.UP ? inputSlots : outputSlots;
    }

    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    public String getGuiID() {
        return "minecraft:brewing_stand";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerBrewingStand(playerInventory, (IInventory)this);
    }

    public int getField(int id) {
        switch (id) {
            case 0: {
                return this.brewTime;
            }
        }
        return 0;
    }

    public void setField(int id, int value) {
        switch (id) {
            case 0: {
                this.brewTime = value;
            }
        }
    }

    public int getFieldCount() {
        return 1;
    }

    public void clear() {
        for (int i = 0; i < this.brewingItemStacks.length; ++i) {
            this.brewingItemStacks[i] = null;
        }
    }
}
