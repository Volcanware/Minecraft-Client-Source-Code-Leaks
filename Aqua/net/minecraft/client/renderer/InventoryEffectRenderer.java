package net.minecraft.client.renderer;

import java.util.Collection;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public abstract class InventoryEffectRenderer
extends GuiContainer {
    private boolean hasActivePotionEffects;

    public InventoryEffectRenderer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    public void initGui() {
        super.initGui();
        this.updateActivePotionEffects();
    }

    protected void updateActivePotionEffects() {
        if (!this.mc.thePlayer.getActivePotionEffects().isEmpty()) {
            this.guiLeft = 160 + (width - this.xSize - 200) / 2;
            this.hasActivePotionEffects = true;
        } else {
            this.guiLeft = (width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.hasActivePotionEffects) {
            this.drawActivePotionEffects();
        }
    }

    private void drawActivePotionEffects() {
        int i = this.guiLeft - 124;
        int j = this.guiTop;
        int k = 166;
        Collection collection = this.mc.thePlayer.getActivePotionEffects();
        if (!collection.isEmpty()) {
            GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.disableLighting();
            int l = 33;
            if (collection.size() > 5) {
                l = 132 / (collection.size() - 1);
            }
            for (PotionEffect potioneffect : this.mc.thePlayer.getActivePotionEffects()) {
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                this.mc.getTextureManager().bindTexture(inventoryBackground);
                this.drawTexturedModalRect(i, j, 0, 166, 140, 32);
                if (potion.hasStatusIcon()) {
                    int i1 = potion.getStatusIconIndex();
                    this.drawTexturedModalRect(i + 6, j + 7, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                }
                String s1 = I18n.format((String)potion.getName(), (Object[])new Object[0]);
                if (potioneffect.getAmplifier() == 1) {
                    s1 = s1 + " " + I18n.format((String)"enchantment.level.2", (Object[])new Object[0]);
                } else if (potioneffect.getAmplifier() == 2) {
                    s1 = s1 + " " + I18n.format((String)"enchantment.level.3", (Object[])new Object[0]);
                } else if (potioneffect.getAmplifier() == 3) {
                    s1 = s1 + " " + I18n.format((String)"enchantment.level.4", (Object[])new Object[0]);
                }
                this.fontRendererObj.drawStringWithShadow(s1, (float)(i + 10 + 18), (float)(j + 6), 0xFFFFFF);
                String s = Potion.getDurationString((PotionEffect)potioneffect);
                this.fontRendererObj.drawStringWithShadow(s, (float)(i + 10 + 18), (float)(j + 6 + 10), 0x7F7F7F);
                j += l;
            }
        }
    }
}
