package net.minecraft.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/*
 * Exception performing whole class analysis ignored.
 */
public class BlockRedSandstone
extends Block {
    public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create((String)"type", EnumType.class);

    public BlockRedSandstone() {
        super(Material.rock, BlockSand.EnumType.RED_SAND.getMapColor());
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, (Comparable)EnumType.DEFAULT));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public int damageDropped(IBlockState state) {
        return ((EnumType)state.getValue(TYPE)).getMetadata();
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (EnumType blockredsandstone$enumtype : EnumType.values()) {
            list.add((Object)new ItemStack(itemIn, 1, blockredsandstone$enumtype.getMetadata()));
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, (Comparable)EnumType.byMetadata((int)meta));
    }

    public int getMetaFromState(IBlockState state) {
        return ((EnumType)state.getValue(TYPE)).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState((Block)this, new IProperty[]{TYPE});
    }
}
