package net.minecraft.client.renderer.vertex;

public static enum VertexFormatElement.EnumType {
    FLOAT(4, "Float", 5126),
    UBYTE(1, "Unsigned Byte", 5121),
    BYTE(1, "Byte", 5120),
    USHORT(2, "Unsigned Short", 5123),
    SHORT(2, "Short", 5122),
    UINT(4, "Unsigned Int", 5125),
    INT(4, "Int", 5124);

    private final int size;
    private final String displayName;
    private final int glConstant;

    private VertexFormatElement.EnumType(int sizeIn, String displayNameIn, int glConstantIn) {
        this.size = sizeIn;
        this.displayName = displayNameIn;
        this.glConstant = glConstantIn;
    }

    public int getSize() {
        return this.size;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getGlConstant() {
        return this.glConstant;
    }
}
