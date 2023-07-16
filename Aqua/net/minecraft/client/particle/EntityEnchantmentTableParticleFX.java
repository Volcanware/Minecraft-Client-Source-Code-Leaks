package net.minecraft.client.particle;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

public class EntityEnchantmentTableParticleFX
extends EntityFX {
    private float field_70565_a;
    private double coordX;
    private double coordY;
    private double coordZ;

    protected EntityEnchantmentTableParticleFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        this.coordX = xCoordIn;
        this.coordY = yCoordIn;
        this.coordZ = zCoordIn;
        this.posX = this.prevPosX = xCoordIn + xSpeedIn;
        this.posY = this.prevPosY = yCoordIn + ySpeedIn;
        this.posZ = this.prevPosZ = zCoordIn + zSpeedIn;
        float f = this.rand.nextFloat() * 0.6f + 0.4f;
        this.field_70565_a = this.particleScale = this.rand.nextFloat() * 0.5f + 0.2f;
        this.particleGreen = this.particleBlue = 1.0f * f;
        this.particleRed = this.particleBlue;
        this.particleGreen *= 0.9f;
        this.particleRed *= 0.9f;
        this.particleMaxAge = (int)(Math.random() * 10.0) + 30;
        this.noClip = true;
        this.setParticleTextureIndex((int)(Math.random() * 26.0 + 1.0 + 224.0));
    }

    public int getBrightnessForRender(float partialTicks) {
        int i = super.getBrightnessForRender(partialTicks);
        float f = (float)this.particleAge / (float)this.particleMaxAge;
        f *= f;
        f *= f;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((k += (int)(f * 15.0f * 16.0f)) > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    public float getBrightness(float partialTicks) {
        float f = super.getBrightness(partialTicks);
        float f1 = (float)this.particleAge / (float)this.particleMaxAge;
        f1 *= f1;
        f1 *= f1;
        return f * (1.0f - f1) + f1;
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        float f = (float)this.particleAge / (float)this.particleMaxAge;
        f = 1.0f - f;
        float f1 = 1.0f - f;
        f1 *= f1;
        f1 *= f1;
        this.posX = this.coordX + this.motionX * (double)f;
        this.posY = this.coordY + this.motionY * (double)f - (double)(f1 * 1.2f);
        this.posZ = this.coordZ + this.motionZ * (double)f;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }
    }
}
