package net.minecraft.client.renderer;

import net.minecraft.util.EnumFacing;

public enum EnumFaceDirection {
    DOWN(new EnumFaceDirection.VertexInformation[]{new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX)}),
    UP(new EnumFaceDirection.VertexInformation[]{new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.NORTH_INDEX)}),
    NORTH(new EnumFaceDirection.VertexInformation[]{new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.NORTH_INDEX)}),
    SOUTH(new EnumFaceDirection.VertexInformation[]{new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX)}),
    WEST(new EnumFaceDirection.VertexInformation[]{new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.WEST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX)}),
    EAST(new EnumFaceDirection.VertexInformation[]{new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.SOUTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.DOWN_INDEX, EnumFaceDirection.Constants.NORTH_INDEX), new EnumFaceDirection.VertexInformation(EnumFaceDirection.Constants.EAST_INDEX, EnumFaceDirection.Constants.UP_INDEX, EnumFaceDirection.Constants.NORTH_INDEX)});

    private static final EnumFaceDirection[] facings = new EnumFaceDirection[6];

    static {
        facings[EnumFaceDirection.Constants.DOWN_INDEX] = DOWN;
        facings[EnumFaceDirection.Constants.UP_INDEX] = UP;
        facings[EnumFaceDirection.Constants.NORTH_INDEX] = NORTH;
        facings[EnumFaceDirection.Constants.SOUTH_INDEX] = SOUTH;
        facings[EnumFaceDirection.Constants.WEST_INDEX] = WEST;
        facings[EnumFaceDirection.Constants.EAST_INDEX] = EAST;
    }

    private final EnumFaceDirection.VertexInformation[] vertexInfos;

    EnumFaceDirection(final EnumFaceDirection.VertexInformation[] vertexInfosIn) {
        this.vertexInfos = vertexInfosIn;
    }

    public static EnumFaceDirection getFacing(final EnumFacing facing) {
        return facings[facing.getIndex()];
    }

    public EnumFaceDirection.VertexInformation func_179025_a(final int p_179025_1_) {
        return this.vertexInfos[p_179025_1_];
    }

    public static final class Constants {
        public static final int SOUTH_INDEX = EnumFacing.SOUTH.getIndex();
        public static final int UP_INDEX = EnumFacing.UP.getIndex();
        public static final int EAST_INDEX = EnumFacing.EAST.getIndex();
        public static final int NORTH_INDEX = EnumFacing.NORTH.getIndex();
        public static final int DOWN_INDEX = EnumFacing.DOWN.getIndex();
        public static final int WEST_INDEX = EnumFacing.WEST.getIndex();
    }

    public static class VertexInformation {
        public final int field_179184_a;
        public final int field_179182_b;
        public final int field_179183_c;

        private VertexInformation(final int p_i46270_1_, final int p_i46270_2_, final int p_i46270_3_) {
            this.field_179184_a = p_i46270_1_;
            this.field_179182_b = p_i46270_2_;
            this.field_179183_c = p_i46270_3_;
        }
    }
}
