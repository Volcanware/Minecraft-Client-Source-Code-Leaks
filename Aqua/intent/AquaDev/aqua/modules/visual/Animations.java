package intent.AquaDev.aqua.modules.visual;

import de.Hero.settings.Setting;
import events.Event;
import intent.AquaDev.aqua.Aqua;
import intent.AquaDev.aqua.modules.Category;
import intent.AquaDev.aqua.modules.Module;
import net.minecraft.client.gui.ScaledResolution;

public class Animations
extends Module {
    public Animations() {
        super("Animations", "Animations", 0, Category.Visual);
        Aqua.setmgr.register(new Setting("Mode", (Module)this, "Aqua", new String[]{"Aqua", "1.7", "Exhibition", "Butter", "High1.7", "Own", "Skidding", "Whack"}));
    }

    public void onEnable() {
        super.onEnable();
    }

    public void onDisable() {
        super.onDisable();
    }

    public void onEvent(Event event) {
        ScaledResolution sr = new ScaledResolution(mc);
    }
}
