package net.minecraft.client.renderer;

import com.alan.clients.module.Module;
import com.alan.clients.module.impl.render.*;
import com.alan.clients.newevent.impl.render.MouseOverEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.newevent.impl.render.Render3DEvent;
import com.alan.clients.newevent.impl.render.ViewBobbingEvent;
import com.google.common.base.Predicates;
import com.google.gson.JsonSyntaxException;
import com.alan.clients.Client;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.shader.ShaderUtil;
import util.time.StopWatch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.*;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.src.Config;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.CustomColors;
import net.optifine.GlErrors;
import net.optifine.Lagometer;
import net.optifine.RandomEntities;
import net.optifine.gui.GuiChatOF;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.TextureUtils;
import net.optifine.util.TimedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class EntityRenderer implements IResourceManagerReloadListener, InstanceAccess {
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation TEXTURE_RAIN = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation TEXTURE_SNOW_HEAVY = new ResourceLocation("textures/environment/snow.png");
    private static final ResourceLocation TEXTURE_SNOW_LIGHT = new ResourceLocation("textures/environment/snow_light.png");
    private static final ResourceLocation TEXTURE_NETHER_PARTICLES = new ResourceLocation("textures/environment/nether_particles.png");
    public static boolean anaglyphEnable;

    /**
     * Anaglyph field (0=R, 1=GB)
     */
    public static int anaglyphField;

    /**
     * A reference to the Minecraft object.
     */
    private final Minecraft mc;
    private final IResourceManager resourceManager;
    private final Random random = new Random();
    private float farPlaneDistance;
    public ItemRenderer itemRenderer;
    private final MapItemRenderer theMapItemRenderer;

    /**
     * Entity renderer update count
     */
    private int rendererUpdateCount;

    /**
     * Pointed entity
     */
    private Entity pointedEntity;
    private MouseFilter mouseFilterXAxis = new MouseFilter();
    private MouseFilter mouseFilterYAxis = new MouseFilter();
    private final float thirdPersonDistance = 4.0F;

    public static float mouseAddedX, mouseAddedY;

    /**
     * Third person distance temp
     */
    private float thirdPersonDistanceTemp = 4.0F;

    /**
     * Smooth cam yaw
     */
    private float smoothCamYaw;

    /**
     * Smooth cam pitch
     */
    private float smoothCamPitch;

    /**
     * Smooth cam filter X
     */
    private float smoothCamFilterX;

    /**
     * Smooth cam filter Y
     */
    private float smoothCamFilterY;

    /**
     * Smooth cam partial ticks
     */
    private float smoothCamPartialTicks;

    /**
     * FOV modifier hand
     */
    private float fovModifierHand;

    /**
     * FOV modifier hand prev
     */
    private float fovModifierHandPrev;
    private float bossColorModifier;
    private float bossColorModifierPrev;

    /**
     * Cloud fog mode
     */
    private boolean cloudFog;
    private final boolean renderHand = true;
    private final boolean drawBlockOutline = true;

    /**
     * Previous frame time in milliseconds
     */
    private long prevFrameTime = Minecraft.getSystemTime();

    /**
     * End time of last render (ns)
     */
    private long renderEndNanoTime;

    /**
     * The texture id of the blocklight/skylight texture used for lighting effects
     */
    private final DynamicTexture lightmapTexture;

    /**
     * Colors computed in updateLightmap() and loaded into the lightmap emptyTexture
     */
    private final int[] lightmapColors;
    private final ResourceLocation locationLightMap;

    /**
     * Is set, updateCameraAndRender() calls updateLightmap(); set by updateTorchFlicker()
     */
    private boolean lightmapUpdateNeeded;

    /**
     * Torch flicker X
     */
    private float torchFlickerX;
    private float torchFlickerDX;

    /**
     * Rain sound counter
     */
    private int rainSoundCounter;
    private final float[] rainXCoords = new float[1024];
    private final float[] rainYCoords = new float[1024];

    /**
     * Fog color buffer
     */
    private final FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
    public float fogColorRed;
    public float fogColorGreen;
    public float fogColorBlue;

    /**
     * Fog color 2
     */
    private float fogColor2;

    /**
     * Fog color 1
     */
    private float fogColor1;
    private final int debugViewDirection = 0;
    private final boolean debugView = false;
    private final double cameraZoom = 1.0D;
    private double cameraYaw;
    private double cameraPitch;
    private ShaderGroup theShaderGroup;
    private static final ResourceLocation[] shaderResourceLocations = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
    public static final int shaderCount = shaderResourceLocations.length;
    private int shaderIndex;
    private boolean useShader;
    public int frameCount;
    private boolean initialized = false;
    private World updatedWorld = null;
    private final boolean showDebugInfo = false;
    public boolean fogStandard = false;
    private float clipDistance = 128.0F;
    private long lastServerTime = 0L;
    private int lastServerTicks = 0;
    private int serverWaitTime = 0;
    private int serverWaitTimeCurrent = 0;
    private float avgServerTimeDiff = 0.0F;
    private float avgServerTickDiff = 0.0F;
    private final ShaderGroup[] fxaaShaders = new ShaderGroup[10];
    private boolean loadVisibleChunks = false;
    private StopWatch previousGuiFrame = new StopWatch();
    private Framebuffer framebuffer = new Framebuffer(1, 1, false);

    public EntityRenderer(final Minecraft mcIn, final IResourceManager resourceManagerIn) {
        this.shaderIndex = shaderCount;
        this.useShader = false;
        this.frameCount = 0;
        this.mc = mcIn;
        this.resourceManager = resourceManagerIn;
        this.itemRenderer = mcIn.getItemRenderer();
        this.theMapItemRenderer = new MapItemRenderer(mcIn.getTextureManager());
        this.lightmapTexture = new DynamicTexture(16, 16);
        this.locationLightMap = mcIn.getTextureManager().getDynamicTextureLocation("lightMap", this.lightmapTexture);
        this.lightmapColors = this.lightmapTexture.getTextureData();
        this.theShaderGroup = null;

        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                final float f = (float) (j - 16);
                final float f1 = (float) (i - 16);
                final float f2 = MathHelper.sqrt_float(f * f + f1 * f1);
                this.rainXCoords[i << 5 | j] = -f1 / f2;
                this.rainYCoords[i << 5 | j] = f / f2;
            }
        }
    }

    public boolean isShaderActive() {
        return OpenGlHelper.shadersSupported && this.theShaderGroup != null;
    }

    public void func_181022_b() {
        if (this.theShaderGroup != null) {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;
        this.shaderIndex = shaderCount;
    }

    public void switchUseShader() {
        this.useShader = !this.useShader;
    }

    /**
     * What shader to use when spectating this entity
     */
    public void loadEntityShader(final Entity entityIn) {
        if (OpenGlHelper.shadersSupported) {
            if (this.theShaderGroup != null) {
                this.theShaderGroup.deleteShaderGroup();
            }

            this.theShaderGroup = null;

            if (entityIn instanceof EntityCreeper) {
                this.loadShader(new ResourceLocation("shaders/post/creeper.json"));
            } else if (entityIn instanceof EntitySpider) {
                this.loadShader(new ResourceLocation("shaders/post/spider.json"));
            } else if (entityIn instanceof EntityEnderman) {
                this.loadShader(new ResourceLocation("shaders/post/invert.json"));
            } else if (Reflector.ForgeHooksClient_loadEntityShader.exists()) {
                Reflector.call(Reflector.ForgeHooksClient_loadEntityShader, entityIn, this);
            }
        }
    }

    public void activateNextShader() {
        if (OpenGlHelper.shadersSupported && this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            if (this.theShaderGroup != null) {
                this.theShaderGroup.deleteShaderGroup();
            }

            this.shaderIndex = (this.shaderIndex + 1) % (shaderResourceLocations.length + 1);

            if (this.shaderIndex != shaderCount) {
                this.loadShader(shaderResourceLocations[this.shaderIndex]);
            } else {
                this.theShaderGroup = null;
            }
        }
    }

    private void loadShader(final ResourceLocation resourceLocationIn) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            try {
                this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), this.resourceManager, this.mc.getFramebuffer(), resourceLocationIn);
                this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
                this.useShader = true;
            } catch (final IOException | JsonSyntaxException ioexception) {
                logger.warn("Failed to load shader: " + resourceLocationIn, ioexception);
                this.shaderIndex = shaderCount;
                this.useShader = false;
            }
        }
    }

    public void onResourceManagerReload(final IResourceManager resourceManager) {
        if (this.theShaderGroup != null) {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;

        if (this.shaderIndex != shaderCount) {
            this.loadShader(shaderResourceLocations[this.shaderIndex]);
        } else {
            this.loadEntityShader(this.mc.getRenderViewEntity());
        }
    }

    /**
     * Updates the entity renderer
     */
    public void updateRenderer() {
        if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
        }

        this.updateFovModifierHand();
        this.updateTorchFlicker();
        this.fogColor2 = this.fogColor1;
        this.thirdPersonDistanceTemp = this.thirdPersonDistance;

        if (this.mc.gameSettings.smoothCamera) {
            final float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            final float f1 = f * f * f * 8.0F;
            this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * f1);
            this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * f1);
            this.smoothCamPartialTicks = 0.0F;
            this.smoothCamYaw = 0.0F;
            this.smoothCamPitch = 0.0F;
        } else {
            this.smoothCamFilterX = 0.0F;
            this.smoothCamFilterY = 0.0F;
            this.mouseFilterXAxis.reset();
            this.mouseFilterYAxis.reset();
        }

        if (this.mc.getRenderViewEntity() == null) {
            this.mc.setRenderViewEntity(this.mc.thePlayer);
        }

        final Entity entity = this.mc.getRenderViewEntity();
        final double d2 = entity.posX;
        final double d0 = entity.posY + (double) entity.getEyeHeight();
        final double d1 = entity.posZ;
        final float f2 = this.mc.theWorld.getLightBrightness(new BlockPos(d2, d0, d1));
        float f3 = (float) this.mc.gameSettings.renderDistanceChunks / 16.0F;
        f3 = MathHelper.clamp_float(f3, 0.0F, 1.0F);
        final float f4 = f2 * (1.0F - f3) + f3;
        this.fogColor1 += (f4 - this.fogColor1) * 0.1F;
        ++this.rendererUpdateCount;
        this.itemRenderer.updateEquippedItem();
        this.addRainParticles();
        this.bossColorModifierPrev = this.bossColorModifier;

        if (BossStatus.hasColorModifier) {
            this.bossColorModifier += 0.05F;

            if (this.bossColorModifier > 1.0F) {
                this.bossColorModifier = 1.0F;
            }

            BossStatus.hasColorModifier = false;
        } else if (this.bossColorModifier > 0.0F) {
            this.bossColorModifier -= 0.0125F;
        }
    }

    public ShaderGroup getShaderGroup() {
        return this.theShaderGroup;
    }

    public void updateShaderGroupSize(final int width, final int height) {
        if (OpenGlHelper.shadersSupported) {
            if (this.theShaderGroup != null) {
                this.theShaderGroup.createBindFramebuffers(width, height);
            }

            this.mc.renderGlobal.createBindEntityOutlineFbs(width, height);
        }
    }

    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    public void getMouseOver(final float partialTicks) {
        final Entity entity = this.mc.getRenderViewEntity();

        if (entity != null && this.mc.theWorld != null) {
            this.mc.mcProfiler.startSection("pick");
            this.mc.pointedEntity = null;
            double blockReachDistance = this.mc.playerController.getBlockReachDistance();
            this.mc.objectMouseOver = entity.rayTrace(blockReachDistance, partialTicks);
            double distance = blockReachDistance;
            final Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            double reach = 3.0D;
            float expand = 0;

            MouseOverEvent mouseOverEvent = new MouseOverEvent(reach, expand);
            Client.INSTANCE.getEventBus().handle(mouseOverEvent);
            reach = mouseOverEvent.getRange();
            expand = mouseOverEvent.getExpand();

            if (this.mc.playerController.extendedReach()) {
                blockReachDistance = 6.0D;
                distance = 6.0D;
            } else if (blockReachDistance > 3.0D) {
                flag = true;
            }

            if (this.mc.objectMouseOver != null) {
                distance = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            final Vec3 vec31 = entity.getLook(partialTicks);
            final Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            final float f = 1.0F;
            final List<Entity> list = this.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = distance;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize() + ((entity instanceof EntityPlayer) ? expand : 0);
                final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    final double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        boolean flag1 = false;

                        if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                            flag1 = Reflector.callBoolean(entity1, Reflector.ForgeEntity_canRiderInteract);
                        }

                        if (!flag1 && entity1 == entity.ridingEntity) {
                            if (d2 == 0.0D) {
                                this.pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            this.pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) > reach) {
                this.pointedEntity = null;
                this.mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (this.pointedEntity != null && (d2 < distance || this.mc.objectMouseOver == null)) {
                this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);

                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }

            this.mc.mcProfiler.endSection();
        }
    }

    /**
     * Update FOV modifier hand
     */
    private void updateFovModifierHand() {
        float f = 1.0F;

        if (this.mc.getRenderViewEntity() instanceof AbstractClientPlayer) {
            final AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer) this.mc.getRenderViewEntity();
            f = abstractclientplayer.getFovModifier();
        }

        this.fovModifierHandPrev = this.fovModifierHand;
        this.fovModifierHand += (f - this.fovModifierHand) * 0.5F;

        if (this.fovModifierHand > 1.5F) {
            this.fovModifierHand = 1.5F;
        }

        if (this.fovModifierHand < 0.1F) {
            this.fovModifierHand = 0.1F;
        }
    }

    /**
     * Changes the field of view of the player depending on if they are underwater or not
     */
    private float getFOVModifier(final float partialTicks, final boolean p_78481_2_) {
        if (this.debugView) {
            return 90.0F;
        } else {
            final Entity entity = this.mc.getRenderViewEntity();
            float f = 70.0F;

            if (p_78481_2_) {
                f = this.mc.gameSettings.fovSetting;

                if (Config.isDynamicFov()) {
                    f *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * partialTicks;
                }
            }

            boolean flag = false;

            if (this.mc.currentScreen == null) {
                flag = GameSettings.isKeyDown(this.mc.gameSettings.ofKeyBindZoom);
            }

            if (flag) {
                if (!Config.zoomMode) {
                    Config.zoomMode = true;
                    this.mc.gameSettings.smoothCamera = true;
                    this.mc.renderGlobal.displayListEntitiesDirty = true;
                }

                f /= 4.0F;
            } else if (Config.zoomMode) {
                Config.zoomMode = false;
                this.mc.gameSettings.smoothCamera = false;
                this.mouseFilterXAxis = new MouseFilter();
                this.mouseFilterYAxis = new MouseFilter();
                this.mc.renderGlobal.displayListEntitiesDirty = true;
            }

            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHealth() <= 0.0F) {
                final float f1 = (float) ((EntityLivingBase) entity).deathTime + partialTicks;
                f /= (1.0F - 500.0F / (f1 + 500.0F)) * 2.0F + 1.0F;
            }

            final Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);

            if (block.getMaterial() == Material.water) {
                f = f * 60.0F / 70.0F;
            }

            return Reflector.ForgeHooksClient_getFOVModifier.exists() ? Reflector.callFloat(Reflector.ForgeHooksClient_getFOVModifier, this, entity, block, partialTicks, f) : f;
        }
    }

    private void hurtCameraEffect(final float partialTicks) {
        if (this.mc.getRenderViewEntity() instanceof EntityLivingBase) {
            final EntityLivingBase entitylivingbase = (EntityLivingBase) this.mc.getRenderViewEntity();
            float f = (float) entitylivingbase.hurtTime - partialTicks;

            if (entitylivingbase.getHealth() <= 0.0F) {
                final float f1 = (float) entitylivingbase.deathTime + partialTicks;
                GlStateManager.rotate(40.0F - 8000.0F / (f1 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (f < 0.0F) {
                return;
            }

            f = f / (float) entitylivingbase.maxHurtTime;
            f = MathHelper.sin(f * f * f * f * (float) Math.PI);
            final HurtCamera hurtCamera = Client.INSTANCE.getModuleManager().get(HurtCamera.class);

            final float f2 = hurtCamera.isEnabled() ? 0 : entitylivingbase.attackedAtYaw;
            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-f * 14.0F, 0.0F, 0.0F, hurtCamera.isEnabled() ? hurtCamera.intensity.getValue().floatValue() : 1.0F);
            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    /**
     * Setups all the GL settings for view bobbing. Args: partialTickTime
     */
    private void setupViewBobbing(final float partialTicks) {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            final EntityPlayer entityplayer = (EntityPlayer) this.mc.getRenderViewEntity();
            final float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            final float f1 = -(entityplayer.distanceWalkedModified + f * partialTicks);
            final float f2 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            final float f3 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
            GlStateManager.translate(MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F, -Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2), 0.0F);
            GlStateManager.rotate(MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
        }
    }

    /**
     * sets up player's eye (or camera in third person mode)
     */
    public void orientCamera(final float partialTicks) {
        final Entity entity = this.mc.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            f = (float) ((double) f + 1.0D);
            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if (!this.mc.gameSettings.debugCamEnable) {
                final BlockPos blockpos = new BlockPos(entity);
                final IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                final Block block = iblockstate.getBlock();

                if (Reflector.ForgeHooksClient_orientBedCamera.exists()) {
                    Reflector.callVoid(Reflector.ForgeHooksClient_orientBedCamera, this.mc.theWorld, blockpos, iblockstate, entity);
                } else if (block == Blocks.bed) {
                    final int j = iblockstate.getValue(BlockBed.FACING).getHorizontalIndex();
                    GlStateManager.rotate((float) (j * 90), 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        } else if (this.mc.gameSettings.thirdPersonView > 0) {
            double d3 = this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTicks;

            if (this.mc.gameSettings.debugCamEnable) {
                GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
            } else {
                final float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;

                if (this.mc.gameSettings.thirdPersonView == 2) {
                    f2 += Client.INSTANCE.getConstantManager().O;
                }

                Module noCameraClip = Client.INSTANCE.getModuleManager().get(NoCameraClip.class);

                if (noCameraClip == null || !noCameraClip.isEnabled()) {
                    final double d4 = (double) (-MathHelper.sin(f1 / 180.0F * (float) Math.PI) * MathHelper.cos(f2 / 180.0F * (float) Math.PI)) * d3;
                    final double d5 = (double) (MathHelper.cos(f1 / 180.0F * (float) Math.PI) * MathHelper.cos(f2 / 180.0F * (float) Math.PI)) * d3;
                    final double d6 = (double) (-MathHelper.sin(f2 / 180.0F * (float) Math.PI)) * d3;

                    for (int i = 0; i < 8; ++i) {
                        float f3 = (float) ((i & 1) * 2 - 1);
                        float f4 = (float) ((i >> 1 & 1) * 2 - 1);
                        float f5 = (float) ((i >> 2 & 1) * 2 - 1);
                        f3 = f3 * 0.1F;
                        f4 = f4 * 0.1F;
                        f5 = f5 * 0.1F;
                        final MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double) f3, d1 + (double) f4, d2 + (double) f5), new Vec3(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5));

                        if (movingobjectposition != null) {
                            final double d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2));

                            if (d7 < d3) {
                                d3 = d7;
                            }
                        }
                    }
                }

                if (this.mc.gameSettings.thirdPersonView == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        } else {
            GlStateManager.translate(0.0F, 0.0F, -0.1F);
        }

        if (Reflector.EntityViewRenderEvent_CameraSetup_Constructor.exists()) {
            if (!this.mc.gameSettings.debugCamEnable) {
                float f6 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
                float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float f8 = 0.0F;

                if (entity instanceof EntityAnimal) {
                    final EntityAnimal entityanimal1 = (EntityAnimal) entity;
                    f6 = entityanimal1.prevRotationYawHead + (entityanimal1.rotationYawHead - entityanimal1.prevRotationYawHead) * partialTicks + 180.0F;
                }

                final Block block1 = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
                final Object object = Reflector.newInstance(Reflector.EntityViewRenderEvent_CameraSetup_Constructor, this, entity, block1, partialTicks, f6, f7, f8);
                Reflector.postForgeBusEvent(object);
                f8 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_roll, f8);
                f7 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_pitch, f7);
                f6 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_yaw, f6);
                GlStateManager.rotate(f8, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(f7, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(f6, 0.0F, 1.0F, 0.0F);
            }
        } else if (!this.mc.gameSettings.debugCamEnable) {
            FreeLook freeLook = getModule(FreeLook.class);

            if (freeLook != null && freeLook.isEnabled()) {
                GlStateManager.rotate(freeLook.lastPitch * (freeLook.invertPitch.getValue() ? -1 : 1), 1.0F, 0.0F, 0.0F);
            } else {
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);
            }

            if (entity instanceof EntityAnimal) {
                final EntityAnimal entityanimal = (EntityAnimal) entity;
                GlStateManager.rotate(entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            } else if (freeLook != null && freeLook.isEnabled()) {
                GlStateManager.rotate(freeLook.lastYaw + 180.0F, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            }
        }

        GlStateManager.translate(0.0F, -f, 0.0F);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

    /**
     * sets up projection, view effects, camera position/rotation
     */
    public void setupCameraTransform(final float partialTicks, final int pass) {
        this.farPlaneDistance = (float) (this.mc.gameSettings.renderDistanceChunks * 16);

        if (Config.isFogFancy()) {
            this.farPlaneDistance *= 0.95F;
        }

        if (Config.isFogFast()) {
            this.farPlaneDistance *= 0.83F;
        }

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        final float f = 0.07F;

        if (this.mc.gameSettings.anaglyph) {
            GlStateManager.translate((float) (-(pass * 2 - 1)) * f, 0.0F, 0.0F);
        }

        this.clipDistance = this.farPlaneDistance * 2.0F;

        if (this.clipDistance < 173.0F) {
            this.clipDistance = 173.0F;
        }

        Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.clipDistance);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();

        if (this.mc.gameSettings.anaglyph) {
            GlStateManager.translate((float) (pass * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        this.hurtCameraEffect(partialTicks);

        final ViewBobbingEvent viewBobbingEvent = new ViewBobbingEvent(0);
        if (this.mc.gameSettings.viewBobbing && !viewBobbingEvent.isCancelled()) {
            this.setupViewBobbing(partialTicks);
        }

        final float f1 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTicks;

        if (f1 > 0.0F) {
            int i = 20;

            if (this.mc.thePlayer.isPotionActive(Potion.confusion)) {
                i = 7;
            }

            float f2 = 5.0F / (f1 * f1 + 5.0F) - f1 * 0.04F;
            f2 = f2 * f2;
            GlStateManager.rotate(((float) this.rendererUpdateCount + partialTicks) * (float) i, 0.0F, 1.0F, 1.0F);
            GlStateManager.scale(1.0F / f2, 1.0F, 1.0F);
            GlStateManager.rotate(-((float) this.rendererUpdateCount + partialTicks) * (float) i, 0.0F, 1.0F, 1.0F);
        }

        this.orientCamera(partialTicks);

        if (this.debugView) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        }
    }

    /**
     * Render player hand
     *
     * @param partialTicks The amount of time passed during the current tick, ranging from 0 to 1.
     */
    private void renderHand(final float partialTicks, final int xOffset) {
        this.renderHand(partialTicks, xOffset, true, true, false);
    }

    public void renderHand(final float p_renderHand_1_, final int p_renderHand_2_, final boolean p_renderHand_3_, final boolean p_renderHand_4_, final boolean p_renderHand_5_) {
        if (!this.debugView) {
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            final float f = 0.07F;

            if (this.mc.gameSettings.anaglyph) {
                GlStateManager.translate((float) (-(p_renderHand_2_ * 2 - 1)) * f, 0.0F, 0.0F);
            }

            if (Config.isShaders()) {
                Shaders.applyHandDepth();
            }

            Project.gluPerspective(this.getFOVModifier(p_renderHand_1_, false), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();

            if (this.mc.gameSettings.anaglyph) {
                GlStateManager.translate((float) (p_renderHand_2_ * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            boolean flag = false;

            if (p_renderHand_3_) {
                GlStateManager.pushMatrix();
                this.hurtCameraEffect(p_renderHand_1_);

                final ViewBobbingEvent viewBobbingEvent = new ViewBobbingEvent(1);

                if (this.mc.gameSettings.viewBobbing && !viewBobbingEvent.isCancelled()) {
                    this.setupViewBobbing(p_renderHand_1_);
                }

                flag = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) this.mc.getRenderViewEntity()).isPlayerSleeping();
                final boolean flag1 = !ReflectorForge.renderFirstPersonHand(this.mc.renderGlobal, p_renderHand_1_, p_renderHand_2_);

                if (flag1 && this.mc.gameSettings.thirdPersonView == 0 && !flag && !this.mc.gameSettings.hideGUI && !this.mc.playerController.isSpectator()) {
                    this.enableLightmap();

                    if (Config.isShaders()) {
                        ShadersRender.renderItemFP(this.itemRenderer, p_renderHand_1_, p_renderHand_5_);
                    } else {
                        this.itemRenderer.renderItemInFirstPerson(p_renderHand_1_);
                    }

                    this.disableLightmap();
                }

                GlStateManager.popMatrix();
            }

            if (!p_renderHand_4_) {
                return;
            }

            this.disableLightmap();

            if (this.mc.gameSettings.thirdPersonView == 0 && !flag) {
                this.itemRenderer.renderOverlays(p_renderHand_1_);
                this.hurtCameraEffect(p_renderHand_1_);
            }

            final ViewBobbingEvent viewBobbingEvent = new ViewBobbingEvent(2);
            if (this.mc.gameSettings.viewBobbing && !viewBobbingEvent.isCancelled()) {
                this.setupViewBobbing(p_renderHand_1_);
            }
        }
    }

    public void disableLightmap() {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders()) {
            Shaders.disableLightmap();
        }
    }

    public void enableLightmap() {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        final float f = 0.00390625F;
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.matrixMode(5888);
        this.mc.getTextureManager().bindTexture(this.locationLightMap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders()) {
            Shaders.enableLightmap();
        }
    }

    /**
     * Recompute a random value that is applied to block color in updateLightmap()
     */
    private void updateTorchFlicker() {
        this.torchFlickerDX = (float) ((double) this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
        this.torchFlickerDX = (float) ((double) this.torchFlickerDX * 0.9D);
        this.torchFlickerX += (this.torchFlickerDX - this.torchFlickerX);
        this.lightmapUpdateNeeded = true;
    }

    private void updateLightmap(final float partialTicks) {
        if (this.lightmapUpdateNeeded) {
            this.mc.mcProfiler.startSection("lightTex");
            final World world = this.mc.theWorld;

            if (world != null) {
                if (Config.isCustomColors() && CustomColors.updateLightmap(world, this.torchFlickerX, this.lightmapColors, this.mc.thePlayer.isPotionActive(Potion.nightVision), partialTicks)) {
                    this.lightmapTexture.updateDynamicTexture();
                    this.lightmapUpdateNeeded = false;
                    this.mc.mcProfiler.endSection();
                    return;
                }

                final float f = world.getSunBrightness(1.0F);
                final float f1 = f * 0.95F + 0.05F;

                for (int i = 0; i < 256; ++i) {
                    float f2 = world.provider.getLightBrightnessTable()[i / 16] * f1;
                    final float f3 = world.provider.getLightBrightnessTable()[i % 16] * (this.torchFlickerX * 0.1F + 1.5F);

                    if (world.getLastLightningBolt() > 0) {
                        f2 = world.provider.getLightBrightnessTable()[i / 16];
                    }

                    final float f4 = f2 * (f * 0.65F + 0.35F);
                    final float f5 = f2 * (f * 0.65F + 0.35F);
                    final float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
                    final float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
                    float f8 = f4 + f3;
                    float f9 = f5 + f6;
                    float f10 = f2 + f7;
                    f8 = f8 * 0.96F + 0.03F;
                    f9 = f9 * 0.96F + 0.03F;
                    f10 = f10 * 0.96F + 0.03F;

                    if (this.bossColorModifier > 0.0F) {
                        final float f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
                        f8 = f8 * (1.0F - f11) + f8 * 0.7F * f11;
                        f9 = f9 * (1.0F - f11) + f9 * 0.6F * f11;
                        f10 = f10 * (1.0F - f11) + f10 * 0.6F * f11;
                    }

                    if (world.provider.getDimensionId() == 1) {
                        f8 = 0.22F + f3 * 0.75F;
                        f9 = 0.28F + f6 * 0.75F;
                        f10 = 0.25F + f7 * 0.75F;
                    }

                    if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
                        final float f15 = this.getNightVisionBrightness(this.mc.thePlayer, partialTicks);
                        float f12 = 1.0F / f8;

                        if (f12 > 1.0F / f9) {
                            f12 = 1.0F / f9;
                        }

                        if (f12 > 1.0F / f10) {
                            f12 = 1.0F / f10;
                        }

                        f8 = f8 * (1.0F - f15) + f8 * f12 * f15;
                        f9 = f9 * (1.0F - f15) + f9 * f12 * f15;
                        f10 = f10 * (1.0F - f15) + f10 * f12 * f15;
                    }

                    if (f8 > 1.0F) {
                        f8 = 1.0F;
                    }

                    if (f9 > 1.0F) {
                        f9 = 1.0F;
                    }

                    if (f10 > 1.0F) {
                        f10 = 1.0F;
                    }

                    final float f16 = this.mc.gameSettings.gammaSetting;
                    float f17 = 1.0F - f8;
                    float f13 = 1.0F - f9;
                    float f14 = 1.0F - f10;
                    f17 = 1.0F - f17 * f17 * f17 * f17;
                    f13 = 1.0F - f13 * f13 * f13 * f13;
                    f14 = 1.0F - f14 * f14 * f14 * f14;
                    f8 = f8 * (1.0F - f16) + f17 * f16;
                    f9 = f9 * (1.0F - f16) + f13 * f16;
                    f10 = f10 * (1.0F - f16) + f14 * f16;
                    f8 = f8 * 0.96F + 0.03F;
                    f9 = f9 * 0.96F + 0.03F;
                    f10 = f10 * 0.96F + 0.03F;

                    if (f8 > 1.0F) {
                        f8 = 1.0F;
                    }

                    if (f9 > 1.0F) {
                        f9 = 1.0F;
                    }

                    if (f10 > 1.0F) {
                        f10 = 1.0F;
                    }

                    if (f8 < 0.0F) {
                        f8 = 0.0F;
                    }

                    if (f9 < 0.0F) {
                        f9 = 0.0F;
                    }

                    if (f10 < 0.0F) {
                        f10 = 0.0F;
                    }

                    final int j = 255;
                    final int k = (int) (f8 * 255.0F);
                    final int l = (int) (f9 * 255.0F);
                    final int i1 = (int) (f10 * 255.0F);
                    this.lightmapColors[i] = j << 24 | k << 16 | l << 8 | i1;
                }

                this.lightmapTexture.updateDynamicTexture();
                this.lightmapUpdateNeeded = false;
                this.mc.mcProfiler.endSection();
            }
        }
    }

    public float getNightVisionBrightness(final EntityLivingBase entitylivingbaseIn, final float partialTicks) {
        final int i = entitylivingbaseIn.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float) i - partialTicks) * (float) Math.PI * 0.2F) * 0.3F;
    }

    public void func_181560_a(final float partialTicks, final long p_181560_2_) {
        Config.renderPartialTicks = partialTicks;
        this.frameInit();
        final boolean flag = Display.isActive();

        if (!flag && this.mc.gameSettings.pauseOnLostFocus && (!this.mc.gameSettings.touchscreen || !Mouse.isButtonDown(1))) {
            if (Minecraft.getSystemTime() - this.prevFrameTime > 500L) {
                this.mc.displayInGameMenu();
            }
        } else {
            this.prevFrameTime = Minecraft.getSystemTime();
        }

        this.mc.mcProfiler.startSection("mouse");

        if (flag && Minecraft.isRunningOnMac && this.mc.inGameHasFocus && !Mouse.isInsideWindow()) {
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
            Mouse.setGrabbed(true);
        }

        FreeLook freeLook = getModule(FreeLook.class);

        if (this.mc.inGameHasFocus && flag && (freeLook != null && !freeLook.isEnabled())) {
            this.mc.mouseHelper.mouseXYChange();
            final float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            final float f1 = f * f * f * 8.0F;
            float f2 = (int) (this.mc.mouseHelper.deltaX + mouseAddedX) * f1;
            float f3 = (int) (this.mc.mouseHelper.deltaY + mouseAddedY) * f1;
            int i = 1;

            EntityRenderer.mouseAddedX = EntityRenderer.mouseAddedY = 0;

            if (this.mc.gameSettings.invertMouse) {
                i = -1;
            }

            if (this.mc.gameSettings.smoothCamera) {
                this.smoothCamYaw += f2;
                this.smoothCamPitch += f3;
                final float f4 = partialTicks - this.smoothCamPartialTicks;
                this.smoothCamPartialTicks = partialTicks;
                f2 = this.smoothCamFilterX * f4;
                f3 = this.smoothCamFilterY * f4;
            } else {
                this.smoothCamYaw = 0.0F;
                this.smoothCamPitch = 0.0F;
            }
            this.mc.thePlayer.setAngles(f2, f3 * (float) i);
        }

        this.mc.mcProfiler.endSection();

        if (!this.mc.skipRenderWorld) {
            anaglyphEnable = this.mc.gameSettings.anaglyph;
            final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            final int i1 = scaledresolution.getScaledWidth();
            final int j1 = scaledresolution.getScaledHeight();
            final int k1 = Mouse.getX() * i1 / this.mc.displayWidth;
            final int l1 = j1 - Mouse.getY() * j1 / this.mc.displayHeight - 1;
            final int i2 = this.mc.gameSettings.limitFramerate;

            if (this.mc.theWorld != null) {
                this.mc.mcProfiler.startSection("level");
                int j = Math.min(Minecraft.getDebugFPS(), i2);
                j = Math.max(j, 60);
                final long k = System.nanoTime() - p_181560_2_;
                final long l = Math.max((long) (1000000000 / j / 4) - k, 0L);
                this.renderWorld(partialTicks, System.nanoTime() + l);

                if (OpenGlHelper.shadersSupported) {
                    this.mc.renderGlobal.renderEntityOutlineFramebuffer();

                    if (this.theShaderGroup != null && this.useShader) {
                        GlStateManager.matrixMode(5890);
                        GlStateManager.pushMatrix();
                        GlStateManager.loadIdentity();
                        this.theShaderGroup.loadShaderGroup(partialTicks);
                        GlStateManager.popMatrix();
                    }

                    this.mc.getFramebuffer().bindFramebuffer(true);
                }

                this.renderEndNanoTime = System.nanoTime();
                this.mc.mcProfiler.endStartSection("gui");

                this.framebuffer = RenderUtil.createFrameBuffer(this.framebuffer);
                mc.ingameGUI.frame++;
                if (!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null) {
                    this.mc.entityRenderer.setupOverlayRendering();
                    GlStateManager.alphaFunc(516, 0.1F);
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

//                    this.mc.ingameGUI.renderGameOverlay(partialTicks);

                    GlStateManager.pushMatrix();
                    Client.INSTANCE.getEventBus().handle(new Render2DEvent(scaledresolution, partialTicks));
                    GlStateManager.popMatrix();

                    Interface interfaceModule = getModule(Interface.class);
                    boolean shaders = interfaceModule == null || interfaceModule.shaders.getValue();

                    InstanceAccess.render2DRunnables(partialTicks, shaders);
                    InstanceAccess.clearRunnables();

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableLighting();
                    GlStateManager.enableAlpha();

                    this.mc.getFramebuffer().bindFramebuffer(false);
                    this.framebuffer.bindFramebufferTexture();
                    ShaderUtil.drawQuads(scaledresolution);
                    GlStateManager.bindTexture(0);

                    if (this.mc.gameSettings.ofShowFps && !this.mc.gameSettings.showDebugInfo) {
                        Config.drawFps();
                    }

                    if (this.mc.gameSettings.showDebugInfo) {
                        Lagometer.showLagometer(scaledresolution);
                    }
                }

                this.mc.mcProfiler.endSection();
            } else {
                GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                this.setupOverlayRendering();
                this.renderEndNanoTime = System.nanoTime();
                TileEntityRendererDispatcher.instance.renderEngine = this.mc.getTextureManager();
                TileEntityRendererDispatcher.instance.fontRenderer = this.mc.fontRendererObj;
            }

            if (this.mc.currentScreen != null) {
                GlStateManager.clear(256);

                try {
                    if (Reflector.ForgeHooksClient_drawScreen.exists()) {
                        Reflector.callVoid(Reflector.ForgeHooksClient_drawScreen, this.mc.currentScreen, k1, l1, partialTicks);
                    } else {
                        this.mc.currentScreen.drawScreen(k1, l1, partialTicks);
                    }
                } catch (final Throwable throwable) {
                    final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
                    final CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
                    crashreportcategory.addCrashSectionCallable("Screen name", () -> EntityRenderer.this.mc.currentScreen.getClass().getCanonicalName());
                    crashreportcategory.addCrashSectionCallable("Mouse location", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d)", k1, l1, Mouse.getX(), Mouse.getY()));
                    crashreportcategory.addCrashSectionCallable("Screen size", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), EntityRenderer.this.mc.displayWidth, EntityRenderer.this.mc.displayHeight, scaledresolution.getScaleFactor()));
                    throw new ReportedException(crashreport);
                }
            }
        }

        this.frameFinish();
        this.waitForServerThread();
        Lagometer.updateLagometer();

        if (this.mc.gameSettings.ofProfiler) {
            this.mc.gameSettings.showDebugProfilerChart = true;
        }
    }

    private boolean isDrawBlockOutline() {
        if (!this.drawBlockOutline) {
            return false;
        } else {
            final Entity entity = this.mc.getRenderViewEntity();
            boolean flag = entity instanceof EntityPlayer && !this.mc.gameSettings.hideGUI;

            if (flag && !((EntityPlayer) entity).capabilities.allowEdit) {
                final ItemStack itemstack = ((EntityPlayer) entity).getCurrentEquippedItem();

                if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    final BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
                    final IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                    final Block block = iblockstate.getBlock();

                    if (this.mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR) {
                        flag = ReflectorForge.blockHasTileEntity(iblockstate) && this.mc.theWorld.getTileEntity(blockpos) instanceof IInventory;
                    } else {
                        flag = itemstack != null && (itemstack.canDestroy(block) || itemstack.canPlaceOn(block));
                    }
                }
            }

            return flag;
        }
    }

    private void renderWorldDirections(final float partialTicks) {
        if (this.mc.gameSettings.showDebugInfo && !this.mc.gameSettings.hideGUI && !this.mc.thePlayer.hasReducedDebug() && !this.mc.gameSettings.reducedDebugInfo) {
            final Entity entity = this.mc.getRenderViewEntity();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            this.orientCamera(partialTicks);
            GlStateManager.translate(0.0F, entity.getEyeHeight(), 0.0F);
            RenderGlobal.func_181563_a(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.005D, 1.0E-4D, 1.0E-4D), 255, 0, 0, 255);
            RenderGlobal.func_181563_a(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 1.0E-4D, 0.005D), 0, 0, 255, 255);
            RenderGlobal.func_181563_a(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 0.0033D, 1.0E-4D), 0, 255, 0, 255);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    public void renderWorld(final float partialTicks, final long finishTimeNano) {
        this.updateLightmap(partialTicks);

        if (this.mc.getRenderViewEntity() == null) {
            this.mc.setRenderViewEntity(this.mc.thePlayer);
        }

        this.getMouseOver(partialTicks);

        if (Config.isShaders()) {
            Shaders.beginRender(this.mc, partialTicks, finishTimeNano);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        this.mc.mcProfiler.startSection("center");

        if (this.mc.gameSettings.anaglyph) {
            anaglyphField = 0;
            GlStateManager.colorMask(false, true, true, false);
            this.renderWorldPass(0, partialTicks, finishTimeNano);
            anaglyphField = 1;
            GlStateManager.colorMask(true, false, false, false);
            this.renderWorldPass(1, partialTicks, finishTimeNano);
            GlStateManager.colorMask(true, true, true, false);
        } else {
            this.renderWorldPass(2, partialTicks, finishTimeNano);
        }

        this.mc.mcProfiler.endSection();
    }

    private void renderWorldPass(final int pass, final float partialTicks, final long finishTimeNano) {
        final boolean flag = Config.isShaders();

        if (flag) {
            Shaders.beginRenderPass(pass, partialTicks, finishTimeNano);
        }

        final RenderGlobal renderglobal = this.mc.renderGlobal;
        final EffectRenderer effectrenderer = this.mc.effectRenderer;
        final boolean flag1 = this.isDrawBlockOutline();
        GlStateManager.enableCull();
        this.mc.mcProfiler.endStartSection("clear");

        if (flag) {
            Shaders.setViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        } else {
            GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        }

        this.updateFogColor(partialTicks);
        GlStateManager.clear(16640);

        if (flag) {
            Shaders.clearRenderBuffer();
        }

        this.mc.mcProfiler.endStartSection("camera");
        this.setupCameraTransform(partialTicks, pass);

        if (flag) {
            Shaders.setCamera(partialTicks);
        }

        ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
        this.mc.mcProfiler.endStartSection("frustum");
        final ClippingHelper clippinghelper = ClippingHelperImpl.getInstance();
        this.mc.mcProfiler.endStartSection("culling");
        clippinghelper.disabled = Config.isShaders() && !Shaders.isFrustumCulling();

        final ICamera icamera = new Frustum(clippinghelper);
        final Entity entity = this.mc.getRenderViewEntity();
        final double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        final double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks) + entity.getEyeHeight();
        final double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

        if (flag) {
            ShadersRender.setFrustrumPosition(icamera, d0, d1, d2);
        } else {
            icamera.setPosition(d0, d1, d2);
        }

        if ((Config.isSkyEnabled() || Config.isSunMoonEnabled() || Config.isStarsEnabled()) && !Shaders.isShadowPass) {
            this.setupFog(-1, partialTicks);
            this.mc.mcProfiler.endStartSection("sky");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.matrixMode(5888);

            if (flag) {
                Shaders.beginSky();
            }

            renderglobal.renderSky(partialTicks, pass);

            if (flag) {
                Shaders.endSky();
            }

            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.matrixMode(5888);
        } else {
            GlStateManager.disableBlend();
        }

        this.setupFog(0, partialTicks);
        GlStateManager.shadeModel(7425);

        if (entity.posY + (double) entity.getEyeHeight() < 128.0D + (double) (this.mc.gameSettings.ofCloudsHeight * 128.0F)) {
            this.renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        this.mc.mcProfiler.endStartSection("prepareterrain");
        this.setupFog(0, partialTicks);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();
        this.mc.mcProfiler.endStartSection("terrain_setup");
        this.checkLoadVisibleChunks(entity, partialTicks, icamera, this.mc.thePlayer.isSpectator());

        if (flag) {
            ShadersRender.setupTerrain(renderglobal, entity, partialTicks, icamera, this.frameCount++, this.mc.thePlayer.isSpectator());
        } else {
            renderglobal.setupTerrain(entity, partialTicks, icamera, this.frameCount++, this.mc.thePlayer.isSpectator());
        }

        if (pass == 2 || pass == 0) {
            this.mc.mcProfiler.endStartSection("updatechunks");
            Lagometer.timerChunkUpload.start();
            this.mc.renderGlobal.updateChunks(finishTimeNano);
            Lagometer.timerChunkUpload.end();
        }

        this.mc.mcProfiler.endStartSection("terrain");
        Lagometer.timerTerrain.start();

        if (this.mc.gameSettings.ofSmoothFps && pass > 0) {
            this.mc.mcProfiler.endStartSection("finish");
            GL11.glFinish();
            this.mc.mcProfiler.endStartSection("terrain");
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();

        if (flag) {
            ShadersRender.beginTerrainSolid();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.SOLID, partialTicks, pass, entity);
        GlStateManager.enableAlpha();

        if (flag) {
            ShadersRender.beginTerrainCutoutMipped();
        }

        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, this.mc.gameSettings.mipmapLevels > 0);
        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT_MIPPED, partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        if (flag) {
            ShadersRender.beginTerrainCutout();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT, partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

        if (flag) {
            ShadersRender.endTerrain();
        }

        Lagometer.timerTerrain.end();
        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);

        if (!this.debugView) {
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("entities");

            if (Reflector.ForgeHooksClient_setRenderPass.exists()) {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, 0);
            }

            renderglobal.renderEntities(entity, icamera, partialTicks);

            if (Reflector.ForgeHooksClient_setRenderPass.exists()) {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, -1);
            }

            RenderHelper.disableStandardItemLighting();
            this.disableLightmap();
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();

            if (this.mc.objectMouseOver != null && entity.isInsideOfMaterial(Material.water) && flag1) {
                final EntityPlayer entityplayer = (EntityPlayer) entity;
                GlStateManager.disableAlpha();
                this.mc.mcProfiler.endStartSection("outline");
                renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, partialTicks);
                GlStateManager.enableAlpha();
            }
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();

        if (flag1 && this.mc.objectMouseOver != null && !entity.isInsideOfMaterial(Material.water)) {
            final EntityPlayer entityplayer1 = (EntityPlayer) entity;
            GlStateManager.disableAlpha();
            this.mc.mcProfiler.endStartSection("outline");

            if ((!Reflector.ForgeHooksClient_onDrawBlockHighlight.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, renderglobal, entityplayer1, this.mc.objectMouseOver, 0, entityplayer1.getHeldItem(), partialTicks)) && !this.mc.gameSettings.hideGUI) {
                renderglobal.drawSelectionBox(entityplayer1, this.mc.objectMouseOver, 0, partialTicks);
            }
            GlStateManager.enableAlpha();
        }

        if (!renderglobal.damagedBlocks.isEmpty()) {
            this.mc.mcProfiler.endStartSection("destroyProgress");
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
            renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getWorldRenderer(), entity, partialTicks);
            this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
            GlStateManager.disableBlend();
        }

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableBlend();

        if (!this.debugView) {
            this.enableLightmap();
            this.mc.mcProfiler.endStartSection("litParticles");

            if (flag) {
                Shaders.beginLitParticles();
            }

//            effectrenderer.renderLitParticles(entity, partialTicks);
            RenderHelper.disableStandardItemLighting();
            this.setupFog(0, partialTicks);
            this.mc.mcProfiler.endStartSection("particles");

            if (flag) {
                Shaders.beginParticles();
            }

            effectrenderer.renderParticles(entity, partialTicks);

            if (flag) {
                Shaders.endParticles();
            }

            this.disableLightmap();
        }

        GlStateManager.depthMask(false);

        if (Config.isShaders()) {
            GlStateManager.depthMask(Shaders.isRainDepth());
        }

        GlStateManager.enableCull();
        this.mc.mcProfiler.endStartSection("weather");

        if (flag) {
            Shaders.beginWeather();
        }

        this.renderRainSnow(partialTicks);

        if (flag) {
            Shaders.endWeather();
        }

        GlStateManager.depthMask(true);
        renderglobal.renderWorldBorder(entity, partialTicks);

        if (flag) {
            ShadersRender.renderHand0(this, partialTicks, pass);
            Shaders.preWater();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.alphaFunc(516, 0.1F);
        this.setupFog(0, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GlStateManager.shadeModel(7425);
        this.mc.mcProfiler.endStartSection("translucent");

        if (flag) {
            Shaders.beginWater();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, partialTicks, pass, entity);

        if (flag) {
            Shaders.endWater();
        }

        if (Reflector.ForgeHooksClient_setRenderPass.exists() && !this.debugView) {
            RenderHelper.enableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("entities");
            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, 1);
            this.mc.renderGlobal.renderEntities(entity, icamera, partialTicks);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, -1);
            RenderHelper.disableStandardItemLighting();
        }

        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        if (entity.posY + (double) entity.getEyeHeight() >= 128.0D + (double) (this.mc.gameSettings.ofCloudsHeight * 128.0F)) {
            this.mc.mcProfiler.endStartSection("aboveClouds");
            this.renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        if (Reflector.ForgeHooksClient_dispatchRenderLast.exists()) {
            this.mc.mcProfiler.endStartSection("forge_render_last");
            Reflector.callVoid(Reflector.ForgeHooksClient_dispatchRenderLast, renderglobal, partialTicks);
        }

        this.mc.mcProfiler.endStartSection("hand");

        if (this.renderHand && !Shaders.isShadowPass) {
            if (flag) {
                ShadersRender.renderHand1(this, partialTicks, pass);
                Shaders.renderCompositeFinal();
            }

            GlStateManager.clear(256);

            if (flag) {
                ShadersRender.renderFPOverlay(this, partialTicks, pass);
            } else {
                this.renderHand(partialTicks, pass);
            }

            this.renderWorldDirections(partialTicks);
        }

        if (flag) {
            Shaders.endRender();
        }

        // I need this here, else there will be bugs
        setupCameraTransform(partialTicks, 0);
        Client.INSTANCE.getEventBus().handle(new Render3DEvent(partialTicks));

        RendererLivingEntity.SHADER_RENDERING = true;
        InstanceAccess.render3DRunnables(partialTicks);
        InstanceAccess.clearRunnables();
        RendererLivingEntity.SHADER_RENDERING = false;
    }

    private void renderCloudsCheck(final RenderGlobal renderGlobalIn, final float partialTicks, final int pass) {
        if (this.mc.gameSettings.renderDistanceChunks >= 4 && !Config.isCloudsOff() && Shaders.shouldRenderClouds(this.mc.gameSettings)) {
            this.mc.mcProfiler.endStartSection("clouds");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.clipDistance * 4.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.pushMatrix();
            this.setupFog(0, partialTicks);
            renderGlobalIn.renderClouds(partialTicks, pass);
            GlStateManager.disableFog();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.matrixMode(5888);
        }
    }

    private void addRainParticles() {
        float f = this.mc.theWorld.getRainStrength(1.0F);

        Ambience ambience = Client.INSTANCE.getModuleManager().get(Ambience.class);
        if (ambience != null && ambience.skipRainParticles()) return;

        if (!Config.isRainFancy()) {
            f /= 2.0F;
        }

        if (f != 0.0F && Config.isRainSplash()) {
            this.random.setSeed((long) this.rendererUpdateCount * 312987231L);
            final Entity entity = this.mc.getRenderViewEntity();
            final World world = this.mc.theWorld;
            final BlockPos blockpos = new BlockPos(entity);
            final int i = 10;
            double d0 = 0.0D;
            double d1 = 0.0D;
            double d2 = 0.0D;
            int j = 0;
            int k = (int) (100.0F * f * f);

            if (this.mc.gameSettings.particleSetting == 1) {
                k >>= 1;
            } else if (this.mc.gameSettings.particleSetting == 2) {
                k = 0;
            }

            for (int l = 0; l < k; ++l) {
                final BlockPos blockpos1 = world.getPrecipitationHeight(blockpos.add(this.random.nextInt(i) - this.random.nextInt(i), 0, this.random.nextInt(i) - this.random.nextInt(i)));
                final BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos1);
                final BlockPos blockpos2 = blockpos1.down();
                final Block block = world.getBlockState(blockpos2).getBlock();

                if (blockpos1.getY() <= blockpos.getY() + i && blockpos1.getY() >= blockpos.getY() - i && biomegenbase.canSpawnLightningBolt() && biomegenbase.getFloatTemperature(blockpos1) >= 0.15F) {
                    final double d3 = this.random.nextDouble();
                    final double d4 = this.random.nextDouble();

                    if (block.getMaterial() == Material.lava) {
                        this.mc.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double) blockpos1.getX() + d3, (double) ((float) blockpos1.getY() + 0.1F) - block.getBlockBoundsMinY(), (double) blockpos1.getZ() + d4, 0.0D, 0.0D, 0.0D);
                    } else if (block.getMaterial() != Material.air) {
                        block.setBlockBoundsBasedOnState(world, blockpos2);
                        ++j;

                        if (this.random.nextInt(j) == 0) {
                            d0 = (double) blockpos2.getX() + d3;
                            d1 = (double) ((float) blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY() - 1.0D;
                            d2 = (double) blockpos2.getZ() + d4;
                        }

                        this.mc.theWorld.spawnParticle(EnumParticleTypes.WATER_DROP, (double) blockpos2.getX() + d3, (double) ((float) blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY(), (double) blockpos2.getZ() + d4, 0.0D, 0.0D, 0.0D);
                    }
                }
            }

            if (j > 0 && this.random.nextInt(3) < this.rainSoundCounter++) {
                this.rainSoundCounter = 0;

                if (d1 > (double) (blockpos.getY() + 1) && world.getPrecipitationHeight(blockpos).getY() > MathHelper.floor_float((float) blockpos.getY())) {
                    this.mc.theWorld.playSound(d0, d1, d2, "ambient.weather.rain", 0.1F, 0.5F, false);
                } else {
                    this.mc.theWorld.playSound(d0, d1, d2, "ambient.weather.rain", 0.2F, 1.0F, false);
                }
            }
        }
    }

    /**
     * Render rain and snow
     */
    protected void renderRainSnow(final float partialTicks) {
        if (Reflector.ForgeWorldProvider_getWeatherRenderer.exists()) {
            final WorldProvider worldprovider = this.mc.theWorld.provider;
            final Object object = Reflector.call(worldprovider, Reflector.ForgeWorldProvider_getWeatherRenderer);

            if (object != null) {
                Reflector.callVoid(object, Reflector.IRenderHandler_render, partialTicks, this.mc.theWorld, this.mc);
                return;
            }
        }

        final float f5 = this.mc.theWorld.getRainStrength(partialTicks);

        if (f5 > 0.0F) {
            if (Config.isRainOff()) {
                return;
            }

            this.enableLightmap();
            final Entity entity = this.mc.getRenderViewEntity();
            final World world = this.mc.theWorld;
            final int i = MathHelper.floor_double(entity.posX);
            final int j = MathHelper.floor_double(entity.posY);
            final int k = MathHelper.floor_double(entity.posZ);
            final Tessellator tessellator = Tessellator.getInstance();
            final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.disableCull();
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.alphaFunc(516, 0.1F);
            final double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
            final double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks) + entity.getEyeHeight();
            final double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
            final int l = MathHelper.floor_double(d1);
            int i1 = 5;

            if (Config.isRainFancy()) {
                i1 = 10;
            }

            int j1 = -1;
            final float f = (float) this.rendererUpdateCount + partialTicks;
            worldrenderer.setTranslation(-d0, -d1, -d2);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k1 = k - i1; k1 <= k + i1; ++k1) {
                for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                    final int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
                    final double d3 = (double) this.rainXCoords[i2] * 0.5D;
                    final double d4 = (double) this.rainYCoords[i2] * 0.5D;
                    blockpos$mutableblockpos.func_181079_c(l1, 0, k1);
                    final BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos$mutableblockpos);

                    if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()) {
                        final int j2 = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                        int k2 = j - i1;
                        int l2 = j + i1;

                        if (k2 < j2) {
                            k2 = j2;
                        }

                        if (l2 < j2) {
                            l2 = j2;
                        }

                        final int i3 = Math.max(j2, l);

                        // Render rain
                        if (k2 != l2) {
                            this.random.setSeed(l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761);
                            blockpos$mutableblockpos.func_181079_c(l1, k2, k1);

                            final Ambience ambience = Client.INSTANCE.getModuleManager().get(Ambience.class);

                            final float f1 = ambience.getFloatTemperature(blockpos$mutableblockpos, biomegenbase);

                            if (world.getWorldChunkManager().getTemperatureAtHeight(f1, j2) >= 0.15F) {
                                if (j1 != 0) {
                                    if (j1 >= 0) {
                                        tessellator.draw();
                                    }

                                    j1 = 0;
                                    this.mc.getTextureManager().bindTexture(TEXTURE_RAIN);
                                    worldrenderer.begin(7, DefaultVertexFormats.field_181704_d);
                                }

                                final double d5 = ((double) (this.rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971 + k1 * k1 * 418711 + k1 * 13761 & 31) + (double) partialTicks) / 32.0D * (3.0D + this.random.nextDouble());
                                final double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
                                final double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
                                final float f2 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float) i1;
                                final float f3 = ((1.0F - f2 * f2) * 0.5F + 0.5F) * f5;
                                blockpos$mutableblockpos.func_181079_c(l1, i3, k1);
                                final int j3 = world.getCombinedLight(blockpos$mutableblockpos, 0);
                                final int k3 = j3 >> 16 & 65535;
                                final int l3 = j3 & 65535;
                                worldrenderer.pos((double) l1 - d3 + 0.5D, k2, (double) k1 - d4 + 0.5D).tex(0.0D, (double) k2 * 0.25D + d5).func_181666_a(1.0F, 1.0F, 1.0F, f3).func_181671_a(k3, l3).endVertex();
                                worldrenderer.pos((double) l1 + d3 + 0.5D, k2, (double) k1 + d4 + 0.5D).tex(1.0D, (double) k2 * 0.25D + d5).func_181666_a(1.0F, 1.0F, 1.0F, f3).func_181671_a(k3, l3).endVertex();
                                worldrenderer.pos((double) l1 + d3 + 0.5D, l2, (double) k1 + d4 + 0.5D).tex(1.0D, (double) l2 * 0.25D + d5).func_181666_a(1.0F, 1.0F, 1.0F, f3).func_181671_a(k3, l3).endVertex();
                                worldrenderer.pos((double) l1 - d3 + 0.5D, l2, (double) k1 - d4 + 0.5D).tex(0.0D, (double) l2 * 0.25D + d5).func_181666_a(1.0F, 1.0F, 1.0F, f3).func_181671_a(k3, l3).endVertex();
                            }

                            // Render snow
                            else {
                                Color color = Color.WHITE;
                                ResourceLocation texture = TEXTURE_SNOW_HEAVY;

                                // Determine what texture we want to draw with
                                if (ambience.isEnabled()) {
                                    color = ambience.snowColor.getValue();
                                    if (ambience.getWeather().getValue().getName().equals("Light Snow"))
                                        texture = TEXTURE_SNOW_LIGHT;
                                    else if (ambience.getWeather().getValue().getName().equals("Nether Particles")) {
                                        texture = TEXTURE_NETHER_PARTICLES;
                                        color = new Color(220, 200, 230);
//                                        this.fogColorRed = 0.5F;
//                                        this.fogColorBlue = 0.5F;
                                    }
                                }

                                if (j1 != 1) {
                                    if (j1 >= 0) {
                                        tessellator.draw();
                                    }

                                    j1 = 1;
                                    this.mc.getTextureManager().bindTexture(texture);
                                    worldrenderer.begin(7, DefaultVertexFormats.field_181704_d);
                                }

                                final double d8 = ((float) (this.rendererUpdateCount & 511) + partialTicks) / 512.0F;
                                final double d9 = this.random.nextDouble() + (double) f * 0.01D * (double) ((float) this.random.nextGaussian());
                                final double d10 = this.random.nextDouble() + (double) (f * (float) this.random.nextGaussian()) * 0.001D;
                                final double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
                                final double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
                                final float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float) i1;
                                final float f4 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f5;
                                blockpos$mutableblockpos.func_181079_c(l1, i3, k1);
                                final int i4 = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                                final int j4 = i4 >> 16 & 65535;
                                final int k4 = i4 & 65535;

                                worldrenderer.pos((double) l1 - d3 + 0.5D, k2, (double) k1 - d4 + 0.5D).tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).func_181666_a(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, f4).func_181671_a(j4, k4).endVertex();
                                worldrenderer.pos((double) l1 + d3 + 0.5D, k2, (double) k1 + d4 + 0.5D).tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).func_181666_a(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, f4).func_181671_a(j4, k4).endVertex();
                                worldrenderer.pos((double) l1 + d3 + 0.5D, l2, (double) k1 + d4 + 0.5D).tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).func_181666_a(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, f4).func_181671_a(j4, k4).endVertex();
                                worldrenderer.pos((double) l1 - d3 + 0.5D, l2, (double) k1 - d4 + 0.5D).tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).func_181666_a(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, f4).func_181671_a(j4, k4).endVertex();
                            }
                        }
                    }
                }
            }

            if (j1 >= 0) {
                tessellator.draw();
            }

            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            this.disableLightmap();
        }
    }

    /**
     * Setup orthogonal projection for rendering GUI screen overlays
     */
    public void setupOverlayRendering() {
        final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    /**
     * calculates fog and calls glClearColor
     */
    private void updateFogColor(final float partialTicks) {
        final World world = this.mc.theWorld;
        final Entity entity = this.mc.getRenderViewEntity();
        float f = 0.25F + 0.75F * (float) this.mc.gameSettings.renderDistanceChunks / 32.0F;
        f = 1.0F - (float) Math.pow(f, 0.25D);
        Vec3 vec3 = world.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
        vec3 = CustomColors.getWorldSkyColor(vec3, world, this.mc.getRenderViewEntity(), partialTicks);
        final float f1 = (float) vec3.xCoord;
        final float f2 = (float) vec3.yCoord;
        final float f3 = (float) vec3.zCoord;
        Vec3 vec31 = world.getFogColor(partialTicks);
        vec31 = CustomColors.getWorldFogColor(vec31, world, this.mc.getRenderViewEntity(), partialTicks);
        this.fogColorRed = (float) vec31.xCoord;
        this.fogColorGreen = (float) vec31.yCoord;
        this.fogColorBlue = (float) vec31.zCoord;

        if (this.mc.gameSettings.renderDistanceChunks >= 4) {
            final double d0 = -1.0D;
            final Vec3 vec32 = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) > 0.0F ? new Vec3(d0, 0.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
            float f5 = (float) entity.getLook(partialTicks).dotProduct(vec32);

            if (f5 < 0.0F) {
                f5 = 0.0F;
            }

            if (f5 > 0.0F) {
                final float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

                if (afloat != null) {
                    f5 = f5 * afloat[3];
                    this.fogColorRed = this.fogColorRed * (1.0F - f5) + afloat[0] * f5;
                    this.fogColorGreen = this.fogColorGreen * (1.0F - f5) + afloat[1] * f5;
                    this.fogColorBlue = this.fogColorBlue * (1.0F - f5) + afloat[2] * f5;
                }
            }
        }

        this.fogColorRed += (f1 - this.fogColorRed) * f;
        this.fogColorGreen += (f2 - this.fogColorGreen) * f;
        this.fogColorBlue += (f3 - this.fogColorBlue) * f;
        final float f8 = world.getRainStrength(partialTicks);

        if (f8 > 0.0F) {
            final float f4 = 1.0F - f8 * 0.5F;
            final float f10 = 1.0F - f8 * 0.4F;
            this.fogColorRed *= f4;
            this.fogColorGreen *= f4;
            this.fogColorBlue *= f10;
        }

        final float f9 = world.getThunderStrength(partialTicks);

        if (f9 > 0.0F) {
            final float f11 = 1.0F - f9 * 0.5F;
            this.fogColorRed *= f11;
            this.fogColorGreen *= f11;
            this.fogColorBlue *= f11;
        }

        final Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);

        if (this.cloudFog) {
            final Vec3 vec33 = world.getCloudColour(partialTicks);
            this.fogColorRed = (float) vec33.xCoord;
            this.fogColorGreen = (float) vec33.yCoord;
            this.fogColorBlue = (float) vec33.zCoord;
        } else if (block.getMaterial() == Material.water) {
            float f12 = (float) EnchantmentHelper.getRespiration(entity) * 0.2F;
            f12 = Config.limit(f12, 0.0F, 0.6F);

            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.waterBreathing)) {
                f12 = f12 * 0.3F + 0.6F;
            }

            this.fogColorRed = 0.02F + f12;
            this.fogColorGreen = 0.02F + f12;
            this.fogColorBlue = 0.2F + f12;
            final Vec3 vec35 = CustomColors.getUnderwaterColor(this.mc.theWorld, this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + 1.0D, this.mc.getRenderViewEntity().posZ);

            if (vec35 != null) {
                this.fogColorRed = (float) vec35.xCoord;
                this.fogColorGreen = (float) vec35.yCoord;
                this.fogColorBlue = (float) vec35.zCoord;
            }
        } else if (block.getMaterial() == Material.lava) {
            this.fogColorRed = 0.6F;
            this.fogColorGreen = 0.1F;
            this.fogColorBlue = 0.0F;
            final Vec3 vec34 = CustomColors.getUnderlavaColor(this.mc.theWorld, this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + 1.0D, this.mc.getRenderViewEntity().posZ);

            if (vec34 != null) {
                this.fogColorRed = (float) vec34.xCoord;
                this.fogColorGreen = (float) vec34.yCoord;
                this.fogColorBlue = (float) vec34.zCoord;
            }
        }

        final float f13 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * partialTicks;
        this.fogColorRed *= f13;
        this.fogColorGreen *= f13;
        this.fogColorBlue *= f13;
        double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks) * world.provider.getVoidFogYFactor();

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.blindness)) {
            final int i = ((EntityLivingBase) entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20) {
                d1 *= 1.0F - (float) i / 20.0F;
            } else {
                d1 = 0.0D;
            }
        }

        if (d1 < 1.0D) {
            if (d1 < 0.0D) {
                d1 = 0.0D;
            }

            d1 = d1 * d1;
            this.fogColorRed = (float) ((double) this.fogColorRed * d1);
            this.fogColorGreen = (float) ((double) this.fogColorGreen * d1);
            this.fogColorBlue = (float) ((double) this.fogColorBlue * d1);
        }

        if (this.bossColorModifier > 0.0F) {
            final float f14 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
            this.fogColorRed = this.fogColorRed * (1.0F - f14) + this.fogColorRed * 0.7F * f14;
            this.fogColorGreen = this.fogColorGreen * (1.0F - f14) + this.fogColorGreen * 0.6F * f14;
            this.fogColorBlue = this.fogColorBlue * (1.0F - f14) + this.fogColorBlue * 0.6F * f14;
        }

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.nightVision)) {
            final float f15 = this.getNightVisionBrightness((EntityLivingBase) entity, partialTicks);
            float f6 = 1.0F / this.fogColorRed;

            if (f6 > 1.0F / this.fogColorGreen) {
                f6 = 1.0F / this.fogColorGreen;
            }

            if (f6 > 1.0F / this.fogColorBlue) {
                f6 = 1.0F / this.fogColorBlue;
            }

            if (Float.isInfinite(f6)) {
                f6 = Math.nextAfter(f6, 0.0D);
            }

            this.fogColorRed = this.fogColorRed * (1.0F - f15) + this.fogColorRed * f6 * f15;
            this.fogColorGreen = this.fogColorGreen * (1.0F - f15) + this.fogColorGreen * f6 * f15;
            this.fogColorBlue = this.fogColorBlue * (1.0F - f15) + this.fogColorBlue * f6 * f15;
        }

        if (this.mc.gameSettings.anaglyph) {
            final float f16 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
            final float f17 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
            final float f7 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
            this.fogColorRed = f16;
            this.fogColorGreen = f17;
            this.fogColorBlue = f7;
        }

        if (Reflector.EntityViewRenderEvent_FogColors_Constructor.exists()) {
            final Object object = Reflector.newInstance(Reflector.EntityViewRenderEvent_FogColors_Constructor, this, entity, block, partialTicks, this.fogColorRed, this.fogColorGreen, this.fogColorBlue);
            Reflector.postForgeBusEvent(object);
            this.fogColorRed = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_red, this.fogColorRed);
            this.fogColorGreen = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_green, this.fogColorGreen);
            this.fogColorBlue = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_blue, this.fogColorBlue);
        }

        Shaders.setClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
    }

    /**
     * Sets up the fog to be rendered. If the arg passed in is -1 the fog starts at 0 and goes to 80% of far plane
     * distance and is used for sky rendering.
     */
    private void setupFog(final int p_78468_1_, final float partialTicks) {
        this.fogStandard = false;
        final Entity entity = this.mc.getRenderViewEntity();

        GL11.glFog(GL11.GL_FOG_COLOR, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        final Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
        float f = -1.0F;

        if (Reflector.ForgeHooksClient_getFogDensity.exists()) {
            f = Reflector.callFloat(Reflector.ForgeHooksClient_getFogDensity, this, entity, block, partialTicks, 0.1F);
        }

        if (f >= 0.0F) {
            GlStateManager.setFogDensity(f);
        } else if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.blindness)) {
            float f4 = 5.0F;
            final int i = ((EntityLivingBase) entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20) {
                f4 = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float) i / 20.0F);
            }

            GlStateManager.setFog(9729);

            if (p_78468_1_ == -1) {
                GlStateManager.setFogStart(0.0F);
                GlStateManager.setFogEnd(f4 * 0.8F);
            } else {
                GlStateManager.setFogStart(f4 * 0.25F);
                GlStateManager.setFogEnd(f4);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance && Config.isFogFancy()) {
                GL11.glFogi(34138, 34139);
            }
        } else if (this.cloudFog) {
            GlStateManager.setFog(2048);
            GlStateManager.setFogDensity(0.1F);
        } else if (block.getMaterial() == Material.water) {
            GlStateManager.setFog(2048);
            final float f1 = Config.isClearWater() ? 0.02F : 0.1F;

            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.waterBreathing)) {
                GlStateManager.setFogDensity(0.01F);
            } else {
                final float f2 = 0.1F - (float) EnchantmentHelper.getRespiration(entity) * 0.03F;
                GlStateManager.setFogDensity(Config.limit(f2, 0.0F, f1));
            }
        } else if (block.getMaterial() == Material.lava) {
            GlStateManager.setFog(2048);
            GlStateManager.setFogDensity(2.0F);
        } else {
            final float f3 = this.farPlaneDistance;
            this.fogStandard = true;
            GlStateManager.setFog(9729);

            if (p_78468_1_ == -1) {
                GlStateManager.setFogStart(0.0F);
                GlStateManager.setFogEnd(f3);
            } else {
                GlStateManager.setFogStart(f3 * Config.getFogStart());
                GlStateManager.setFogEnd(f3);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance) {
                if (Config.isFogFancy()) {
                    GL11.glFogi(34138, 34139);
                }

                if (Config.isFogFast()) {
                    GL11.glFogi(34138, 34140);
                }
            }

            if (this.mc.theWorld.provider.doesXZShowFog((int) entity.posX, (int) entity.posZ)) {
                GlStateManager.setFogStart(f3 * 0.05F);
                GlStateManager.setFogEnd(f3);
            }

            if (Reflector.ForgeHooksClient_onFogRender.exists()) {
                Reflector.callVoid(Reflector.ForgeHooksClient_onFogRender, this, entity, block, partialTicks, p_78468_1_, f3);
            }
        }

        GlStateManager.enableColorMaterial();
        GlStateManager.enableFog();
        GlStateManager.colorMaterial(1028, 4608);
    }

    /**
     * Update and return fogColorBuffer with the RGBA values passed as arguments
     */
    private FloatBuffer setFogColorBuffer(final float red, final float green, final float blue, final float alpha) {
        if (Config.isShaders()) {
            Shaders.setFogColor(red, green, blue);
        }

        this.fogColorBuffer.clear();
        this.fogColorBuffer.put(red).put(green).put(blue).put(alpha);
        this.fogColorBuffer.flip();
        return this.fogColorBuffer;
    }

    public MapItemRenderer getMapItemRenderer() {
        return this.theMapItemRenderer;
    }

    private void waitForServerThread() {
        this.serverWaitTimeCurrent = 0;

        if (Config.isSmoothWorld() && Config.isSingleProcessor()) {
            if (this.mc.isIntegratedServerRunning()) {
                final IntegratedServer integratedserver = this.mc.getIntegratedServer();

                if (integratedserver != null) {
                    final boolean flag = this.mc.isGamePaused();

                    if (!flag && !(this.mc.currentScreen instanceof GuiDownloadTerrain)) {
                        if (this.serverWaitTime > 0) {
                            Lagometer.timerServer.start();
                            Config.sleep(this.serverWaitTime);
                            Lagometer.timerServer.end();
                            this.serverWaitTimeCurrent = this.serverWaitTime;
                        }

                        final long i = System.nanoTime() / 1000000L;

                        if (this.lastServerTime != 0L && this.lastServerTicks != 0) {
                            long j = i - this.lastServerTime;

                            if (j < 0L) {
                                this.lastServerTime = i;
                                j = 0L;
                            }

                            if (j >= 50L) {
                                this.lastServerTime = i;
                                final int k = integratedserver.getTickCounter();
                                int l = k - this.lastServerTicks;

                                if (l < 0) {
                                    this.lastServerTicks = k;
                                    l = 0;
                                }

                                if (l < 1 && this.serverWaitTime < 100) {
                                    this.serverWaitTime += 2;
                                }

                                if (l > 1 && this.serverWaitTime > 0) {
                                    --this.serverWaitTime;
                                }

                                this.lastServerTicks = k;
                            }
                        } else {
                            this.lastServerTime = i;
                            this.lastServerTicks = integratedserver.getTickCounter();
                            this.avgServerTickDiff = 1.0F;
                            this.avgServerTimeDiff = 50.0F;
                        }
                    } else {
                        if (this.mc.currentScreen instanceof GuiDownloadTerrain) {
                            Config.sleep(20L);
                        }

                        this.lastServerTime = 0L;
                        this.lastServerTicks = 0;
                    }
                }
            }
        } else {
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
        }
    }

    private void frameInit() {
        GlErrors.frameStart();

        if (!this.initialized) {
            TextureUtils.registerResourceListener();

            if (Config.getBitsOs() == 64 && Config.getBitsJre() == 32) {
                Config.setNotify64BitJava(true);
            }

            this.initialized = true;
        }

        Config.checkDisplayMode();
        final World world = this.mc.theWorld;

        if (world != null) {
            if (Config.getNewRelease() != null) {
                final String s = "HD_U".replace("HD_U", "HD Ultra").replace("L", "Light");
                final String s1 = s + " " + Config.getNewRelease();
                final ChatComponentText chatcomponenttext = new ChatComponentText(I18n.format("of.message.newVersion", "\u00a7n" + s1 + "\u00a7r"));
                chatcomponenttext.setChatStyle((new ChatStyle()).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://optifine.net/downloads")));
                this.mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext);
                Config.setNewRelease(null);
            }

            if (Config.isNotify64BitJava()) {
                Config.setNotify64BitJava(false);
                final ChatComponentText chatcomponenttext1 = new ChatComponentText(I18n.format("of.message.java64Bit"));
                this.mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext1);
            }
        }

        if (this.mc.currentScreen instanceof GuiMainMenu) {
            this.updateMainMenu((GuiMainMenu) this.mc.currentScreen);
        }

        if (this.updatedWorld != world) {
            RandomEntities.worldChanged(this.updatedWorld, world);
            Config.updateThreadPriorities();
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
            this.updatedWorld = world;
        }

        if (!this.setFxaaShader(Shaders.configAntialiasingLevel)) {
            Shaders.configAntialiasingLevel = 0;
        }

        if (this.mc.currentScreen != null && this.mc.currentScreen.getClass() == GuiChat.class) {
            this.mc.displayGuiScreen(new GuiChatOF((GuiChat) this.mc.currentScreen));
        }

        StencilUtil.checkSetupFBO(mc.getFramebuffer());
    }

    private void frameFinish() {
        if (this.mc.theWorld != null && Config.isShowGlErrors() && TimedEvent.isActive("CheckGlErrorFrameFinish", 10000L)) {
            final int i = GlStateManager.glGetError();

            if (i != 0 && GlErrors.isEnabled(i)) {
                final String s = Config.getGlErrorString(i);
                final ChatComponentText chatcomponenttext = new ChatComponentText(I18n.format("of.message.openglError", i, s));
                this.mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext);
            }
        }
    }

    private void updateMainMenu(final GuiMainMenu p_updateMainMenu_1_) {
        try {
            String s = null;
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            final int i = calendar.get(Calendar.DATE);
            final int j = calendar.get(Calendar.MONTH) + 1;

            if (i == 8 && j == 4) {
                s = "Happy birthday, OptiFine!";
            }

            if (i == 14 && j == 8) {
                s = "Happy birthday, sp614x!";
            }

            if (s == null) {
                return;
            }

            Reflector.setFieldValue(p_updateMainMenu_1_, Reflector.GuiMainMenu_splashText, s);
        } catch (final Throwable var6) {
        }
    }

    public boolean setFxaaShader(final int p_setFxaaShader_1_) {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            return false;
        } else if (this.theShaderGroup != null && this.theShaderGroup != this.fxaaShaders[2] && this.theShaderGroup != this.fxaaShaders[4]) {
            return true;
        } else if (p_setFxaaShader_1_ != 2 && p_setFxaaShader_1_ != 4) {
            if (this.theShaderGroup != null) {
                this.theShaderGroup.deleteShaderGroup();
                this.theShaderGroup = null;
            }
            return true;
        } else if (this.theShaderGroup != null && this.theShaderGroup == this.fxaaShaders[p_setFxaaShader_1_]) {
            return true;
        } else if (this.mc.theWorld == null) {
            return true;
        } else {
            this.loadShader(new ResourceLocation("shaders/post/fxaa_of_" + p_setFxaaShader_1_ + "x.json"));
            this.fxaaShaders[p_setFxaaShader_1_] = this.theShaderGroup;
            return this.useShader;
        }
    }

    private void checkLoadVisibleChunks(final Entity p_checkLoadVisibleChunks_1_, final float p_checkLoadVisibleChunks_2_, final ICamera p_checkLoadVisibleChunks_3_, final boolean p_checkLoadVisibleChunks_4_) {
        final int i = 201435902;

        if (this.loadVisibleChunks) {
            this.loadVisibleChunks = false;
            this.loadAllVisibleChunks(p_checkLoadVisibleChunks_1_, p_checkLoadVisibleChunks_2_, p_checkLoadVisibleChunks_3_, p_checkLoadVisibleChunks_4_);
            this.mc.ingameGUI.getChatGUI().deleteChatLine(i);
        }

        if (Keyboard.isKeyDown(61) && Keyboard.isKeyDown(38)) {
            if (this.mc.currentScreen != null) {
                return;
            }

            this.loadVisibleChunks = true;
            final ChatComponentText chatcomponenttext = new ChatComponentText(I18n.format("of.message.loadingVisibleChunks"));
            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chatcomponenttext, i);
        }
    }

    private void loadAllVisibleChunks(final Entity p_loadAllVisibleChunks_1_, final double p_loadAllVisibleChunks_2_, final ICamera p_loadAllVisibleChunks_4_, final boolean p_loadAllVisibleChunks_5_) {
        final int i = this.mc.gameSettings.ofChunkUpdates;
        final boolean flag = this.mc.gameSettings.ofLazyChunkLoading;

        try {
            this.mc.gameSettings.ofChunkUpdates = 1000;
            this.mc.gameSettings.ofLazyChunkLoading = false;
            final RenderGlobal renderglobal = Config.getRenderGlobal();
            int j = renderglobal.getCountLoadedChunks();
            final long k = System.currentTimeMillis();
            Config.dbg("Loading visible chunks");
            long l = System.currentTimeMillis() + 5000L;
            int i1 = 0;
            boolean flag1;

            do {
                flag1 = false;

                renderglobal.displayListEntitiesDirty = true;
                renderglobal.setupTerrain(p_loadAllVisibleChunks_1_, p_loadAllVisibleChunks_2_, p_loadAllVisibleChunks_4_, this.frameCount++, p_loadAllVisibleChunks_5_);

                for (int j1 = 0; j1 < 100; ++j1) {

                    if (!renderglobal.hasNoChunkUpdates()) {
                        flag1 = true;
                    }

                    i1 = i1 + renderglobal.getCountChunksToUpdate();

                    while (!renderglobal.hasNoChunkUpdates()) {
                        renderglobal.updateChunks(System.nanoTime() + 1000000000L);
                    }

                    i1 = i1 - renderglobal.getCountChunksToUpdate();

                    if (!flag1) {
                        break;
                    }
                }

                if (renderglobal.getCountLoadedChunks() != j) {
                    flag1 = true;
                    j = renderglobal.getCountLoadedChunks();
                }

                if (System.currentTimeMillis() > l) {
                    Config.log("Chunks loaded: " + i1);
                    l = System.currentTimeMillis() + 5000L;
                }

            } while (flag1);

            Config.log("Chunks loaded: " + i1);
            Config.log("Finished loading visible chunks");
            RenderChunk.renderChunksUpdated = 0;
        } finally {
            this.mc.gameSettings.ofChunkUpdates = i;
            this.mc.gameSettings.ofLazyChunkLoading = flag;
        }
    }
}
