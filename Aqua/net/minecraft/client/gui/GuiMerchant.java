package net.minecraft.client.gui;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiMerchant
extends GuiContainer {
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation("textures/gui/container/villager.png");
    private IMerchant merchant;
    private MerchantButton nextButton;
    private MerchantButton previousButton;
    private int selectedMerchantRecipe;
    private IChatComponent chatComponent;

    public GuiMerchant(InventoryPlayer p_i45500_1_, IMerchant p_i45500_2_, World worldIn) {
        super((Container)new ContainerMerchant(p_i45500_1_, p_i45500_2_, worldIn));
        this.merchant = p_i45500_2_;
        this.chatComponent = p_i45500_2_.getDisplayName();
    }

    public void initGui() {
        super.initGui();
        int i = (width - this.xSize) / 2;
        int j = (height - this.ySize) / 2;
        this.nextButton = new MerchantButton(1, i + 120 + 27, j + 24 - 1, true);
        this.buttonList.add((Object)this.nextButton);
        this.previousButton = new MerchantButton(2, i + 36 - 19, j + 24 - 1, false);
        this.buttonList.add((Object)this.previousButton);
        this.nextButton.enabled = false;
        this.previousButton.enabled = false;
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.chatComponent.getUnformattedText();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 0x404040);
        this.fontRendererObj.drawString(I18n.format((String)"container.inventory", (Object[])new Object[0]), 8, this.ySize - 96 + 2, 0x404040);
    }

    public void updateScreen() {
        super.updateScreen();
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes((EntityPlayer)this.mc.thePlayer);
        if (merchantrecipelist != null) {
            this.nextButton.enabled = this.selectedMerchantRecipe < merchantrecipelist.size() - 1;
            this.previousButton.enabled = this.selectedMerchantRecipe > 0;
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        boolean flag = false;
        if (button == this.nextButton) {
            ++this.selectedMerchantRecipe;
            MerchantRecipeList merchantrecipelist = this.merchant.getRecipes((EntityPlayer)this.mc.thePlayer);
            if (merchantrecipelist != null && this.selectedMerchantRecipe >= merchantrecipelist.size()) {
                this.selectedMerchantRecipe = merchantrecipelist.size() - 1;
            }
            flag = true;
        } else if (button == this.previousButton) {
            --this.selectedMerchantRecipe;
            if (this.selectedMerchantRecipe < 0) {
                this.selectedMerchantRecipe = 0;
            }
            flag = true;
        }
        if (flag) {
            ((ContainerMerchant)this.inventorySlots).setCurrentRecipeIndex(this.selectedMerchantRecipe);
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(this.selectedMerchantRecipe);
            this.mc.getNetHandler().addToSendQueue((Packet)new C17PacketCustomPayload("MC|TrSel", packetbuffer));
        }
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
        int i = (width - this.xSize) / 2;
        int j = (height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes((EntityPlayer)this.mc.thePlayer);
        if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
            int k = this.selectedMerchantRecipe;
            if (k < 0 || k >= merchantrecipelist.size()) {
                return;
            }
            MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(k);
            if (merchantrecipe.isRecipeDisabled()) {
                this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
                GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                GlStateManager.disableLighting();
                this.drawTexturedModalRect(this.guiLeft + 83, this.guiTop + 21, 212, 0, 28, 21);
                this.drawTexturedModalRect(this.guiLeft + 83, this.guiTop + 51, 212, 0, 28, 21);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        MerchantRecipeList merchantrecipelist = this.merchant.getRecipes((EntityPlayer)this.mc.thePlayer);
        if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
            int i = (width - this.xSize) / 2;
            int j = (height - this.ySize) / 2;
            int k = this.selectedMerchantRecipe;
            MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(k);
            ItemStack itemstack = merchantrecipe.getItemToBuy();
            ItemStack itemstack1 = merchantrecipe.getSecondItemToBuy();
            ItemStack itemstack2 = merchantrecipe.getItemToSell();
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableLighting();
            this.itemRender.zLevel = 100.0f;
            this.itemRender.renderItemAndEffectIntoGUI(itemstack, i + 36, j + 24);
            this.itemRender.renderItemOverlays(this.fontRendererObj, itemstack, i + 36, j + 24);
            if (itemstack1 != null) {
                this.itemRender.renderItemAndEffectIntoGUI(itemstack1, i + 62, j + 24);
                this.itemRender.renderItemOverlays(this.fontRendererObj, itemstack1, i + 62, j + 24);
            }
            this.itemRender.renderItemAndEffectIntoGUI(itemstack2, i + 120, j + 24);
            this.itemRender.renderItemOverlays(this.fontRendererObj, itemstack2, i + 120, j + 24);
            this.itemRender.zLevel = 0.0f;
            GlStateManager.disableLighting();
            if (this.isPointInRegion(36, 24, 16, 16, mouseX, mouseY) && itemstack != null) {
                this.renderToolTip(itemstack, mouseX, mouseY);
            } else if (itemstack1 != null && this.isPointInRegion(62, 24, 16, 16, mouseX, mouseY) && itemstack1 != null) {
                this.renderToolTip(itemstack1, mouseX, mouseY);
            } else if (itemstack2 != null && this.isPointInRegion(120, 24, 16, 16, mouseX, mouseY) && itemstack2 != null) {
                this.renderToolTip(itemstack2, mouseX, mouseY);
            } else if (merchantrecipe.isRecipeDisabled() && (this.isPointInRegion(83, 21, 28, 21, mouseX, mouseY) || this.isPointInRegion(83, 51, 28, 21, mouseX, mouseY))) {
                this.drawCreativeTabHoveringText(I18n.format((String)"merchant.deprecated", (Object[])new Object[0]), mouseX, mouseY);
            }
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }

    public IMerchant getMerchant() {
        return this.merchant;
    }

    static /* synthetic */ ResourceLocation access$000() {
        return MERCHANT_GUI_TEXTURE;
    }
}