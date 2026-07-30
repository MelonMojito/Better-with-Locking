package betterwithlocking.mixin;

import betterwithlocking.Lockable;
import betterwithlocking.config.Data;
import betterwithlocking.util.Feedback;
import betterwithlocking.util.LockUtil;
import net.minecraft.core.block.entity.*;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketBlockUpdate;
import net.minecraft.core.net.packet.PacketPlayerAction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import net.minecraft.server.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketHandlerServer.class, remap = false)
public abstract class PacketHandlerServerMixin {
	@Shadow
	private PlayerServer playerEntity;

	@Shadow
	private MinecraftServer mcServer;

	@Shadow
	public abstract void sendPacket(Packet packet);

	@Inject(
		at = @At("HEAD"),
		method = "handlePlayerAction",
		cancellable = true)
	private void handleBlockDigInject(@NotNull PacketPlayerAction packetPlayerAction, CallbackInfo ci){
		PlayerServer player = this.playerEntity;
		WorldServer world = this.mcServer.getDimensionWorld(player.dimension);
		TileEntity container = world.getTileEntity(packetPlayerAction.xPosition, packetPlayerAction.yPosition, packetPlayerAction.zPosition);
		if(container instanceof Lockable) {
			Lockable lockable = (Lockable) world.getTileEntity(packetPlayerAction.xPosition, packetPlayerAction.yPosition, packetPlayerAction.zPosition);
			if (lockable.getLockOwner() != null
				&& !lockable.getLockOwner().equals(player.uuid)
				&& !lockable.getTrustedPlayers().contains(player.uuid)
				&& !Data.Users.getOrCreate(lockable.getLockOwner()).usersTrustedToAllContainers.containsKey(player.uuid)
				&& !Data.Users.getOrCreate(player.uuid).lockBypass){
				ci.cancel();
				sendPacket(new PacketBlockUpdate(packetPlayerAction.xPosition, packetPlayerAction.yPosition, packetPlayerAction.zPosition, world));
				return;
			}

			if (packetPlayerAction.action == PacketPlayerAction.ACTION_DIG_START
				&& Data.Users.getOrCreate(player.uuid).lockOnBlockPunched
				&& lockable.getIsLocked()
				&& !lockable.getLockOwner().equals(player.uuid))
			{
				Feedback.error(player, "Failed to Lock Container! (Not Owned By You)");
				ci.cancel();
				return;
			}

			if (packetPlayerAction.action == PacketPlayerAction.ACTION_DIG_START
				&& Data.Users.getOrCreate(player.uuid).lockOnBlockPunched
				&& lockable.getIsLocked()
				&& lockable.getLockOwner().equals(player.uuid))
			{
				Feedback.error(player, "Failed to Lock Container! (Already Locked)");
				ci.cancel();
				return;
			}

			if(packetPlayerAction.action == PacketPlayerAction.ACTION_DIG_START && Data.Users.getOrCreate(player.uuid).lockOnBlockPunched && !lockable.getIsLocked()){
				if (container instanceof TileEntityChest) {
					Lockable iOtherContainer = (Lockable) LockUtil.getOtherChest(world, (TileEntityChest) container);
					if (iOtherContainer != null) {
						lockable.setIsLocked(true);
						iOtherContainer.setIsLocked(true);
						lockable.setLockOwner(player.uuid);
						iOtherContainer.setLockOwner(player.uuid);
						Feedback.success(player, "Locked Double Chest!");
						ci.cancel();
						return;
					} else {
						Feedback.success(player, "Locked Chest!");
					}
				} else if (container instanceof TileEntityFurnaceBlast) {
					Feedback.success(player, "Locked Blast Furnace!");
				} else if (container instanceof TileEntityFurnace) {
					Feedback.success(player, "Locked Furnace!");
				} else if (container instanceof TileEntityDispenser) {
					Feedback.success(player, "Locked Dispenser!");
				} else if (container instanceof TileEntityMeshGold) {
					Feedback.success(player, "Locked Golden Mesh!");
				} else if (container instanceof TileEntityTrommel) {
					Feedback.success(player, "Locked Trommel!");
				} else if (container instanceof TileEntityBasket) {
					Feedback.success(player, "Locked Basket!");
				} else if (container instanceof TileEntityActivator) {
					Feedback.success(player, "Locked Activator!");
				}

				lockable.setIsLocked(true);
				lockable.setLockOwner(player.uuid);
				ci.cancel();
				return;
			}
		}
	}
}
