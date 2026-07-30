package betterwithlocking.command;

import betterwithlocking.Lockable;
import betterwithlocking.config.Data;
import betterwithlocking.util.Feedback;
import betterwithlocking.util.LockUtil;
import com.mojang.brigadier.Command;
import net.minecraft.core.block.entity.*;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.util.helper.UUIDHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandLogicLock {

	public static int lock(PlayerServer sender){
		HitResult rayCastResult = LockUtil.rayCastFromPlayer(sender);
		if (!(rayCastResult instanceof HitResult.Tile)) {
			Feedback.error(sender, "Failed to Lock Container! (Not Looking at Container)");
			return Command.SINGLE_SUCCESS;
		}

		TileEntity container = sender.world.getTileEntity(((HitResult.Tile) rayCastResult).tilePos);
		if(container != null){
			if (container instanceof Lockable) {
				Lockable lockable = ((Lockable) container);
				if (!lockable.getIsLocked()) {
					if (container instanceof TileEntityChest) {
						Lockable otherLockable = (Lockable) LockUtil.getOtherChest(sender.world, (TileEntityChest) container);
						if (otherLockable != null) {
							lockable.setIsLocked(true);
							otherLockable.setIsLocked(true);
							lockable.setLockOwner(sender.uuid);
							otherLockable.setLockOwner(sender.uuid);
							Feedback.success(sender, "Locked Double Chest!");
							return Command.SINGLE_SUCCESS;
						}
						Feedback.success(sender, "Locked Chest!");
					} else if (container instanceof TileEntityFurnaceBlast) {
						Feedback.success(sender, "Locked Blast Furnace!");
					} else if (container instanceof TileEntityFurnace) {
						Feedback.success(sender, "Locked Furnace!");
					} else if (container instanceof TileEntityDispenser) {
						Feedback.success(sender, "Locked Dispenser!");
					} else if (container instanceof TileEntityMeshGold) {
						Feedback.success(sender, "Locked Golden Mesh!");
					} else if (container instanceof TileEntityTrommel) {
						Feedback.success(sender, "Locked Trommel!");
					} else if (container instanceof TileEntityBasket) {
						Feedback.success(sender, "Locked Basket!");
					} else if (container instanceof TileEntityActivator) {
						Feedback.success(sender, "Locked Activator!");
					}

					lockable.setIsLocked(true);
					lockable.setLockOwner(sender.uuid);
					return Command.SINGLE_SUCCESS;

				} else if (lockable.getIsLocked() && !lockable.getLockOwner().equals(sender.uuid)) {
					Feedback.error(sender, "Failed to Lock Container! (Not Owned By You)");
					return Command.SINGLE_SUCCESS;
				}
				Feedback.error(sender, "Failed to Lock Container! (Already Locked)");
				return Command.SINGLE_SUCCESS;
			}
		}
		Feedback.error(sender, "Failed to Lock Container! (Not Looking at Container)");
		return Command.SINGLE_SUCCESS;
	}

	public static int lockOnBlockPlaced(@NotNull PlayerServer sender){
		UUID senderUUID = sender.uuid;

		if(Data.Users.getOrCreate(senderUUID).lockOnBlockPlaced){
			Data.Users.getOrCreate(senderUUID).lockOnBlockPlaced = false;
			Data.Users.save(senderUUID);
			Feedback.destructive(sender, "Locking on Block Placed Disabled");
		} else {
			Data.Users.getOrCreate(senderUUID).lockOnBlockPlaced = true;
			Data.Users.save(senderUUID);
			Feedback.success(sender, "Locking on Block Placed Enabled!");
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int lockOnBlockPunched(@NotNull PlayerServer sender){
		UUID senderUUID = sender.uuid;

		if(Data.Users.getOrCreate(senderUUID).lockOnBlockPunched){
			Data.Users.getOrCreate(senderUUID).lockOnBlockPunched = false;
			Data.Users.save(senderUUID);
			Feedback.destructive(sender, "Locking on Block Punched Disabled");
		} else {
			Data.Users.getOrCreate(senderUUID).lockOnBlockPunched = true;
			Data.Users.save(senderUUID);
			Feedback.success(sender, "Locking on Block Punched Enabled!");
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int lockTrust(PlayerServer sender, String targetUsername){

		Pair<UUID, String> profile;
		try {
			profile = LockUtil.getProfileFromUsername(targetUsername);
		} catch (NullPointerException e) {
			Feedback.error(sender, "Failed to Trust %s to Container! (Player Does not Exist)", targetUsername);
			return 0;
		}
		String targetUsernameOrDisplayName = profile.getRight();
		UUID targetUUID = profile.getLeft();

		HitResult rayCastResult = LockUtil.rayCastFromPlayer(sender);
		if (!(rayCastResult instanceof HitResult.Tile)) {
			Feedback.error(sender, "Failed to Trust %s to Container! (Not Looking at Container)", targetUsernameOrDisplayName);
			return Command.SINGLE_SUCCESS;
		}

		TileEntity container = sender.world.getTileEntity(((HitResult.Tile) rayCastResult).tilePos);

		if(container != null) {
			if (container instanceof Lockable) {
				Lockable lockable = ((Lockable) container);
				if (lockable.getIsLocked()) {

					if (!lockable.getLockOwner().equals(sender.uuid)) {
						Feedback.error(sender, "Failed to Trust %s to Container! (Not Owned By You)", targetUsernameOrDisplayName);
						return Command.SINGLE_SUCCESS;
					}

					if (lockable.getTrustedPlayers().contains(targetUUID)) {
						Feedback.error(sender, "Failed to Trust %s to Container! (Player already Trusted)", targetUsernameOrDisplayName);
						return Command.SINGLE_SUCCESS;
					}

					if (container instanceof TileEntityChest) {
						Lockable otherLockable = (Lockable) LockUtil.getOtherChest(sender.world, (TileEntityChest) container);
						if (otherLockable != null) {
							lockable.addTrustedPlayer(targetUUID);
							otherLockable.addTrustedPlayer(targetUUID);
							Feedback.success(sender, "Trusted %s to this Double Chest!", targetUsernameOrDisplayName);
							return Command.SINGLE_SUCCESS;
						}
						Feedback.success(sender, "Trusted %s to this Chest!", targetUsernameOrDisplayName);
					} else if (container instanceof TileEntityFurnaceBlast) {
						Feedback.success(sender, "Trusted %s to this Blast Furnace!", targetUsernameOrDisplayName);
					} else if (container instanceof TileEntityFurnace) {
						Feedback.success(sender, "Trusted %s to this Furnace!", targetUsernameOrDisplayName);
					} else if (container instanceof TileEntityDispenser) {
						Feedback.success(sender, "Trusted %s to this Dispenser!", targetUsernameOrDisplayName);
					} else if (container instanceof TileEntityMeshGold) {
						Feedback.success(sender, "Trusted %s to this Golden Mesh!", targetUsernameOrDisplayName);
					} else if (container instanceof TileEntityTrommel) {
						Feedback.success(sender, "Trusted %s to this Trommel!", targetUsernameOrDisplayName);
					} else if (container instanceof TileEntityBasket) {
						Feedback.success(sender, "Trusted %s to this Basket!", targetUsernameOrDisplayName);
					} else if (container instanceof TileEntityActivator) {
						Feedback.success(sender, "Trusted %s to this Activator!", targetUsernameOrDisplayName);
					}
					lockable.addTrustedPlayer(targetUUID);
				} else {
					Feedback.error(sender, "Failed to Trust %s to Container! (Container not Locked)", targetUsernameOrDisplayName);
				}
				return Command.SINGLE_SUCCESS;
			}
		}
		Feedback.error(sender, "Failed to Trust %s to Container! (Not Looking at Container)", targetUsernameOrDisplayName);
		return Command.SINGLE_SUCCESS;
	}

	public static int lockTrustAll(PlayerServer sender, String targetUsername){
		PlayerServer target = MinecraftServer.getInstance().playerList.getPlayerEntity(targetUsername);
		UUID targetUUID;

		if(target != null){
			targetUUID = target.uuid;
			String targetDisplayName = target.getDisplayName();
			if(!Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.containsKey(targetUUID)){

				Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.put(targetUUID, targetUsername);
				Data.Users.save(sender.uuid);
				Feedback.success(sender, "Trusted %s to all Containers!", targetDisplayName);
				return Command.SINGLE_SUCCESS;
			}
			Feedback.error(sender, "Failed to Trust %s to all Containers! (Player is Already Trusted)", targetDisplayName);
			return Command.SINGLE_SUCCESS;
		} else {
			UUIDHelper.runConversionAction(targetUsername, targetuuid ->
				{
					if(!Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.containsKey(targetuuid)){

						Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.put(targetuuid, targetUsername);
						Data.Users.save(sender.uuid);
						Feedback.success(sender, "Trusted %s to all Containers!", targetUsername);
						return;
					}
					Feedback.error(sender, "Failed to Trust %s to all Containers! (Player is Already Trusted)", targetUsername);
				},
				username -> Feedback.error(sender, "Failed to Trust %s to all Containers! (Player Does not Exist)", targetUsername)
			);
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int lockTrustCommunity(PlayerServer sender) {
		HitResult rayCastResult = LockUtil.rayCastFromPlayer(sender);
		if (!(rayCastResult instanceof HitResult.Tile)) {
			Feedback.error(sender, "Failed to Trust Community to Container! (Not Looking at Container)");
			return Command.SINGLE_SUCCESS;
		}

		TileEntity container = sender.world.getTileEntity(((HitResult.Tile) rayCastResult).tilePos);

		if(container != null) {
			if (container instanceof Lockable) {
				Lockable lockable = ((Lockable) container);
				if (lockable.getIsLocked()) {

					if (!lockable.getLockOwner().equals(sender.uuid)) {
						Feedback.error(sender, "Failed to Trust Community to Container! (Not Owned By You)");
						return Command.SINGLE_SUCCESS;
					}

					if (lockable.getIsCommunityContainer()){
						Feedback.error(sender, "Failed to Trust Community to Container! (Community already Trusted)");
						return Command.SINGLE_SUCCESS;
					}

					if (container instanceof TileEntityChest) {
						Lockable otherLockable = (Lockable) LockUtil.getOtherChest(sender.world, (TileEntityChest) container);
						if (otherLockable != null) {
							lockable.setIsCommunityContainer(true);
							otherLockable.setIsCommunityContainer(true);
							Feedback.success(sender, "Trusted Community to this Double Chest!");
							return Command.SINGLE_SUCCESS;
						}
						Feedback.success(sender, "Trusted Community to this Chest!");
					} else if (container instanceof TileEntityFurnaceBlast) {
						Feedback.success(sender, "Trusted Community to this Blast Furnace!");
					} else if (container instanceof TileEntityFurnace) {
						Feedback.success(sender, "Trusted Community to this Furnace!");
					} else if (container instanceof TileEntityDispenser) {
						Feedback.success(sender, "Trusted Community to this Dispenser!");
					} else if (container instanceof TileEntityMeshGold) {
						Feedback.success(sender, "Trusted Community to this Golden Mesh!");
					} else if (container instanceof TileEntityTrommel) {
						Feedback.success(sender, "Trusted Community to this Trommel!");
					} else if (container instanceof TileEntityBasket) {
						Feedback.success(sender, "Trusted Community to this Basket!");
					} else if (container instanceof TileEntityActivator) {
						Feedback.success(sender, "Trusted Community to this Activator!");
					}
					lockable.setIsCommunityContainer(true);
				} else {
					Feedback.error(sender, "Failed to Trust Community to Container! (Container not Locked)");
				}
				return Command.SINGLE_SUCCESS;
			}
		}
		Feedback.error(sender, "Failed to Trust Community to Container! (Not Looking at Container)");
		return Command.SINGLE_SUCCESS;
	}

	public static int lockUntrust(PlayerServer sender, String targetUsername){
		PlayerServer target = MinecraftServer.getInstance().playerList.getPlayerEntity(targetUsername);
		UUID targetUUID;
		String targetDisplayName;

		if(target != null){
			targetUUID = target.uuid;
			targetDisplayName = target.getDisplayName();
		} else {
			targetUUID = UUIDHelper.getUUIDFromName(targetUsername);
			if(targetUUID == null){
				Feedback.error(sender, "Failed to Untrust %s from Container! (Player Does not Exist)", targetUsername);
				return 0;
			}
			targetDisplayName = targetUsername;
		}

		HitResult rayCastResult = LockUtil.rayCastFromPlayer(sender);
		if (!(rayCastResult instanceof HitResult.Tile)) {
			Feedback.error(sender, "Failed to Untrust %s from Container! (Not Looking at Container)", targetDisplayName);
			return Command.SINGLE_SUCCESS;
		}

		TileEntity container = sender.world.getTileEntity(((HitResult.Tile) rayCastResult).tilePos);

		if(container != null) {
			if (container instanceof Lockable) {
				Lockable lockable = ((Lockable) container);
				if (lockable.getIsLocked()) {

					if (!lockable.getLockOwner().equals(sender.uuid)) {
						Feedback.error(sender, "Failed to Untrust %s from Container! (Not Owned By You)", targetDisplayName);
						return Command.SINGLE_SUCCESS;
					}

					if (!lockable.getTrustedPlayers().contains(targetUUID)) {
						Feedback.error(sender, "Failed to Untrust %s from Container! (Player not Trusted)", targetDisplayName);
						return Command.SINGLE_SUCCESS;
					}

					if (container instanceof TileEntityChest) {
						Lockable otherLockable = (Lockable) LockUtil.getOtherChest(sender.world, (TileEntityChest) container);
						if (otherLockable != null) {
							lockable.removeTrustedPlayer(targetUUID);
							otherLockable.removeTrustedPlayer(targetUUID);
							Feedback.destructive(sender, "Untrusted %s from this Double Chest!", targetDisplayName);
							return Command.SINGLE_SUCCESS;
						}
						Feedback.destructive(sender, "Untrusted %s from this Chest!", targetDisplayName);
					} else if (container instanceof TileEntityFurnaceBlast) {
						Feedback.destructive(sender, "Untrusted %s from this Blast Furnace!", targetDisplayName);
					} else if (container instanceof TileEntityFurnace) {
						Feedback.destructive(sender, "Untrusted %s from this Furnace!", targetDisplayName);
					} else if (container instanceof TileEntityDispenser) {
						Feedback.destructive(sender, "Untrusted %s from this Dispenser!", targetDisplayName);
					} else if (container instanceof TileEntityMeshGold) {
						Feedback.destructive(sender, "Untrusted %s from this Golden Mesh!", targetDisplayName);
					} else if (container instanceof TileEntityTrommel) {
						Feedback.destructive(sender, "Untrusted %s from this Trommel!", targetDisplayName);
					} else if (container instanceof TileEntityBasket) {
						Feedback.destructive(sender, "Untrusted %s from this Basket!", targetDisplayName);
					} else if (container instanceof TileEntityActivator) {
						Feedback.destructive(sender, "Untrusted %s from this Activator!", targetDisplayName);
					}
					lockable.removeTrustedPlayer(targetUUID);
				} else {
					Feedback.error(sender, "Failed to Untrust %s from Container! (Container not Locked)", targetDisplayName);
				}
				return Command.SINGLE_SUCCESS;
			}
		}
		Feedback.error(sender, "Failed to Untrust %s from Container! (Not Looking at Container)", targetDisplayName);
		return Command.SINGLE_SUCCESS;
	}

	public static int lockUntrustAll(PlayerServer sender, String targetUsername){
		PlayerServer target = MinecraftServer.getInstance().playerList.getPlayerEntity(targetUsername);
		UUID targetUUID;

		if(target != null){
			targetUUID = target.uuid;
			String targetDisplayName = target.getDisplayName();
			if(Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.containsKey(targetUUID)){
				Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.remove(targetUUID);
				Data.Users.save(sender.uuid);
				Feedback.destructive(sender, "Untrusted %s from all Containers!", targetDisplayName);
				return Command.SINGLE_SUCCESS;
			}
			Feedback.error(sender, "Failed to Untrust %s from all Containers! (Player is Not Trusted)", targetDisplayName);
			return Command.SINGLE_SUCCESS;
		} else {
			UUIDHelper.runConversionAction(targetUsername, targetuuid -> {
				if(Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.containsKey(targetuuid)){

					Data.Users.getOrCreate(sender.uuid).usersTrustedToAllContainers.remove(targetuuid, targetUsername);
					Data.Users.save(sender.uuid);
					Feedback.destructive(sender, "Untrusted %s from all Containers!", targetUsername);
					return;
				}
				Feedback.error(sender, "Failed to Untrust %s from all Containers! (Player is Not Trusted)", targetUsername);
			}, username -> Feedback.error(sender, "Failed to Untrust %s from all Containers! (Player Does not Exist)", targetUsername));
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int lockUntrustCommunity(PlayerServer sender){
		HitResult rayCastResult = LockUtil.rayCastFromPlayer(sender);
		if (!(rayCastResult instanceof HitResult.Tile)) {
			Feedback.error(sender, "Failed to Untrust Community from Container! (Not Looking at Container)");
			return Command.SINGLE_SUCCESS;
		}

		TileEntity container = sender.world.getTileEntity(((HitResult.Tile) rayCastResult).tilePos);

		if(container != null) {
			if (container instanceof Lockable) {
				Lockable lockable = ((Lockable) container);
				if (lockable.getIsLocked()) {

					if (!lockable.getLockOwner().equals(sender.uuid)) {
						Feedback.error(sender, "Failed to Untrust Community from Container! (Not Owned By You)");
						return Command.SINGLE_SUCCESS;
					}

					if (!lockable.getIsCommunityContainer()){
						Feedback.error(sender, "Failed to Untrust Community from Container! (Community not Trusted)");
						return Command.SINGLE_SUCCESS;
					}

					if (container instanceof TileEntityChest) {
						Lockable otherLockable = (Lockable) LockUtil.getOtherChest(sender.world, (TileEntityChest) container);
						if (otherLockable != null) {
							lockable.setIsCommunityContainer(false);
							otherLockable.setIsCommunityContainer(false);
							Feedback.destructive(sender, "Untrusted Community from this Double Chest!");
							return Command.SINGLE_SUCCESS;
						}
						Feedback.destructive(sender, "Untrusted Community from this Chest!");
					} else if (container instanceof TileEntityFurnaceBlast) {
						Feedback.destructive(sender, "Untrusted Community from this Blast Furnace!");
					} else if (container instanceof TileEntityFurnace) {
						Feedback.destructive(sender, "Untrusted Community from this Furnace!");
					} else if (container instanceof TileEntityDispenser) {
						Feedback.destructive(sender, "Untrusted Community from this Dispenser!");
					} else if (container instanceof TileEntityMeshGold) {
						Feedback.destructive(sender, "Untrusted Community from this Golden Mesh!");
					} else if (container instanceof TileEntityTrommel) {
						Feedback.destructive(sender, "Untrusted Community from this Trommel!");
					} else if (container instanceof TileEntityBasket) {
						Feedback.destructive(sender, "Untrusted Community from this Basket!");
					} else if (container instanceof TileEntityActivator) {
						Feedback.destructive(sender, "Untrusted Community from this Activator!");
					}
					lockable.setIsCommunityContainer(false);
				} else {
					Feedback.error(sender, "Failed to Untrust Community from Container!");
					Feedback.error(sender, "(Container not Locked)");
				}
				return Command.SINGLE_SUCCESS;
			}
		}
		Feedback.error(sender, "Failed to Untrust Community from Container!");
		Feedback.error(sender, "(Not Looking at Container)");
		return Command.SINGLE_SUCCESS;
	}

	public static int lockBypass(PlayerServer sender){
		UUID senderUUID = sender.uuid;

		if(Data.Users.getOrCreate(senderUUID).lockBypass){
			Data.Users.getOrCreate(senderUUID).lockBypass = false;
			Data.Users.save(senderUUID);
			Feedback.destructive(sender, "Lock Bypass Disabled");
		} else {
			Data.Users.getOrCreate(senderUUID).lockBypass = true;
			Data.Users.save(senderUUID);
			Feedback.success(sender, "Lock Bypass Enabled!");
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int lockInfo(PlayerServer sender){
		HitResult rayCastResult = LockUtil.rayCastFromPlayer(sender);
		if (!(rayCastResult instanceof HitResult.Tile)) {
			Feedback.error(sender, "Failed to get info from Container! (Not Looking at Container)");
			return Command.SINGLE_SUCCESS;
		}

		TileEntity container = sender.world.getTileEntity(((HitResult.Tile) rayCastResult).tilePos);

		if(container != null) {
			if (container instanceof Lockable) {
				Lockable lockable = ((Lockable) container);
					if (container instanceof TileEntityChest) {
						Lockable otherLockable = (Lockable) LockUtil.getOtherChest(sender.world, (TileEntityChest) container);
						if (otherLockable != null) {
							LockUtil.sendContainerLockInfo(sender, lockable, "Double Chest");
							return Command.SINGLE_SUCCESS;
						}
						LockUtil.sendContainerLockInfo(sender, lockable, "Chest");
					} else if (container instanceof TileEntityFurnaceBlast) {
						LockUtil.sendContainerLockInfo(sender, lockable, "Blast Furnace");
					} else if (container instanceof TileEntityFurnace) {
						LockUtil.sendContainerLockInfo(sender, lockable, "Furnace");
					} else if (container instanceof TileEntityDispenser) {
						LockUtil.sendContainerLockInfo(sender, lockable, "Dispenser");
					} else if (container instanceof TileEntityMeshGold) {
						LockUtil.sendContainerLockInfo(sender, lockable, "Gold Mesh");
					} else if (container instanceof TileEntityTrommel) {
						LockUtil.sendContainerLockInfo(sender, lockable, "Trommel");
					} else if (container instanceof TileEntityBasket) {
						LockUtil.sendContainerLockInfo(sender, lockable, "Basket");
					} else if (container instanceof TileEntityActivator) {
						LockUtil.sendContainerLockInfo(sender, lockable, "Activator");
					}
				return Command.SINGLE_SUCCESS;
			}
		}
		Feedback.error(sender, "Failed to get info from Container! (Not Looking at Container)");
		return Command.SINGLE_SUCCESS;
	}
}
