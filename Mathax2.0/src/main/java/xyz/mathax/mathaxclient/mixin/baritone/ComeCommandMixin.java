package xyz.mathax.mathaxclient.mixin.baritone;

import baritone.api.pathing.goals.GoalBlock;
import baritone.command.defaults.ComeCommand;
import xyz.mathax.mathaxclient.systems.modules.Modules;
import xyz.mathax.mathaxclient.systems.modules.render.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static xyz.mathax.mathaxclient.MatHax.mc;

@Mixin(ComeCommand.class)
public class ComeCommandMixin {
    @ModifyArgs(method = "execute", at = @At(value = "INVOKE", target = "Lbaritone/api/process/ICustomGoalProcess;setGoalAndPath(Lbaritone/api/pathing/goals/Goal;)V"), remap = false)
    private void getComeCommandTarget(Args args) {
        Freecam freecam = Modules.get().get(Freecam.class);
        if (freecam.isEnabled()) {
            float tickDelta = mc.getTickDelta();
            args.set(0, new GoalBlock((int) freecam.getX(tickDelta), (int) freecam.getY(tickDelta), (int) freecam.getZ(tickDelta)));
        }
    }
}
