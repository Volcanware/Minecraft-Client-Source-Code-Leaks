package net.minecraft.client.gui;

import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;

public class GuiPlayerTabOverlay
extends Gui {
    public static final Ordering<NetworkPlayerInfo> field_175252_a = Ordering.from((Comparator)new PlayerComparator(null));
    private final Minecraft mc;
    private final GuiIngame guiIngame;
    private IChatComponent footer;
    private IChatComponent header;
    private long lastTimeOpened;
    private boolean isBeingRendered;

    public GuiPlayerTabOverlay(Minecraft mcIn, GuiIngame guiIngameIn) {
        this.mc = mcIn;
        this.guiIngame = guiIngameIn;
    }

    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
        return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName((Team)networkPlayerInfoIn.getPlayerTeam(), (String)networkPlayerInfoIn.getGameProfile().getName());
    }

    public void updatePlayerList(boolean willBeRendered) {
        if (willBeRendered && !this.isBeingRendered) {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }
        this.isBeingRendered = willBeRendered;
    }

    public void renderPlayerlist(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn) {
        boolean flag;
        int l3;
        NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
        List list = field_175252_a.sortedCopy((Iterable)nethandlerplayclient.getPlayerInfoMap());
        int i = 0;
        int j = 0;
        for (NetworkPlayerInfo networkplayerinfo : list) {
            int k = this.mc.fontRendererObj.getStringWidth(this.getPlayerName(networkplayerinfo));
            i = Math.max((int)i, (int)k);
            if (scoreObjectiveIn == null || scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) continue;
            k = this.mc.fontRendererObj.getStringWidth(" " + scoreboardIn.getValueFromObjective(networkplayerinfo.getGameProfile().getName(), scoreObjectiveIn).getScorePoints());
            j = Math.max((int)j, (int)k);
        }
        list = list.subList(0, Math.min((int)list.size(), (int)80));
        int i4 = l3 = list.size();
        int j4 = 1;
        while (i4 > 20) {
            i4 = (l3 + ++j4 - 1) / j4;
        }
        boolean bl = flag = this.mc.isIntegratedServerRunning() || this.mc.getNetHandler().getNetworkManager().getIsencrypted();
        int l = scoreObjectiveIn != null ? (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS ? 90 : j) : 0;
        int i1 = Math.min((int)(j4 * ((flag ? 9 : 0) + i + l + 13)), (int)(width - 50)) / j4;
        int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
        int k1 = 10;
        int l1 = i1 * j4 + (j4 - 1) * 5;
        List list1 = null;
        List list2 = null;
        if (this.header != null) {
            list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);
            for (String s : list1) {
                l1 = Math.max((int)l1, (int)this.mc.fontRendererObj.getStringWidth(s));
            }
        }
        if (this.footer != null) {
            list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);
            for (String s2 : list2) {
                l1 = Math.max((int)l1, (int)this.mc.fontRendererObj.getStringWidth(s2));
            }
        }
        if (list1 != null) {
            FontRenderer cfr_ignored_0 = this.mc.fontRendererObj;
            GuiPlayerTabOverlay.drawRect((int)(width / 2 - l1 / 2 - 1), (int)(k1 - 1), (int)(width / 2 + l1 / 2 + 1), (int)(k1 + list1.size() * FontRenderer.FONT_HEIGHT), (int)Integer.MIN_VALUE);
            for (String s3 : list1) {
                int i2 = this.mc.fontRendererObj.getStringWidth(s3);
                this.mc.fontRendererObj.drawStringWithShadow(s3, (float)(width / 2 - i2 / 2), (float)k1, -1);
                FontRenderer cfr_ignored_1 = this.mc.fontRendererObj;
                k1 += FontRenderer.FONT_HEIGHT;
            }
            ++k1;
        }
        GuiPlayerTabOverlay.drawRect((int)(width / 2 - l1 / 2 - 1), (int)(k1 - 1), (int)(width / 2 + l1 / 2 + 1), (int)(k1 + i4 * 9), (int)Integer.MIN_VALUE);
        for (int k4 = 0; k4 < l3; ++k4) {
            int k5;
            int l5;
            int l4 = k4 / i4;
            int i5 = k4 % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            int k2 = k1 + i5 * 9;
            GuiPlayerTabOverlay.drawRect((int)j2, (int)k2, (int)(j2 + i1), (int)(k2 + 8), (int)0x20FFFFFF);
            GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
            if (k4 >= list.size()) continue;
            NetworkPlayerInfo networkplayerinfo1 = (NetworkPlayerInfo)list.get(k4);
            String s1 = this.getPlayerName(networkplayerinfo1);
            GameProfile gameprofile = networkplayerinfo1.getGameProfile();
            if (flag) {
                EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
                boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE) && (gameprofile.getName().equals((Object)"Dinnerbone") || gameprofile.getName().equals((Object)"Grumm"));
                this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
                int l2 = 8 + (flag1 ? 8 : 0);
                int i3 = 8 * (flag1 ? -1 : 1);
                Gui.drawScaledCustomSizeModalRect((int)j2, (int)k2, (float)8.0f, (float)l2, (int)8, (int)i3, (int)8, (int)8, (float)64.0f, (float)64.0f);
                if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT)) {
                    int j3 = 8 + (flag1 ? 8 : 0);
                    int k3 = 8 * (flag1 ? -1 : 1);
                    Gui.drawScaledCustomSizeModalRect((int)j2, (int)k2, (float)40.0f, (float)j3, (int)8, (int)k3, (int)8, (int)8, (float)64.0f, (float)64.0f);
                }
                j2 += 9;
            }
            if (networkplayerinfo1.getGameType() == WorldSettings.GameType.SPECTATOR) {
                s1 = EnumChatFormatting.ITALIC + s1;
                this.mc.fontRendererObj.drawStringWithShadow(s1, (float)j2, (float)k2, -1862270977);
            } else {
                this.mc.fontRendererObj.drawStringWithShadow(s1, (float)j2, (float)k2, -1);
            }
            if (scoreObjectiveIn != null && networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR && (l5 = (k5 = j2 + i + 1) + l) - k5 > 5) {
                this.drawScoreboardValues(scoreObjectiveIn, k2, gameprofile.getName(), k5, l5, networkplayerinfo1);
            }
            this.drawPing(i1, j2 - (flag ? 9 : 0), k2, networkplayerinfo1);
        }
        if (list2 != null) {
            k1 = k1 + i4 * 9 + 1;
            FontRenderer cfr_ignored_2 = this.mc.fontRendererObj;
            GuiPlayerTabOverlay.drawRect((int)(width / 2 - l1 / 2 - 1), (int)(k1 - 1), (int)(width / 2 + l1 / 2 + 1), (int)(k1 + list2.size() * FontRenderer.FONT_HEIGHT), (int)Integer.MIN_VALUE);
            for (String s4 : list2) {
                int j5 = this.mc.fontRendererObj.getStringWidth(s4);
                this.mc.fontRendererObj.drawStringWithShadow(s4, (float)(width / 2 - j5 / 2), (float)k1, -1);
                FontRenderer cfr_ignored_3 = this.mc.fontRendererObj;
                k1 += FontRenderer.FONT_HEIGHT;
            }
        }
    }

    protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn) {
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.mc.getTextureManager().bindTexture(icons);
        int i = 0;
        int j = 0;
        j = networkPlayerInfoIn.getResponseTime() < 0 ? 5 : (networkPlayerInfoIn.getResponseTime() < 150 ? 0 : (networkPlayerInfoIn.getResponseTime() < 300 ? 1 : (networkPlayerInfoIn.getResponseTime() < 600 ? 2 : (networkPlayerInfoIn.getResponseTime() < 1000 ? 3 : 4))));
        zLevel += 100.0f;
        this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, 0 + i * 10, 176 + j * 8, 10, 8);
        zLevel -= 100.0f;
    }

    private void drawScoreboardValues(ScoreObjective p_175247_1_, int p_175247_2_, String p_175247_3_, int p_175247_4_, int p_175247_5_, NetworkPlayerInfo p_175247_6_) {
        int i = p_175247_1_.getScoreboard().getValueFromObjective(p_175247_3_, p_175247_1_).getScorePoints();
        if (p_175247_1_.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
            boolean flag;
            this.mc.getTextureManager().bindTexture(icons);
            if (this.lastTimeOpened == p_175247_6_.func_178855_p()) {
                if (i < p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(this.guiIngame.getUpdateCounter() + 20));
                } else if (i > p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(this.guiIngame.getUpdateCounter() + 10));
                }
            }
            if (Minecraft.getSystemTime() - p_175247_6_.func_178847_n() > 1000L || this.lastTimeOpened != p_175247_6_.func_178855_p()) {
                p_175247_6_.func_178836_b(i);
                p_175247_6_.func_178857_c(i);
                p_175247_6_.func_178846_a(Minecraft.getSystemTime());
            }
            p_175247_6_.func_178843_c(this.lastTimeOpened);
            p_175247_6_.func_178836_b(i);
            int j = MathHelper.ceiling_float_int((float)((float)Math.max((int)i, (int)p_175247_6_.func_178860_m()) / 2.0f));
            int k = Math.max((int)MathHelper.ceiling_float_int((float)(i / 2)), (int)Math.max((int)MathHelper.ceiling_float_int((float)(p_175247_6_.func_178860_m() / 2)), (int)10));
            boolean bl = flag = p_175247_6_.func_178858_o() > (long)this.guiIngame.getUpdateCounter() && (p_175247_6_.func_178858_o() - (long)this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;
            if (j > 0) {
                float f = Math.min((float)((float)(p_175247_5_ - p_175247_4_ - 4) / (float)k), (float)9.0f);
                if (f > 3.0f) {
                    for (int l = j; l < k; ++l) {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)l * f, p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                    }
                    for (int j1 = 0; j1 < j; ++j1) {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                        if (flag) {
                            if (j1 * 2 + 1 < p_175247_6_.func_178860_m()) {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, p_175247_2_, 70, 0, 9, 9);
                            }
                            if (j1 * 2 + 1 == p_175247_6_.func_178860_m()) {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, p_175247_2_, 79, 0, 9, 9);
                            }
                        }
                        if (j1 * 2 + 1 < i) {
                            this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, p_175247_2_, j1 >= 10 ? 160 : 52, 0, 9, 9);
                        }
                        if (j1 * 2 + 1 != i) continue;
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, p_175247_2_, j1 >= 10 ? 169 : 61, 0, 9, 9);
                    }
                } else {
                    float f1 = MathHelper.clamp_float((float)((float)i / 20.0f), (float)0.0f, (float)1.0f);
                    int i1 = (int)((1.0f - f1) * 255.0f) << 16 | (int)(f1 * 255.0f) << 8;
                    String s = "" + (float)i / 2.0f;
                    if (p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s + "hp") >= p_175247_4_) {
                        s = s + "hp";
                    }
                    this.mc.fontRendererObj.drawStringWithShadow(s, (float)((p_175247_5_ + p_175247_4_) / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2), (float)p_175247_2_, i1);
                }
            }
        } else {
            String s1 = EnumChatFormatting.YELLOW + "" + i;
            this.mc.fontRendererObj.drawStringWithShadow(s1, (float)(p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s1)), (float)p_175247_2_, 0xFFFFFF);
        }
    }

    public void setFooter(IChatComponent footerIn) {
        this.footer = footerIn;
    }

    public void setHeader(IChatComponent headerIn) {
        this.header = headerIn;
    }

    public void resetFooterHeader() {
        this.header = null;
        this.footer = null;
    }
}
