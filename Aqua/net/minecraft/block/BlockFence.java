package net.minecraft.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemLead;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFence
extends Block {
    public static final PropertyBool NORTH = PropertyBool.create((String)"north");
    public static final PropertyBool EAST = PropertyBool.create((String)"east");
    public static final PropertyBool SOUTH = PropertyBool.create((String)"south");
    public static final PropertyBool WEST = PropertyBool.create((String)"west");

    public BlockFence(Material materialIn) {
        this(materialIn, materialIn.getMaterialMapColor());
    }

    public BlockFence(Material p_i46395_1_, MapColor p_i46395_2_) {
        super(p_i46395_1_, p_i46395_2_);
        this.setDefaultState(this.blockState.getBaseState().withProperty((IProperty)NORTH, (Comparable)Boolean.valueOf((boolean)false)).withProperty((IProperty)EAST, (Comparable)Boolean.valueOf((boolean)false)).withProperty((IProperty)SOUTH, (Comparable)Boolean.valueOf((boolean)false)).withProperty((IProperty)WEST, (Comparable)Boolean.valueOf((boolean)false)));
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        boolean flag = this.canConnectTo((IBlockAccess)worldIn, pos.north());
        boolean flag1 = this.canConnectTo((IBlockAccess)worldIn, pos.south());
        boolean flag2 = this.canConnectTo((IBlockAccess)worldIn, pos.west());
        boolean flag3 = this.canConnectTo((IBlockAccess)worldIn, pos.east());
        float f = 0.375f;
        float f1 = 0.625f;
        float f2 = 0.375f;
        float f3 = 0.625f;
        if (flag) {
            f2 = 0.0f;
        }
        if (flag1) {
            f3 = 1.0f;
        }
        if (flag || flag1) {
            this.setBlockBounds(f, 0.0f, f2, f1, 1.5f, f3);
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }
        f2 = 0.375f;
        f3 = 0.625f;
        if (flag2) {
            f = 0.0f;
        }
        if (flag3) {
            f1 = 1.0f;
        }
        if (flag2 || flag3 || !flag && !flag1) {
            this.setBlockBounds(f, 0.0f, f2, f1, 1.5f, f3);
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }
        if (flag) {
            f2 = 0.0f;
        }
        if (flag1) {
            f3 = 1.0f;
        }
        this.setBlockBounds(f, 0.0f, f2, f1, 1.0f, f3);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        boolean flag = this.canConnectTo(worldIn, pos.north());
        boolean flag1 = this.canConnectTo(worldIn, pos.south());
        boolean flag2 = this.canConnectTo(worldIn, pos.west());
        boolean flag3 = this.canConnectTo(worldIn, pos.east());
        float f = 0.375f;
        float f1 = 0.625f;
        float f2 = 0.375f;
        float f3 = 0.625f;
        if (flag) {
            f2 = 0.0f;
        }
        if (flag1) {
            f3 = 1.0f;
        }
        if (flag2) {
            f = 0.0f;
        }
        if (flag3) {
            f1 = 1.0f;
        }
        this.setBlockBounds(f, 0.0f, f2, f1, 1.0f, f3);
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos).getBlock();
        return block == Blocks.barrier ? false : (!(block instanceof BlockFence && block.blockMaterial == this.blockMaterial || block instanceof BlockFenceGate) ? (block.blockMaterial.isOpaque() && block.isFullCube() ? block.blockMaterial != Material.gourd : false) : true);
    }

    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        return worldIn.isRemote ? true : ItemLead.attachToFence((EntityPlayer)playerIn, (World)worldIn, (BlockPos)pos);
    }

    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty((IProperty)NORTH, (Comparable)Boolean.valueOf((boolean)this.canConnectTo(worldIn, pos.north()))).withProperty((IProperty)EAST, (Comparable)Boolean.valueOf((boolean)this.canConnectTo(worldIn, pos.east()))).withProperty((IProperty)SOUTH, (Comparable)Boolean.valueOf((boolean)this.canConnectTo(worldIn, pos.south()))).withProperty((IProperty)WEST, (Comparable)Boolean.valueOf((boolean)this.canConnectTo(worldIn, pos.west())));
    }

    protected BlockState createBlockState() {
        return new BlockState((Block)this, new IProperty[]{NORTH, EAST, WEST, SOUTH});
    }
}
