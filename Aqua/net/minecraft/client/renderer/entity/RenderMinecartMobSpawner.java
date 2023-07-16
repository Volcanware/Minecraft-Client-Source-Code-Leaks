package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;

public class RenderMinecartMobSpawner
extends RenderMinecart<EntityMinecartMobSpawner> {
    public RenderMinecartMobSpawner(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    protected void func_180560_a(EntityMinecartMobSpawner minecart, float partialTicks, IBlockState state) {
        super.func_180560_a((EntityMinecart)minecart, partialTicks, state);
        if (state.getBlock() == Blocks.mob_spawner) {
            TileEntityMobSpawnerRenderer.renderMob((MobSpawnerBaseLogic)minecart.func_98039_d(), (double)minecart.posX, (double)minecart.posY, (double)minecart.posZ, (float)partialTicks);
        }
    }
}
