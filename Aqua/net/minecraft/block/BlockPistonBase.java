package net.minecraft.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPistonBase
extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create((String)"facing");
    public static final PropertyBool EXTENDED = PropertyBool.create((String)"extended");
    private final boolean isSticky;

    public BlockPistonBase(boolean isSticky) {
        super(Material.piston);
        this.setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH).withProperty((IProperty)EXTENDED, (Comparable)Boolean.valueOf((boolean)false)));
        this.isSticky = isSticky;
        this.setStepSound(soundTypePiston);
        this.setHardness(0.5f);
        this.setCreativeTab(CreativeTabs.tabRedstone);
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty((IProperty)FACING, (Comparable)BlockPistonBase.getFacingFromEntity(worldIn, pos, placer)), 2);
        if (!worldIn.isRemote) {
            this.checkForMove(worldIn, pos, state);
        }
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (!worldIn.isRemote) {
            this.checkForMove(worldIn, pos, state);
        }
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote && worldIn.getTileEntity(pos) == null) {
            this.checkForMove(worldIn, pos, state);
        }
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty((IProperty)FACING, (Comparable)BlockPistonBase.getFacingFromEntity(worldIn, pos, placer)).withProperty((IProperty)EXTENDED, (Comparable)Boolean.valueOf((boolean)false));
    }

    private void checkForMove(World worldIn, BlockPos pos, IBlockState state) {
        EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
        boolean flag = this.shouldBeExtended(worldIn, pos, enumfacing);
        if (flag && !((Boolean)state.getValue((IProperty)EXTENDED)).booleanValue()) {
            if (new BlockPistonStructureHelper(worldIn, pos, enumfacing, true).canMove()) {
                worldIn.addBlockEvent(pos, (Block)this, 0, enumfacing.getIndex());
            }
        } else if (!flag && ((Boolean)state.getValue((IProperty)EXTENDED)).booleanValue()) {
            worldIn.setBlockState(pos, state.withProperty((IProperty)EXTENDED, (Comparable)Boolean.valueOf((boolean)false)), 2);
            worldIn.addBlockEvent(pos, (Block)this, 1, enumfacing.getIndex());
        }
    }

    private boolean shouldBeExtended(World worldIn, BlockPos pos, EnumFacing facing) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (enumfacing == facing || !worldIn.isSidePowered(pos.offset(enumfacing), enumfacing)) continue;
            return true;
        }
        if (worldIn.isSidePowered(pos, EnumFacing.DOWN)) {
            return true;
        }
        BlockPos blockpos = pos.up();
        for (EnumFacing enumfacing1 : EnumFacing.values()) {
            if (enumfacing1 == EnumFacing.DOWN || !worldIn.isSidePowered(blockpos.offset(enumfacing1), enumfacing1)) continue;
            return true;
        }
        return false;
    }

    public boolean onBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
        if (!worldIn.isRemote) {
            boolean flag = this.shouldBeExtended(worldIn, pos, enumfacing);
            if (flag && eventID == 1) {
                worldIn.setBlockState(pos, state.withProperty((IProperty)EXTENDED, (Comparable)Boolean.valueOf((boolean)true)), 2);
                return false;
            }
            if (!flag && eventID == 0) {
                return false;
            }
        }
        if (eventID == 0) {
            if (!this.doMove(worldIn, pos, enumfacing, true)) {
                return false;
            }
            worldIn.setBlockState(pos, state.withProperty((IProperty)EXTENDED, (Comparable)Boolean.valueOf((boolean)true)), 2);
            worldIn.playSoundEffect((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, "tile.piston.out", 0.5f, worldIn.rand.nextFloat() * 0.25f + 0.6f);
        } else if (eventID == 1) {
            TileEntity tileentity1 = worldIn.getTileEntity(pos.offset(enumfacing));
            if (tileentity1 instanceof TileEntityPiston) {
                ((TileEntityPiston)tileentity1).clearPistonTileEntity();
            }
            worldIn.setBlockState(pos, Blocks.piston_extension.getDefaultState().withProperty((IProperty)BlockPistonMoving.FACING, (Comparable)enumfacing).withProperty((IProperty)BlockPistonMoving.TYPE, (Comparable)(this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT)), 3);
            worldIn.setTileEntity(pos, BlockPistonMoving.newTileEntity((IBlockState)this.getStateFromMeta(eventParam), (EnumFacing)enumfacing, (boolean)false, (boolean)true));
            if (this.isSticky) {
                TileEntityPiston tileentitypiston;
                TileEntity tileentity;
                BlockPos blockpos = pos.add(enumfacing.getFrontOffsetX() * 2, enumfacing.getFrontOffsetY() * 2, enumfacing.getFrontOffsetZ() * 2);
                Block block = worldIn.getBlockState(blockpos).getBlock();
                boolean flag1 = false;
                if (block == Blocks.piston_extension && (tileentity = worldIn.getTileEntity(blockpos)) instanceof TileEntityPiston && (tileentitypiston = (TileEntityPiston)tileentity).getFacing() == enumfacing && tileentitypiston.isExtending()) {
                    tileentitypiston.clearPistonTileEntity();
                    flag1 = true;
                }
                if (!flag1 && block.getMaterial() != Material.air && BlockPistonBase.canPush(block, worldIn, blockpos, enumfacing.getOpposite(), false) && (block.getMobilityFlag() == 0 || block == Blocks.piston || block == Blocks.sticky_piston)) {
                    this.doMove(worldIn, pos, enumfacing, false);
                }
            } else {
                worldIn.setBlockToAir(pos.offset(enumfacing));
            }
            worldIn.playSoundEffect((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, "tile.piston.in", 0.5f, worldIn.rand.nextFloat() * 0.15f + 0.6f);
        }
        return true;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        if (iblockstate.getBlock() == this && ((Boolean)iblockstate.getValue((IProperty)EXTENDED)).booleanValue()) {
            float f = 0.25f;
            EnumFacing enumfacing = (EnumFacing)iblockstate.getValue((IProperty)FACING);
            if (enumfacing != null) {
                switch (1.$SwitchMap$net$minecraft$util$EnumFacing[enumfacing.ordinal()]) {
                    case 1: {
                        this.setBlockBounds(0.0f, 0.25f, 0.0f, 1.0f, 1.0f, 1.0f);
                        break;
                    }
                    case 2: {
                        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.75f, 1.0f);
                        break;
                    }
                    case 3: {
                        this.setBlockBounds(0.0f, 0.0f, 0.25f, 1.0f, 1.0f, 1.0f);
                        break;
                    }
                    case 4: {
                        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.75f);
                        break;
                    }
                    case 5: {
                        this.setBlockBounds(0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                        break;
                    }
                    case 6: {
                        this.setBlockBounds(0.0f, 0.0f, 0.0f, 0.75f, 1.0f, 1.0f);
                    }
                }
            }
        } else {
            this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public void setBlockBoundsForItemRender() {
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        this.setBlockBoundsBasedOnState((IBlockAccess)worldIn, pos);
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    public boolean isFullCube() {
        return false;
    }

    public static EnumFacing getFacing(int meta) {
        int i = meta & 7;
        return i > 5 ? null : EnumFacing.getFront((int)i);
    }

    public static EnumFacing getFacingFromEntity(World worldIn, BlockPos clickedBlock, EntityLivingBase entityIn) {
        if (MathHelper.abs((float)((float)entityIn.posX - (float)clickedBlock.getX())) < 2.0f && MathHelper.abs((float)((float)entityIn.posZ - (float)clickedBlock.getZ())) < 2.0f) {
            double d0 = entityIn.posY + (double)entityIn.getEyeHeight();
            if (d0 - (double)clickedBlock.getY() > 2.0) {
                return EnumFacing.UP;
            }
            if ((double)clickedBlock.getY() - d0 > 0.0) {
                return EnumFacing.DOWN;
            }
        }
        return entityIn.getHorizontalFacing().getOpposite();
    }

    public static boolean canPush(Block blockIn, World worldIn, BlockPos pos, EnumFacing direction, boolean allowDestroy) {
        if (blockIn == Blocks.obsidian) {
            return false;
        }
        if (!worldIn.getWorldBorder().contains(pos)) {
            return false;
        }
        if (pos.getY() >= 0 && (direction != EnumFacing.DOWN || pos.getY() != 0)) {
            if (pos.getY() <= worldIn.getHeight() - 1 && (direction != EnumFacing.UP || pos.getY() != worldIn.getHeight() - 1)) {
                if (blockIn != Blocks.piston && blockIn != Blocks.sticky_piston) {
                    if (blockIn.getBlockHardness(worldIn, pos) == -1.0f) {
                        return false;
                    }
                    if (blockIn.getMobilityFlag() == 2) {
                        return false;
                    }
                    if (blockIn.getMobilityFlag() == 1) {
                        return allowDestroy;
                    }
                } else if (((Boolean)worldIn.getBlockState(pos).getValue((IProperty)EXTENDED)).booleanValue()) {
                    return false;
                }
                return !(blockIn instanceof ITileEntityProvider);
            }
            return false;
        }
        return false;
    }

    private boolean doMove(World worldIn, BlockPos pos, EnumFacing direction, boolean extending) {
        if (!extending) {
            worldIn.setBlockToAir(pos.offset(direction));
        }
        BlockPistonStructureHelper blockpistonstructurehelper = new BlockPistonStructureHelper(worldIn, pos, direction, extending);
        List list = blockpistonstructurehelper.getBlocksToMove();
        List list1 = blockpistonstructurehelper.getBlocksToDestroy();
        if (!blockpistonstructurehelper.canMove()) {
            return false;
        }
        int i = list.size() + list1.size();
        Block[] ablock = new Block[i];
        EnumFacing enumfacing = extending ? direction : direction.getOpposite();
        for (int j = list1.size() - 1; j >= 0; --j) {
            BlockPos blockpos = (BlockPos)list1.get(j);
            Block block = worldIn.getBlockState(blockpos).getBlock();
            block.dropBlockAsItem(worldIn, blockpos, worldIn.getBlockState(blockpos), 0);
            worldIn.setBlockToAir(blockpos);
            ablock[--i] = block;
        }
        for (int k = list.size() - 1; k >= 0; --k) {
            BlockPos blockpos2 = (BlockPos)list.get(k);
            IBlockState iblockstate = worldIn.getBlockState(blockpos2);
            Block block1 = iblockstate.getBlock();
            block1.getMetaFromState(iblockstate);
            worldIn.setBlockToAir(blockpos2);
            blockpos2 = blockpos2.offset(enumfacing);
            worldIn.setBlockState(blockpos2, Blocks.piston_extension.getDefaultState().withProperty((IProperty)FACING, (Comparable)direction), 4);
            worldIn.setTileEntity(blockpos2, BlockPistonMoving.newTileEntity((IBlockState)iblockstate, (EnumFacing)direction, (boolean)extending, (boolean)false));
            ablock[--i] = block1;
        }
        BlockPos blockpos1 = pos.offset(direction);
        if (extending) {
            BlockPistonExtension.EnumPistonType blockpistonextension$enumpistontype = this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;
            IBlockState iblockstate1 = Blocks.piston_head.getDefaultState().withProperty((IProperty)BlockPistonExtension.FACING, (Comparable)direction).withProperty((IProperty)BlockPistonExtension.TYPE, (Comparable)blockpistonextension$enumpistontype);
            IBlockState iblockstate2 = Blocks.piston_extension.getDefaultState().withProperty((IProperty)BlockPistonMoving.FACING, (Comparable)direction).withProperty((IProperty)BlockPistonMoving.TYPE, (Comparable)(this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT));
            worldIn.setBlockState(blockpos1, iblockstate2, 4);
            worldIn.setTileEntity(blockpos1, BlockPistonMoving.newTileEntity((IBlockState)iblockstate1, (EnumFacing)direction, (boolean)true, (boolean)false));
        }
        for (int l = list1.size() - 1; l >= 0; --l) {
            worldIn.notifyNeighborsOfStateChange((BlockPos)list1.get(l), ablock[i++]);
        }
        for (int i1 = list.size() - 1; i1 >= 0; --i1) {
            worldIn.notifyNeighborsOfStateChange((BlockPos)list.get(i1), ablock[i++]);
        }
        if (extending) {
            worldIn.notifyNeighborsOfStateChange(blockpos1, (Block)Blocks.piston_head);
            worldIn.notifyNeighborsOfStateChange(pos, (Block)this);
        }
        return true;
    }

    public IBlockState getStateForEntityRender(IBlockState state) {
        return this.getDefaultState().withProperty((IProperty)FACING, (Comparable)EnumFacing.UP);
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty((IProperty)FACING, (Comparable)BlockPistonBase.getFacing(meta)).withProperty((IProperty)EXTENDED, (Comparable)Boolean.valueOf(((meta & 8) > 0 ? 1 : 0) != 0));
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i |= ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
        if (((Boolean)state.getValue((IProperty)EXTENDED)).booleanValue()) {
            i |= 8;
        }
        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState((Block)this, new IProperty[]{FACING, EXTENDED});
    }
}
