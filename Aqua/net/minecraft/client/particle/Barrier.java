package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class Barrier
extends EntityFX {
    protected Barrier(World worldIn, double p_i46286_2_, double p_i46286_4_, double p_i46286_6_, Item p_i46286_8_) {
        super(worldIn, p_i46286_2_, p_i46286_4_, p_i46286_6_, 0.0, 0.0, 0.0);
        this.setParticleIcon(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(p_i46286_8_));
        this.particleBlue = 1.0f;
        this.particleGreen = 1.0f;
        this.particleRed = 1.0f;
        this.motionZ = 0.0;
        this.motionY = 0.0;
        this.motionX = 0.0;
        this.particleGravity = 0.0f;
        this.particleMaxAge = 80;
    }

    public int getFXLayer() {
        return 1;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = this.particleIcon.getMinU();
        float f1 = this.particleIcon.getMaxU();
        float f2 = this.particleIcon.getMinV();
        float f3 = this.particleIcon.getMaxV();
        float f4 = 0.5f;
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 0xFFFF;
        int k = i & 0xFFFF;
        worldRendererIn.pos((double)(f5 - rotationX * 0.5f - rotationXY * 0.5f), (double)(f6 - rotationZ * 0.5f), (double)(f7 - rotationYZ * 0.5f - rotationXZ * 0.5f)).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
        worldRendererIn.pos((double)(f5 - rotationX * 0.5f + rotationXY * 0.5f), (double)(f6 + rotationZ * 0.5f), (double)(f7 - rotationYZ * 0.5f + rotationXZ * 0.5f)).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
        worldRendererIn.pos((double)(f5 + rotationX * 0.5f + rotationXY * 0.5f), (double)(f6 + rotationZ * 0.5f), (double)(f7 + rotationYZ * 0.5f + rotationXZ * 0.5f)).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
        worldRendererIn.pos((double)(f5 + rotationX * 0.5f - rotationXY * 0.5f), (double)(f6 - rotationZ * 0.5f), (double)(f7 + rotationYZ * 0.5f - rotationXZ * 0.5f)).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
    }
}
