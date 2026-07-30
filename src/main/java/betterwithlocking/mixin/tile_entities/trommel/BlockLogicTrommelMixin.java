package betterwithlocking.mixin.tile_entities.trommel;

import betterwithlocking.Lockable;
import betterwithlocking.util.Feedback;
import betterwithlocking.util.LockUtil;
import betterwithlocking.LockManager;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicTrommel;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockLogicTrommel.class, remap = false)
public abstract class BlockLogicTrommelMixin extends BlockLogic {
	public BlockLogicTrommelMixin(Block<?> block, Material material) {
		super(block, material);
	}

	@Override
	public int getPistonPushReaction(World world, int x, int y, int z) {
		Lockable lockable = (Lockable) world.getTileEntity(x, y, z);
		if(lockable.getIsLocked()){
			return Material.PISTON_CANT_PUSH;
		}
		return super.getPistonPushReaction(world, x, y, z);
	}

	@Inject(at = @At("HEAD"), method = "onInteracted", cancellable = true)
	public void onBlockRightClickedInject(World world, TilePosc pos, Player player, Side side, double xPlaced, double yPlaced, CallbackInfoReturnable<Boolean> cir) {

		Lockable lockable = (Lockable) world.getTileEntity(pos);

		if(player instanceof PlayerServer && LockManager.determineAuthStatus(lockable, (PlayerServer) player) <= LockManager.UNTRUSTED && !player.isSneaking()){
			Feedback.error((PlayerServer) player, "Trommel is Locked! (Use /lock info for more information)");
			cir.setReturnValue(false);
			return;
		} else if(player instanceof PlayerServer && LockManager.determineAuthStatus(lockable, (PlayerServer) player) <= LockManager.UNTRUSTED && player.isSneaking()){
			cir.setReturnValue(false);
			return;
		}
	}

	@Override
	public void onPlacedByMob(World world, TilePosc pos, Side placeSide, Mob mob, double xPlaced, double yPlaced) {
		super.onPlacedByMob(world, pos, placeSide, mob, xPlaced, yPlaced);
		LockUtil.lockOnPlace(world, pos, mob);
	}
}
