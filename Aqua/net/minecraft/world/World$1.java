package net.minecraft.world;

import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockPos;

class World.1
implements Callable<String> {
    final /* synthetic */ BlockPos val$pos;

    World.1(BlockPos blockPos) {
        this.val$pos = blockPos;
    }

    public String call() throws Exception {
        return CrashReportCategory.getCoordinateInfo((BlockPos)this.val$pos);
    }
}
