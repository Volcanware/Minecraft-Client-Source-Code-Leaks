package net.minecraft.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public class RecipesTools {
    private String[][] recipePatterns = new String[][]{{"XXX", " # ", " # "}, {"X", "#", "#"}, {"XX", "X#", " #"}, {"XX", " #", " #"}};
    private Object[][] recipeItems = new Object[][]{{Blocks.planks, Blocks.cobblestone, Items.iron_ingot, Items.diamond, Items.gold_ingot}, {Items.wooden_pickaxe, Items.stone_pickaxe, Items.iron_pickaxe, Items.diamond_pickaxe, Items.golden_pickaxe}, {Items.wooden_shovel, Items.stone_shovel, Items.iron_shovel, Items.diamond_shovel, Items.golden_shovel}, {Items.wooden_axe, Items.stone_axe, Items.iron_axe, Items.diamond_axe, Items.golden_axe}, {Items.wooden_hoe, Items.stone_hoe, Items.iron_hoe, Items.diamond_hoe, Items.golden_hoe}};

    public void addRecipes(CraftingManager p_77586_1_) {
        for (int i = 0; i < this.recipeItems[0].length; ++i) {
            Object object = this.recipeItems[0][i];
            for (int j = 0; j < this.recipeItems.length - 1; ++j) {
                Item item = (Item)this.recipeItems[j + 1][i];
                p_77586_1_.addRecipe(new ItemStack(item), new Object[]{this.recipePatterns[j], Character.valueOf((char)'#'), Items.stick, Character.valueOf((char)'X'), object});
            }
        }
        p_77586_1_.addRecipe(new ItemStack((Item)Items.shears), new Object[]{" #", "# ", Character.valueOf((char)'#'), Items.iron_ingot});
    }
}
