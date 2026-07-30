package betterwithlocking.command;

import betterwithlocking.LockManager;
import betterwithlocking.Lockable;
import betterwithlocking.util.Feedback;
import betterwithlocking.util.LockUtil;
import com.mojang.brigadier.Command;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityChest;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.server.entity.player.PlayerServer;

import java.util.Collections;

public class CommandLogicUnlock {

	public static int unlock(PlayerServer sender){
		HitResult rayCastResult = LockUtil.rayCastFromPlayer(sender);
		if (!(rayCastResult instanceof HitResult.Tile)) {
			Feedback.error(sender, "Failed to Unlock Container! (Not Looking at Container)");
			return Command.SINGLE_SUCCESS;
		}

		TileEntity container = sender.world.getTileEntity(((HitResult.Tile) rayCastResult).tilePos);
		if(container != null){
			if (container instanceof Lockable) {
				Lockable lockable = ((Lockable) container);

				if (!lockable.getIsLocked()) {
					Feedback.error(sender, "Failed to Unlock Container! (Not Locked)");
					return Command.SINGLE_SUCCESS;
				}

				//owner or lock bypass only
				if (LockManager.determineAuthStatus(lockable, sender) < LockManager.FULL) {
					Feedback.error(sender, "Failed to Unlock Container! (Not Owned By You)");
					return Command.SINGLE_SUCCESS;
				}

				if (container instanceof TileEntityChest) {
					Lockable otherLockable = (Lockable) LockUtil.getOtherChest(sender.world, (TileEntityChest) container);
					if (otherLockable != null) {
						resetLock(lockable);
						resetLock(otherLockable);
						Feedback.destructive(sender, "Unlocked Double Chest!");
						return Command.SINGLE_SUCCESS;
					}
				}

				resetLock(lockable);
				Feedback.destructive(sender, "Unlocked " + LockUtil.getContainerName(container) + "!");
				return Command.SINGLE_SUCCESS;
			}
		}
		Feedback.error(sender, "Failed to Unlock Container! (Not Looking at Container)");
		return Command.SINGLE_SUCCESS;
	}

	private static void resetLock(Lockable lockable){
		lockable.setIsLocked(false);
		lockable.setLockOwner(null);
		lockable.setIsCommunityContainer(false);
		lockable.setTrustedPlayers(Collections.emptyList());
	}
}
