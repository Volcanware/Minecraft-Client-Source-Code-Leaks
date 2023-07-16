package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class ShapelessRecipes
implements IRecipe {
    private final ItemStack recipeOutput;
    private final List<ItemStack> recipeItems;

    public ShapelessRecipes(ItemStack output, List<ItemStack> inputList) {
        this.recipeOutput = output;
        this.recipeItems = inputList;
    }

    public ItemStack getRecipeOutput() {
        return this.recipeOutput;
    }

    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];
        for (int i = 0; i < aitemstack.length; ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (itemstack == null || !itemstack.getItem().hasContainerItem()) continue;
            aitemstack[i] = new ItemStack(itemstack.getItem().getContainerItem());
        }
        return aitemstack;
    }

    public boolean matches(InventoryCrafting inv, World worldIn) {
        ArrayList list = Lists.newArrayList(this.recipeItems);
        for (int i = 0; i < inv.getHeight(); ++i) {
            for (int j = 0; j < inv.getWidth(); ++j) {
                ItemStack itemstack = inv.getStackInRowAndColumn(j, i);
                if (itemstack == null) continue;
                boolean flag = false;
                for (ItemStack itemstack1 : list) {
                    if (itemstack.getItem() != itemstack1.getItem() || itemstack1.getMetadata() != Short.MAX_VALUE && itemstack.getMetadata() != itemstack1.getMetadata()) continue;
                    flag = true;
                    list.remove((Object)itemstack1);
                    break;
                }
                if (flag) continue;
                return false;
            }
        }
        return list.isEmpty();
    }

    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return this.recipeOutput.copy();
    }

    public int getRecipeSize() {
        return this.recipeItems.size();
    }
}
