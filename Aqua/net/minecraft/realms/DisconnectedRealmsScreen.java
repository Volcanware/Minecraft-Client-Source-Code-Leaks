package net.minecraft.realms;

import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.IChatComponent;

public class DisconnectedRealmsScreen
extends RealmsScreen {
    private String title;
    private IChatComponent reason;
    private List<String> lines;
    private final RealmsScreen parent;
    private int textHeight;

    public DisconnectedRealmsScreen(RealmsScreen parentIn, String unlocalizedTitle, IChatComponent reasonIn) {
        this.parent = parentIn;
        this.title = DisconnectedRealmsScreen.getLocalizedString((String)unlocalizedTitle);
        this.reason = reasonIn;
    }

    public void init() {
        Realms.setConnectedToRealms((boolean)false);
        this.buttonsClear();
        this.lines = this.fontSplit(this.reason.getFormattedText(), this.width() - 50);
        this.textHeight = this.lines.size() * this.fontLineHeight();
        this.buttonsAdd(DisconnectedRealmsScreen.newButton((int)0, (int)(this.width() / 2 - 100), (int)(this.height() / 2 + this.textHeight / 2 + this.fontLineHeight()), (String)DisconnectedRealmsScreen.getLocalizedString((String)"gui.back")));
    }

    public void keyPressed(char p_keyPressed_1_, int p_keyPressed_2_) {
        if (p_keyPressed_2_ == 1) {
            Realms.setScreen((RealmsScreen)this.parent);
        }
    }

    public void buttonClicked(RealmsButton p_buttonClicked_1_) {
        if (p_buttonClicked_1_.id() == 0) {
            Realms.setScreen((RealmsScreen)this.parent);
        }
    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground();
        this.drawCenteredString(this.title, this.width() / 2, this.height() / 2 - this.textHeight / 2 - this.fontLineHeight() * 2, 0xAAAAAA);
        int i = this.height() / 2 - this.textHeight / 2;
        if (this.lines != null) {
            for (String s : this.lines) {
                this.drawCenteredString(s, this.width() / 2, i, 0xFFFFFF);
                i += this.fontLineHeight();
            }
        }
        super.render(p_render_1_, p_render_2_, p_render_3_);
    }
}
