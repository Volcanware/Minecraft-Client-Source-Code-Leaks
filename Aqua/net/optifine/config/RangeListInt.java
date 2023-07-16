package net.optifine.config;

import net.minecraft.src.Config;
import net.optifine.config.RangeInt;

public class RangeListInt {
    private RangeInt[] ranges = new RangeInt[0];

    public RangeListInt() {
    }

    public RangeListInt(RangeInt ri) {
        this.addRange(ri);
    }

    public void addRange(RangeInt ri) {
        this.ranges = (RangeInt[])Config.addObjectToArray((Object[])this.ranges, (Object)ri);
    }

    public boolean isInRange(int val) {
        for (int i = 0; i < this.ranges.length; ++i) {
            RangeInt rangeint = this.ranges[i];
            if (!rangeint.isInRange(val)) continue;
            return true;
        }
        return false;
    }

    public int getCountRanges() {
        return this.ranges.length;
    }

    public RangeInt getRange(int i) {
        return this.ranges[i];
    }

    public String toString() {
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append("[");
        for (int i = 0; i < this.ranges.length; ++i) {
            RangeInt rangeint = this.ranges[i];
            if (i > 0) {
                stringbuffer.append(", ");
            }
            stringbuffer.append(rangeint.toString());
        }
        stringbuffer.append("]");
        return stringbuffer.toString();
    }
}
