package betterwithlocking.mixin;

import betterwithlocking.BetterWithLocking;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftServer.class, remap = false)
public abstract class MinecraftServerMixin {

	@Inject(at = @At("TAIL"), method = "startServer")
	private void startServerInject(CallbackInfoReturnable<Boolean> cir){
		BetterWithLocking.afterServerStart();
	}
}
