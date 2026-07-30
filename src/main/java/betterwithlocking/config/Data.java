package betterwithlocking.config;

import betterwithlocking.BetterWithLocking;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import melib.config.FileStore;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class Data {

	public static Gson gson = new GsonBuilder()
		.registerTypeAdapter(User.class, new UserJsonAdapter())
		.setPrettyPrinting().create();

	private static final String configDir = FabricLoader.getInstance().getConfigDir() + "/" + BetterWithLocking.MOD_ID;

	public static class Users {

		private static final FileStore<UUID, User> STORE =
			FileStore.byUuid(new File(configDir, "users"), gson, User.class, User::new, user -> user.uuid);

		public static User get(UUID uuid){
			return STORE.get(uuid);
		}
		public static User getOrCreate(UUID uuid){
			return STORE.getOrCreate(uuid);
		}
		public static void save(UUID uuid){
			STORE.save(uuid);
		}
		public static void delete(UUID uuid){
			STORE.delete(uuid);
		}
		public static void reload(){
			STORE.reload();
		}
		public static Map<UUID, User> map(){
			return STORE.map();
		}
	}
}
