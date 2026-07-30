package betterwithlocking.util;

import betterwithlocking.Lockable;
import melib.util.Profiles;
import betterwithlocking.config.Data;
import net.minecraft.core.block.BlockLogicChest;
import net.minecraft.core.block.entity.*;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.util.phys.Vec3;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LockUtil {

	public static HitResult rayCastFromPlayer(PlayerServer sender) {
		float f = 1.0f;
		float f1 = sender.xRotO + (sender.xRot - sender.xRotO) * f;
		float f2 = sender.yRotO + (sender.yRot - sender.yRotO) * f;
		double posX = sender.xo + (sender.x - sender.xo) * (double) f;
		float yOff = sender instanceof PlayerServer ? sender.getHeadHeight() : 0.0f;
		double posY = sender.yo + (sender.y - sender.yo) + (double) yOff;
		double posZ = sender.zo + (sender.z - sender.zo) * (double) f;
		Vec3 vec3 = Vec3.getTempVec3(posX, posY, posZ);
		float f3 = MathHelper.cos(-f2 * 0.01745329f - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.01745329f - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.01745329f);
		float f6 = MathHelper.sin(-f1 * 0.01745329f);
		float f7 = f4 * f5;
		float f8 = f6;
		float f9 = f3 * f5;
		double reachDistance = sender.getGamemode().getBlockReachDistance();
		Vec3 vec3_1 = vec3.add((double) f7 * reachDistance, (double) f8 * reachDistance, (double) f9 * reachDistance);
		return sender.world.checkBlockCollisionBetweenPoints(vec3.asJomlVec(), vec3_1.asJomlVec(), false);
	}

	public static @Nullable TileEntityChest getOtherChest(@NotNull World world, @NotNull TileEntityChest chest){
		int meta = world.getBlockMetadata(chest.tilePos.x, chest.tilePos.y, chest.tilePos.z);
		BlockLogicChest.Type type = BlockLogicChest.getTypeFromMeta(meta);
		if (type != BlockLogicChest.Type.SINGLE) {
			Direction direction = BlockLogicChest.getDirectionFromMeta(meta);
			int otherChestX = chest.tilePos.x;
			int otherChestZ = chest.tilePos.z;
			if (direction == Direction.NORTH) {
				if (type == BlockLogicChest.Type.LEFT) {
					--otherChestX;
				}
				if (type == BlockLogicChest.Type.RIGHT) {
					++otherChestX;
				}
			}
			if (direction == Direction.EAST) {
				if (type == BlockLogicChest.Type.LEFT) {
					--otherChestZ;
				}
				if (type == BlockLogicChest.Type.RIGHT) {
					++otherChestZ;
				}
			}
			if (direction == Direction.SOUTH) {
				if (type == BlockLogicChest.Type.LEFT) {
					++otherChestX;
				}
				if (type == BlockLogicChest.Type.RIGHT) {
					--otherChestX;
				}
			}
			if (direction == Direction.WEST) {
				if (type == BlockLogicChest.Type.LEFT) {
					++otherChestZ;
				}
				if (type == BlockLogicChest.Type.RIGHT) {
					--otherChestZ;
				}
			}
			return (TileEntityChest) world.getTileEntity(otherChestX, chest.tilePos.y, otherChestZ);
		}
		//return's null if chest is a single chest
		return  null;
	}

	public static String getContainerName(TileEntity container) {
		if (container instanceof TileEntityChest) return "Chest";
		if (container instanceof TileEntityFurnaceBlast) return "Blast Furnace";
		if (container instanceof TileEntityFurnace) return "Furnace";
		if (container instanceof TileEntityDispenser) return "Dispenser";
		if (container instanceof TileEntityMeshGold) return "Golden Mesh";
		if (container instanceof TileEntityTrommel) return "Trommel";
		if (container instanceof TileEntityBasket) return "Basket";
		if (container instanceof TileEntityActivator) return "Activator";
		return "Container";
	}

	//locks a freshly placed container to its placer if they have lockOnBlockPlaced enabled
	public static void lockOnPlace(World world, TilePosc pos, Mob mob) {
		if (!(mob instanceof PlayerServer player)) return;
		if (!Data.Users.getOrCreate(player.uuid).lockOnBlockPlaced) return;

		TileEntity container = world.getTileEntity(pos);
		if (!(container instanceof Lockable lockable)) return;
		if (lockable.getIsLocked()) return;

		lockable.setIsLocked(true);
		lockable.setLockOwner(player.uuid);
		Feedback.success(player, "Locked " + getContainerName(container) + "!");
	}

	public static void sendContainerLockInfo(PlayerServer player, @NotNull Lockable lockable, String containerName) {
		if (!lockable.getIsLocked()) {
			player.sendMessage(TextFormatting.GRAY + "< " + TextFormatting.LIGHT_GRAY + containerName + ": " + TextFormatting.RED + "Not Locked " + TextFormatting.GRAY + ">");
			Feedback.playSound(player, "note.bd");
			return;
		}

		new Thread(() -> {
			String owner = Profiles.getNameFromUUID(lockable.getLockOwner());
			Map<String, Boolean> trustedPlayers = new HashMap<>();
			for(Map.Entry<UUID, Boolean> entry : lockable.getAllTrustedPlayers().entrySet()){
				trustedPlayers.put(Profiles.getNameFromUUID(entry.getKey()), entry.getValue());
			}
			containerLockInfoLogic(player, lockable, containerName, owner, trustedPlayers);
		}).start();
	}

	private static void containerLockInfoLogic(@NotNull PlayerServer player, @NotNull Lockable lockable, String containerName, String owner, @NotNull Map<String, Boolean> trustedPlayers){
		player.sendMessage(TextFormatting.GRAY + "< " + TextFormatting.LIGHT_GRAY + containerName + ": " + TextFormatting.GRAY + ">" + TextFormatting.ORANGE + " * " + TextFormatting.GRAY + "=" + TextFormatting.LIGHT_GRAY + " In " + owner + "'s TrustAll List");
		player.sendMessage(TextFormatting.GRAY + "  > " + TextFormatting.LIGHT_GRAY + "Owner: " + TextFormatting.GRAY + "[" + TextFormatting.LIGHT_GRAY + owner + TextFormatting.GRAY + "]");
		player.sendMessage(TextFormatting.GRAY + "  > " + TextFormatting.LIGHT_GRAY + "Community Container: " + TextFormatting.GRAY + "[" + TextFormatting.LIGHT_GRAY + lockable.getIsCommunityContainer() + TextFormatting.GRAY + "]");
		player.sendMessage(TextFormatting.GRAY + "  > " + TextFormatting.LIGHT_GRAY + "Trusted Players: ");
		for(Map.Entry<String, Boolean> entry : trustedPlayers.entrySet()){
			if(entry.getValue()){
				player.sendMessage(TextFormatting.GRAY + "    > [" + TextFormatting.LIGHT_GRAY + entry.getKey() + TextFormatting.GRAY + "]" + TextFormatting.ORANGE + "*");
			} else {
				player.sendMessage(TextFormatting.GRAY + "    > [" + TextFormatting.LIGHT_GRAY + entry.getKey() + TextFormatting.GRAY + "]");
			}
		}
		player.sendMessage(TextFormatting.GRAY + "<>");
		Feedback.playSound(player, "note.snare");
	}
}
