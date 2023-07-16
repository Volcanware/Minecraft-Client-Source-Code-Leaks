package net.optifine.shaders.uniform;

import java.util.Arrays;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.ARBShaderObjects;

public abstract class ShaderUniformBase {
    private String name;
    private int program = 0;
    private int[] locations = new int[]{-1};
    private static final int LOCATION_UNDEFINED = -1;
    private static final int LOCATION_UNKNOWN = Integer.MIN_VALUE;

    public ShaderUniformBase(String name) {
        this.name = name;
    }

    public void setProgram(int program) {
        if (this.program != program) {
            this.program = program;
            this.expandLocations();
            this.onProgramSet(program);
        }
    }

    private void expandLocations() {
        if (this.program >= this.locations.length) {
            int[] aint = new int[this.program * 2];
            Arrays.fill((int[])aint, (int)Integer.MIN_VALUE);
            System.arraycopy((Object)this.locations, (int)0, (Object)aint, (int)0, (int)this.locations.length);
            this.locations = aint;
        }
    }

    protected abstract void onProgramSet(int var1);

    public String getName() {
        return this.name;
    }

    public int getProgram() {
        return this.program;
    }

    public int getLocation() {
        if (this.program <= 0) {
            return -1;
        }
        int i = this.locations[this.program];
        if (i == Integer.MIN_VALUE) {
            this.locations[this.program] = i = ARBShaderObjects.glGetUniformLocationARB((int)this.program, (CharSequence)this.name);
        }
        return i;
    }

    public boolean isDefined() {
        return this.getLocation() >= 0;
    }

    public void disable() {
        this.locations[this.program] = -1;
    }

    public void reset() {
        this.program = 0;
        this.locations = new int[]{-1};
        this.resetValue();
    }

    protected abstract void resetValue();

    protected void checkGLError() {
        if (Shaders.checkGLError((String)this.name) != 0) {
            this.disable();
        }
    }

    public String toString() {
        return this.name;
    }
}
