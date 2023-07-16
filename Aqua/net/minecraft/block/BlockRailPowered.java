package net.minecraft.block;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockRailPowered
extends BlockRailBase {
    public static final PropertyEnum<BlockRailBase.EnumRailDirection> SHAPE = PropertyEnum.create((String)"shape", BlockRailBase.EnumRailDirection.class, (Predicate)new /* Unavailable Anonymous Inner Class!! */);
    public static final PropertyBool POWERED = PropertyBool.create((String)"powered");

    protected BlockRailPowered() {
        super(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, (Comparable)BlockRailBase.EnumRailDirection.NORTH_SOUTH).withProperty((IProperty)POWERED, (Comparable)Boolean.valueOf((boolean)false)));
    }

    protected boolean func_176566_a(World worldIn, BlockPos pos, IBlockState state, boolean p_176566_4_, int p_176566_5_) {
        if (p_176566_5_ >= 8) {
            return false;
        }
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        boolean flag = true;
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)state.getValue(SHAPE);
        switch (2.$SwitchMap$net$minecraft$block$BlockRailBase$EnumRailDirection[blockrailbase$enumraildirection.ordinal()]) {
            case 1: {
                if (p_176566_4_) {
                    ++k;
                    break;
                }
                --k;
                break;
            }
            case 2: {
                if (p_176566_4_) {
                    --i;
                    break;
                }
                ++i;
                break;
            }
            case 3: {
                if (p_176566_4_) {
                    --i;
                } else {
                    ++i;
                    ++j;
                    flag = false;
                }
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
                break;
            }
            case 4: {
                if (p_176566_4_) {
                    --i;
                    ++j;
                    flag = false;
                } else {
                    ++i;
                }
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
                break;
            }
            case 5: {
                if (p_176566_4_) {
                    ++k;
                } else {
                    --k;
                    ++j;
                    flag = false;
                }
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                break;
            }
            case 6: {
                if (p_176566_4_) {
                    ++k;
                    ++j;
                    flag = false;
                } else {
                    --k;
                }
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }
        }
        return this.func_176567_a(worldIn, new BlockPos(i, j, k), p_176566_4_, p_176566_5_, blockrailbase$enumraildirection) ? true : flag && this.func_176567_a(worldIn, new BlockPos(i, j - 1, k), p_176566_4_, p_176566_5_, blockrailbase$enumraildirection);
    }

    protected boolean func_176567_a(World worldIn, BlockPos p_176567_2_, boolean p_176567_3_, int distance, BlockRailBase.EnumRailDirection p_176567_5_) {
        IBlockState iblockstate = worldIn.getBlockState(p_176567_2_);
        if (iblockstate.getBlock() != this) {
            return false;
        }
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)iblockstate.getValue(SHAPE);
        return p_176567_5_ != BlockRailBase.EnumRailDirection.EAST_WEST || blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.NORTH_SOUTH && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_NORTH && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_SOUTH ? (p_176567_5_ != BlockRailBase.EnumRailDirection.NORTH_SOUTH || blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.EAST_WEST && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_EAST && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_WEST ? (((Boolean)iblockstate.getValue((IProperty)POWERED)).booleanValue() ? (worldIn.isBlockPowered(p_176567_2_) ? true : this.func_176566_a(worldIn, p_176567_2_, iblockstate, p_176567_3_, distance + 1)) : false) : false) : false;
    }

    protected void onNeighborChangedInternal(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        boolean flag1;
        boolean flag = (Boolean)state.getValue((IProperty)POWERED);
        boolean bl = flag1 = worldIn.isBlockPowered(pos) || this.func_176566_a(worldIn, pos, state, true, 0) || this.func_176566_a(worldIn, pos, state, false, 0);
        if (flag1 != flag) {
            worldIn.setBlockState(pos, state.withProperty((IProperty)POWERED, (Comparable)Boolean.valueOf((boolean)flag1)), 3);
            worldIn.notifyNeighborsOfStateChange(pos.down(), (Block)this);
            if (((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)).isAscending()) {
                worldIn.notifyNeighborsOfStateChange(pos.up(), (Block)this);
            }
        }
    }

    public IProperty<BlockRailBase.EnumRailDirection> getShapeProperty() {
        return SHAPE;
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(SHAPE, (Comparable)BlockRailBase.EnumRailDirection.byMetadata((int)(meta & 7))).withProperty((IProperty)POWERED, (Comparable)Boolean.valueOf(((meta & 8) > 0 ? 1 : 0) != 0));
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i |= ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)).getMetadata();
        if (((Boolean)state.getValue((IProperty)POWERED)).booleanValue()) {
            i |= 8;
        }
        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState((Block)this, new IProperty[]{SHAPE, POWERED});
    }
}
