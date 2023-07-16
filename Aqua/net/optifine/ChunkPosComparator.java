package net.optifine;

import java.util.Comparator;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;

public class ChunkPosComparator
implements Comparator<ChunkCoordIntPair> {
    private int chunkPosX;
    private int chunkPosZ;
    private double yawRad;
    private double pitchNorm;

    public ChunkPosComparator(int chunkPosX, int chunkPosZ, double yawRad, double pitchRad) {
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        this.yawRad = yawRad;
        this.pitchNorm = 1.0 - MathHelper.clamp_double((double)(Math.abs((double)pitchRad) / 1.5707963267948966), (double)0.0, (double)1.0);
    }

    public int compare(ChunkCoordIntPair cp1, ChunkCoordIntPair cp2) {
        int i = this.getDistSq(cp1);
        int j = this.getDistSq(cp2);
        return i - j;
    }

    private int getDistSq(ChunkCoordIntPair cp) {
        int i = cp.chunkXPos - this.chunkPosX;
        int j = cp.chunkZPos - this.chunkPosZ;
        int k = i * i + j * j;
        double d0 = MathHelper.atan2((double)j, (double)i);
        double d1 = Math.abs((double)(d0 - this.yawRad));
        if (d1 > Math.PI) {
            d1 = Math.PI * 2 - d1;
        }
        k = (int)((double)k * 1000.0 * this.pitchNorm * d1 * d1);
        return k;
    }
}
