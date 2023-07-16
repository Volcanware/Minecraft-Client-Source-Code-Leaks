package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import intent.AquaDev.aqua.Aqua;
import intent.AquaDev.aqua.modules.combat.Killaura;
import java.nio.FloatBuffer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.src.Config;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.optifine.EmissiveTextures;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorConstructor;
import net.optifine.reflect.ReflectorMethod;
import net.optifine.shaders.Shaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public abstract class RendererLivingEntity<T extends EntityLivingBase>
extends Render<T> {
    private static final Logger logger = LogManager.getLogger();
    private static final DynamicTexture textureBrightness = new DynamicTexture(16, 16);
    public ModelBase mainModel;
    protected FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer((int)4);
    protected List<LayerRenderer<T>> layerRenderers = Lists.newArrayList();
    protected boolean renderOutlines = false;
    public static float NAME_TAG_RANGE = 64.0f;
    public static float NAME_TAG_RANGE_SNEAK = 32.0f;
    public EntityLivingBase renderEntity;
    public float renderLimbSwing;
    public float renderLimbSwingAmount;
    public float renderAgeInTicks;
    public float renderHeadYaw;
    public float renderHeadPitch;
    public float renderScaleFactor;
    public float renderPartialTicks;
    private boolean renderModelPushMatrix;
    private boolean renderLayersPushMatrix;
    public static final boolean animateModelLiving = Boolean.getBoolean((String)"animate.model.living");

    public RendererLivingEntity(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
        this.mainModel = modelBaseIn;
        this.shadowSize = shadowSizeIn;
        this.renderModelPushMatrix = this.mainModel instanceof ModelSpider;
    }

    public <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean addLayer(U layer) {
        return this.layerRenderers.add(layer);
    }

    protected <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean removeLayer(U layer) {
        return this.layerRenderers.remove(layer);
    }

    public ModelBase getMainModel() {
        return this.mainModel;
    }

    protected float interpolateRotation(float par1, float par2, float par3) {
        float f;
        for (f = par2 - par1; f < -180.0f; f += 360.0f) {
        }
        while (f >= 180.0f) {
            f -= 360.0f;
        }
        return par1 + par3 * f;
    }

    public void transformHeldFull3DItemLayer() {
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!Reflector.RenderLivingEvent_Pre_Constructor.exists() || !Reflector.postForgeBusEvent((ReflectorConstructor)Reflector.RenderLivingEvent_Pre_Constructor, (Object[])new Object[]{entity, this, x, y, z})) {
            if (animateModelLiving) {
                ((EntityLivingBase)entity).limbSwingAmount = 1.0f;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
            this.mainModel.isRiding = entity.isRiding();
            if (Reflector.ForgeEntity_shouldRiderSit.exists()) {
                this.mainModel.isRiding = entity.isRiding() && ((EntityLivingBase)entity).ridingEntity != null && Reflector.callBoolean((Object)((EntityLivingBase)entity).ridingEntity, (ReflectorMethod)Reflector.ForgeEntity_shouldRiderSit, (Object[])new Object[0]);
            }
            this.mainModel.isChild = entity.isChild();
            try {
                float f = this.interpolateRotation(((EntityLivingBase)entity).prevRenderYawOffset, ((EntityLivingBase)entity).renderYawOffset, partialTicks);
                float f1 = this.interpolateRotation(((EntityLivingBase)entity).prevRotationYawHead, ((EntityLivingBase)entity).rotationYawHead, partialTicks);
                float f2 = f1 - f;
                if (this.mainModel.isRiding && ((EntityLivingBase)entity).ridingEntity instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase)((EntityLivingBase)entity).ridingEntity;
                    f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    f2 = f1 - f;
                    float f3 = MathHelper.wrapAngleTo180_float((float)f2);
                    if (f3 < -85.0f) {
                        f3 = -85.0f;
                    }
                    if (f3 >= 85.0f) {
                        f3 = 85.0f;
                    }
                    f = f1 - f3;
                    if (f3 * f3 > 2500.0f) {
                        f += f3 * 0.2f;
                    }
                    f2 = f1 - f;
                }
                float f7 = Aqua.moduleManager.getModuleByName("Scaffold").isToggled() || Aqua.moduleManager.getModuleByName("Longjump").isToggled() || Aqua.moduleManager.getModuleByName("Killaura").isToggled() && Killaura.target != null ? ((EntityLivingBase)entity).rotationPitchHead : ((EntityLivingBase)entity).prevRotationPitch + 0.0f * partialTicks;
                this.renderLivingAt(entity, x, y, z);
                float f8 = this.handleRotationFloat(entity, partialTicks);
                this.rotateCorpse(entity, f8, f, partialTicks);
                GlStateManager.enableRescaleNormal();
                GlStateManager.scale((float)-1.0f, (float)-1.0f, (float)1.0f);
                this.preRenderCallback(entity, partialTicks);
                float f4 = 0.0625f;
                GlStateManager.translate((float)0.0f, (float)-1.5078125f, (float)0.0f);
                float f5 = ((EntityLivingBase)entity).prevLimbSwingAmount + (((EntityLivingBase)entity).limbSwingAmount - ((EntityLivingBase)entity).prevLimbSwingAmount) * partialTicks;
                float f6 = ((EntityLivingBase)entity).limbSwing - ((EntityLivingBase)entity).limbSwingAmount * (1.0f - partialTicks);
                if (entity.isChild()) {
                    f6 *= 3.0f;
                }
                if (f5 > 1.0f) {
                    f5 = 1.0f;
                }
                GlStateManager.enableAlpha();
                this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
                this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, 0.0625f, entity);
                if (CustomEntityModels.isActive()) {
                    this.renderEntity = entity;
                    this.renderLimbSwing = f6;
                    this.renderLimbSwingAmount = f5;
                    this.renderAgeInTicks = f8;
                    this.renderHeadYaw = f2;
                    this.renderHeadPitch = f7;
                    this.renderScaleFactor = f4;
                    this.renderPartialTicks = partialTicks;
                }
                if (this.renderOutlines) {
                    boolean flag1 = this.setScoreTeamColor(entity);
                    this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625f);
                    if (flag1) {
                        this.unsetScoreTeamColor();
                    }
                } else {
                    boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                    if (EmissiveTextures.isActive()) {
                        EmissiveTextures.beginRender();
                    }
                    if (this.renderModelPushMatrix) {
                        GlStateManager.pushMatrix();
                    }
                    this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625f);
                    if (this.renderModelPushMatrix) {
                        GlStateManager.popMatrix();
                    }
                    if (EmissiveTextures.isActive()) {
                        if (EmissiveTextures.hasEmissive()) {
                            this.renderModelPushMatrix = true;
                            EmissiveTextures.beginRenderEmissive();
                            GlStateManager.pushMatrix();
                            this.renderModel(entity, f6, f5, f8, f2, f7, f4);
                            GlStateManager.popMatrix();
                            EmissiveTextures.endRenderEmissive();
                        }
                        EmissiveTextures.endRender();
                    }
                    if (flag) {
                        this.unsetBrightness();
                    }
                    GlStateManager.depthMask((boolean)true);
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
                        this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625f);
                    }
                }
                if (CustomEntityModels.isActive()) {
                    this.renderEntity = null;
                }
                GlStateManager.disableRescaleNormal();
            }
            catch (Exception exception) {
                logger.error("Couldn't render entity", (Throwable)exception);
            }
            GlStateManager.setActiveTexture((int)OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
            if (!this.renderOutlines) {
                super.doRender(entity, x, y, z, entityYaw, partialTicks);
            }
            if (Reflector.RenderLivingEvent_Post_Constructor.exists()) {
                Reflector.postForgeBusEvent((ReflectorConstructor)Reflector.RenderLivingEvent_Post_Constructor, (Object[])new Object[]{entity, this, x, y, z});
            }
        }
    }

    protected boolean setScoreTeamColor(T entityLivingBaseIn) {
        String s;
        ScorePlayerTeam scoreplayerteam;
        int i = 0xFFFFFF;
        if (entityLivingBaseIn instanceof EntityPlayer && (scoreplayerteam = (ScorePlayerTeam)entityLivingBaseIn.getTeam()) != null && (s = FontRenderer.getFormatFromString((String)scoreplayerteam.getColorPrefix())).length() >= 2) {
            i = this.getFontRendererFromRenderManager().getColorCode(s.charAt(1));
        }
        float f1 = (float)(i >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(i >> 8 & 0xFF) / 255.0f;
        float f = (float)(i & 0xFF) / 255.0f;
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
        GlStateManager.color((float)f1, (float)f2, (float)f, (float)1.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture((int)OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void unsetScoreTeamColor() {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture((int)OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
    }

    protected void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor) {
        boolean flag1;
        boolean flag = !entitylivingbaseIn.isInvisible();
        boolean bl = flag1 = !flag && !entitylivingbaseIn.isInvisibleToPlayer((EntityPlayer)Minecraft.getMinecraft().thePlayer);
        if (flag || flag1) {
            if (!this.bindEntityTexture((Entity)entitylivingbaseIn)) {
                return;
            }
            if (flag1) {
                GlStateManager.pushMatrix();
                GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)0.15f);
                GlStateManager.depthMask((boolean)false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc((int)770, (int)771);
                GlStateManager.alphaFunc((int)516, (float)0.003921569f);
            }
            this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
            if (flag1) {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc((int)516, (float)0.1f);
                GlStateManager.popMatrix();
                GlStateManager.depthMask((boolean)true);
            }
        }
    }

    protected boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks) {
        return this.setBrightness(entityLivingBaseIn, partialTicks, true);
    }

    protected boolean setBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures) {
        boolean flag1;
        float f = entitylivingbaseIn.getBrightness(partialTicks);
        int i = this.getColorMultiplier(entitylivingbaseIn, f, partialTicks);
        boolean flag = (i >> 24 & 0xFF) > 0;
        boolean bl = flag1 = ((EntityLivingBase)entitylivingbaseIn).hurtTime > 0 || ((EntityLivingBase)entitylivingbaseIn).deathTime > 0;
        if (!flag && !flag1) {
            return false;
        }
        if (!flag && !combineTextures) {
            return false;
        }
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi((int)8960, (int)8704, (int)OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_RGB, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_RGB, (int)OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE1_RGB, (int)OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND1_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_ALPHA, (int)7681);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_ALPHA, (int)OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_ALPHA, (int)770);
        GlStateManager.setActiveTexture((int)OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi((int)8960, (int)8704, (int)OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_RGB, (int)OpenGlHelper.GL_INTERPOLATE);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_RGB, (int)OpenGlHelper.GL_CONSTANT);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE1_RGB, (int)OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE2_RGB, (int)OpenGlHelper.GL_CONSTANT);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND1_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND2_RGB, (int)770);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_ALPHA, (int)7681);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_ALPHA, (int)OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_ALPHA, (int)770);
        this.brightnessBuffer.position(0);
        if (flag1) {
            this.brightnessBuffer.put(1.0f);
            this.brightnessBuffer.put(0.0f);
            this.brightnessBuffer.put(0.0f);
            this.brightnessBuffer.put(0.3f);
            if (Config.isShaders()) {
                Shaders.setEntityColor((float)1.0f, (float)0.0f, (float)0.0f, (float)0.3f);
            }
        } else {
            float f1 = (float)(i >> 24 & 0xFF) / 255.0f;
            float f2 = (float)(i >> 16 & 0xFF) / 255.0f;
            float f3 = (float)(i >> 8 & 0xFF) / 255.0f;
            float f4 = (float)(i & 0xFF) / 255.0f;
            this.brightnessBuffer.put(f2);
            this.brightnessBuffer.put(f3);
            this.brightnessBuffer.put(f4);
            this.brightnessBuffer.put(1.0f - f1);
            if (Config.isShaders()) {
                Shaders.setEntityColor((float)f2, (float)f3, (float)f4, (float)(1.0f - f1));
            }
        }
        this.brightnessBuffer.flip();
        GL11.glTexEnv((int)8960, (int)8705, (FloatBuffer)this.brightnessBuffer);
        GlStateManager.setActiveTexture((int)OpenGlHelper.GL_TEXTURE2);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture((int)textureBrightness.getGlTextureId());
        GL11.glTexEnvi((int)8960, (int)8704, (int)OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_RGB, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_RGB, (int)OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE1_RGB, (int)OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND1_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_ALPHA, (int)7681);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_ALPHA, (int)OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_ALPHA, (int)770);
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void unsetBrightness() {
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi((int)8960, (int)8704, (int)OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_RGB, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_RGB, (int)OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE1_RGB, (int)OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND1_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_ALPHA, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_ALPHA, (int)OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE1_ALPHA, (int)OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_ALPHA, (int)770);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND1_ALPHA, (int)770);
        GlStateManager.setActiveTexture((int)OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi((int)8960, (int)8704, (int)OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_RGB, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND1_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_RGB, (int)5890);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE1_RGB, (int)OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_ALPHA, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_ALPHA, (int)770);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_ALPHA, (int)5890);
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.setActiveTexture((int)OpenGlHelper.GL_TEXTURE2);
        GlStateManager.disableTexture2D();
        GlStateManager.bindTexture((int)0);
        GL11.glTexEnvi((int)8960, (int)8704, (int)OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_RGB, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND1_RGB, (int)768);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_RGB, (int)5890);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE1_RGB, (int)OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_COMBINE_ALPHA, (int)8448);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_OPERAND0_ALPHA, (int)770);
        GL11.glTexEnvi((int)8960, (int)OpenGlHelper.GL_SOURCE0_ALPHA, (int)5890);
        GlStateManager.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
        if (Config.isShaders()) {
            Shaders.setEntityColor((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f);
        }
    }

    protected void renderLivingAt(T entityLivingBaseIn, double x, double y, double z) {
        GlStateManager.translate((float)((float)x), (float)((float)y), (float)((float)z));
    }

    protected void rotateCorpse(T bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        GlStateManager.rotate((float)(180.0f - p_77043_3_), (float)0.0f, (float)1.0f, (float)0.0f);
        if (((EntityLivingBase)bat).deathTime > 0) {
            float f = ((float)((EntityLivingBase)bat).deathTime + partialTicks - 1.0f) / 20.0f * 1.6f;
            if ((f = MathHelper.sqrt_float((float)f)) > 1.0f) {
                f = 1.0f;
            }
            GlStateManager.rotate((float)(f * this.getDeathMaxRotation(bat)), (float)0.0f, (float)0.0f, (float)1.0f);
        } else {
            String s = EnumChatFormatting.getTextWithoutFormattingCodes((String)bat.getName());
            if (s != null && (s.equals((Object)"Dinnerbone") || s.equals((Object)"Grumm")) && (!(bat instanceof EntityPlayer) || ((EntityPlayer)bat).isWearing(EnumPlayerModelParts.CAPE))) {
                GlStateManager.translate((float)0.0f, (float)(((EntityLivingBase)bat).height + 0.1f), (float)0.0f);
                GlStateManager.rotate((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            }
        }
    }

    protected float getSwingProgress(T livingBase, float partialTickTime) {
        return livingBase.getSwingProgress(partialTickTime);
    }

    protected float handleRotationFloat(T livingBase, float partialTicks) {
        return (float)((EntityLivingBase)livingBase).ticksExisted + partialTicks;
    }

    protected void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_) {
        for (LayerRenderer layerrenderer : this.layerRenderers) {
            boolean flag = this.setBrightness(entitylivingbaseIn, partialTicks, layerrenderer.shouldCombineTextures());
            if (EmissiveTextures.isActive()) {
                EmissiveTextures.beginRender();
            }
            if (this.renderLayersPushMatrix) {
                GlStateManager.pushMatrix();
            }
            layerrenderer.doRenderLayer(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);
            if (this.renderLayersPushMatrix) {
                GlStateManager.popMatrix();
            }
            if (EmissiveTextures.isActive()) {
                if (EmissiveTextures.hasEmissive()) {
                    this.renderLayersPushMatrix = true;
                    EmissiveTextures.beginRenderEmissive();
                    GlStateManager.pushMatrix();
                    layerrenderer.doRenderLayer(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);
                    GlStateManager.popMatrix();
                    EmissiveTextures.endRenderEmissive();
                }
                EmissiveTextures.endRender();
            }
            if (!flag) continue;
            this.unsetBrightness();
        }
    }

    protected float getDeathMaxRotation(T entityLivingBaseIn) {
        return 90.0f;
    }

    protected int getColorMultiplier(T entitylivingbaseIn, float lightBrightness, float partialTickTime) {
        return 0;
    }

    protected void preRenderCallback(T entitylivingbaseIn, float partialTickTime) {
    }

    public void renderName(T entity, double x, double y, double z) {
        if (!Reflector.RenderLivingEvent_Specials_Pre_Constructor.exists() || !Reflector.postForgeBusEvent((ReflectorConstructor)Reflector.RenderLivingEvent_Specials_Pre_Constructor, (Object[])new Object[]{entity, this, x, y, z})) {
            if (this.canRenderName(entity)) {
                float f;
                double d0 = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
                float f2 = f = entity.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;
                if (d0 < (double)(f * f)) {
                    String s = entity.getDisplayName().getFormattedText();
                    float f1 = 0.02666667f;
                    GlStateManager.alphaFunc((int)516, (float)0.1f);
                    if (entity.isSneaking()) {
                        FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                        GlStateManager.pushMatrix();
                        GlStateManager.translate((float)((float)x), (float)((float)y + ((EntityLivingBase)entity).height + 0.5f - (entity.isChild() ? ((EntityLivingBase)entity).height / 2.0f : 0.0f)), (float)((float)z));
                        GL11.glNormal3f((float)0.0f, (float)1.0f, (float)0.0f);
                        GlStateManager.rotate((float)(-this.renderManager.playerViewY), (float)0.0f, (float)1.0f, (float)0.0f);
                        GlStateManager.rotate((float)this.renderManager.playerViewX, (float)1.0f, (float)0.0f, (float)0.0f);
                        GlStateManager.scale((float)-0.02666667f, (float)-0.02666667f, (float)0.02666667f);
                        GlStateManager.translate((float)0.0f, (float)9.374999f, (float)0.0f);
                        GlStateManager.disableLighting();
                        GlStateManager.depthMask((boolean)false);
                        GlStateManager.enableBlend();
                        GlStateManager.disableTexture2D();
                        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
                        int i = fontrenderer.getStringWidth(s) / 2;
                        Tessellator tessellator = Tessellator.getInstance();
                        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        worldrenderer.pos((double)(-i - 1), -1.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                        worldrenderer.pos((double)(-i - 1), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                        worldrenderer.pos((double)(i + 1), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                        worldrenderer.pos((double)(i + 1), -1.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                        tessellator.draw();
                        GlStateManager.enableTexture2D();
                        GlStateManager.depthMask((boolean)true);
                        fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 0x20FFFFFF);
                        GlStateManager.enableLighting();
                        GlStateManager.disableBlend();
                        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                        GlStateManager.popMatrix();
                    } else {
                        this.renderOffsetLivingLabel((Entity)entity, x, y - (entity.isChild() ? (double)(((EntityLivingBase)entity).height / 2.0f) : 0.0), z, s, 0.02666667f, d0);
                    }
                }
            }
            if (Reflector.RenderLivingEvent_Specials_Post_Constructor.exists()) {
                Reflector.postForgeBusEvent((ReflectorConstructor)Reflector.RenderLivingEvent_Specials_Post_Constructor, (Object[])new Object[]{entity, this, x, y, z});
            }
        }
    }

    protected boolean canRenderName(T entity) {
        EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;
        if (entity instanceof EntityPlayer && entity != entityplayersp) {
            Team team = entity.getTeam();
            Team team1 = entityplayersp.getTeam();
            if (team != null) {
                Team.EnumVisible team$enumvisible = team.getNameTagVisibility();
                switch (1.$SwitchMap$net$minecraft$scoreboard$Team$EnumVisible[team$enumvisible.ordinal()]) {
                    case 1: {
                        return true;
                    }
                    case 2: {
                        return false;
                    }
                    case 3: {
                        return team1 == null || team.isSameTeam(team1);
                    }
                    case 4: {
                        return team1 == null || !team.isSameTeam(team1);
                    }
                }
                return true;
            }
        }
        return Minecraft.isGuiEnabled() && entity != this.renderManager.livingPlayer && !entity.isInvisibleToPlayer((EntityPlayer)entityplayersp) && ((EntityLivingBase)entity).riddenByEntity == null;
    }

    public void setRenderOutlines(boolean renderOutlinesIn) {
        this.renderOutlines = renderOutlinesIn;
    }

    public List<LayerRenderer<T>> getLayerRenderers() {
        return this.layerRenderers;
    }

    static {
        int[] aint = textureBrightness.getTextureData();
        for (int i = 0; i < 256; ++i) {
            aint[i] = -1;
        }
        textureBrightness.updateDynamicTexture();
    }
}
