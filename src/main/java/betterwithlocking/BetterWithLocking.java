package betterwithlocking;

import betterwithlocking.command.CommandLock;
import betterwithlocking.command.CommandUnlock;
import betterwithlocking.config.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.net.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;

public class BetterWithLocking implements ModInitializer {

	public static final String MOD_ID = HalpLibe.registerMod("betterwithlocking", true);
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final boolean isServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;

	@Override
	public void onInitialize() {
		LOGGER.info("Better with Locking initializing!");
		Data.Users.reload();
		LOGGER.info("Better with Locking initialized!");
	}

	public static void registerServerCommands() {
		CommandManager.registerCommand(new CommandLock());
		CommandManager.registerCommand(new CommandUnlock());
	}

	public static void registerClientCommands() {
	}

	public static void afterServerStart() {
		if (isServer) {
			Data.Users.reload();
		}
	}
}
