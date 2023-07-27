package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;

public class GenLayerRareBiome extends GenLayer {
    public GenLayerRareBiome(final long p_i45478_1_, final GenLayer p_i45478_3_) {
        super(p_i45478_1_);
        this.parent = p_i45478_3_;
    }

    /**
     * Returns a list of integer values generated by this layer. These may be interpreted as temperatures, rainfall
     * amounts, or biomeList[] indices based on the particular GenLayer subclass.
     */
    public int[] getInts(final int areaX, final int areaY, final int areaWidth, final int areaHeight) {
        final int[] aint = this.parent.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        final int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);

        for (int i = 0; i < areaHeight; ++i) {
            for (int j = 0; j < areaWidth; ++j) {
                this.initChunkSeed(j + areaX, i + areaY);
                final int k = aint[j + 1 + (i + 1) * (areaWidth + 2)];

                if (this.nextInt(57) == 0) {
                    if (k == BiomeGenBase.plains.biomeID) {
                        aint1[j + i * areaWidth] = BiomeGenBase.plains.biomeID + 128;
                    } else {
                        aint1[j + i * areaWidth] = k;
                    }
                } else {
                    aint1[j + i * areaWidth] = k;
                }
            }
        }

        return aint1;
    }
}