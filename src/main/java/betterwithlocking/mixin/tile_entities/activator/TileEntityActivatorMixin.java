package betterwithlocking.mixin.tile_entities.activator;

import betterwithlocking.config.Data;
import betterwithlocking.Lockable;
import betterwithlocking.util.Feedback;
import betterwithlocking.LockManager;
import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import com.mojang.nbt.tags.Tag;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.packet.PacketSetHeldObject;
import net.minecraft.core.util.helper.UUIDHelper;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.core.world.ICarriable;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = TileEntityActivator.class, remap = false)
public class TileEntityActivatorMixin implements Lockable {
	@Unique
	private boolean isLocked;

	@Unique
	private boolean isCommunityContainer;

	@Unique
	private UUID lockOwner;

	@Unique
	private final List<UUID> trustedPlayers = new ArrayList<>();

	@Inject(at = @At("TAIL"), method = "writeAdditionalData")
	public void writeToNBTInject(@NotNull CompoundTag compoundTag, CallbackInfo ci){
		compoundTag.putBoolean("isLocked", isLocked);
		UUIDHelper.writeToTag(compoundTag, lockOwner, "lockOwner");
		compoundTag.putBoolean("isCommunityContainer", isCommunityContainer);

		ListTag trustedPlayers = new ListTag();
		for(UUID uuid : this.trustedPlayers){
			CompoundTag cTag = new CompoundTag();
			UUIDHelper.writeToTag(cTag, uuid, "uuid");
			trustedPlayers.addTag(cTag);
		}
		compoundTag.putList("trustedPlayers", trustedPlayers);
	}

	@Inject(at = @At("TAIL"), method = "readAdditionalData")
	public void readFromNBTInject(@NotNull CompoundTag compoundTag, CallbackInfo ci){
		isLocked = compoundTag.getBooleanOrDefault("isLocked", false);
		lockOwner = UUIDHelper.readFromTag(compoundTag, "lockOwner");
		isCommunityContainer = compoundTag.getBooleanOrDefault("isCommunityContainer", false);

		ListTag tempListTag = compoundTag.getList("trustedPlayers");

		for(Tag<?> tag : tempListTag){
			if(tag instanceof CompoundTag ctag){
				trustedPlayers.add(UUIDHelper.readFromTag(ctag, "uuid"));
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "pickup", cancellable = true)
	public void canBeCarriedInject(World world, Entity holder, TilePosc tilePos_, CallbackInfoReturnable<ICarriable> cir){
		if(holder instanceof PlayerServer && LockManager.determineAuthStatus(this, (PlayerServer) holder) <= LockManager.COMMUNITY){
			Feedback.error((PlayerServer) holder, "Failed to Pickup Container! (Not Authorized)");
			((PlayerServer) holder).playerNetServerHandler.sendPacket(new PacketSetHeldObject(holder.id, holder.getHeldObject()));
			cir.setReturnValue(null);
			return;
		}
	}

	@Override
	public boolean getIsLocked() {
		return isLocked;
	}

	@Override
	public boolean getIsCommunityContainer() {
		return isCommunityContainer;
	}

	@Override
	public void setIsLocked(boolean flag) {
		isLocked = flag;
	}

	@Override
	public void setIsCommunityContainer(boolean flag) {
		isCommunityContainer = flag;
	}

	@Override
	public UUID getLockOwner() {
		return lockOwner;
	}

	@Override
	public void setLockOwner(UUID owner) {
		lockOwner = owner;
	}

	@Override
	public List<UUID> getTrustedPlayers() {
		return trustedPlayers;
	}

	@Override
	public Map<UUID, Boolean> getAllTrustedPlayers() {
		Map<UUID, Boolean> tempTrustedPlayers = new HashMap<>(Collections.emptyMap());
		for(UUID uuid : trustedPlayers){
			tempTrustedPlayers.put(uuid, false);
		}
		for(Map.Entry<UUID, String> entry : Data.Users.getOrCreate(lockOwner).usersTrustedToAllContainers.entrySet()){
			tempTrustedPlayers.put(entry.getKey(), true);
		}
		return tempTrustedPlayers;
	}

	@Override
	public void setTrustedPlayers(List<UUID> trustedPlayers) {
		this.trustedPlayers.clear();
		this.trustedPlayers.addAll(trustedPlayers);
	}

	@Override
	public void addTrustedPlayer(UUID uuid) {
		trustedPlayers.add(uuid);
	}

	@Override
	public void removeTrustedPlayer(UUID uuid) {
		trustedPlayers.remove(uuid);
	}
}
