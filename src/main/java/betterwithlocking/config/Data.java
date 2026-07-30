package betterwithlocking.config;

import betterwithlocking.BetterWithLocking;
import com.b100.utils.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Data {

	public static Gson gson = new GsonBuilder()
		.registerTypeAdapter(User.class, new UserJsonAdapter())
		.setPrettyPrinting().create();

	public static class Users {

		public static final Map<UUID, User> userDataHashMap = new HashMap<>();
		private static final String userDir = FabricLoader.getInstance().getConfigDir() + "/" + BetterWithLocking.MOD_ID + "/users";

		public static User create(UUID uuid){
			User user = new User(uuid);
			userDataHashMap.put(uuid, user);
			save(uuid);
			return user;
		}
		public static void delete(UUID uuid){
			userDataHashMap.remove(uuid);
			if(!new File(userDir, uuid + ".json").delete()){
				BetterWithLocking.LOGGER.error("Could not delete file for User {}", uuid);
			}
		}
		public static void reload(){
			userDataHashMap.clear();
			File dir = new File(userDir);
			dir.mkdirs();
			File[] files = dir.listFiles();
			if (files != null) {
				for (File child : files) {
					try {
						User user = gson.fromJson(new JsonReader(new FileReader(child)), User.class);
						userDataHashMap.put(user.uuid, user);
					} catch (FileNotFoundException e) {
						BetterWithLocking.LOGGER.error("Could not find file {} while trying to reload Users!", child);
						continue;
					}
				}
			} else {
				BetterWithLocking.LOGGER.error("User config dir does not exist!");
			}
		}
		public static void save(UUID uuid){
			File file = FileUtils.createNewFile(new File(userDir, uuid + ".json"));
			try (FileWriter writer = new FileWriter(file)) {
				gson.toJson(getOrCreate(uuid), User.class, writer);
			} catch (IOException e) {
				BetterWithLocking.LOGGER.error("User {} failed to save!", uuid);
			}
		}
		public static User getOrCreate(UUID uuid){
			if(!userDataHashMap.containsKey(uuid)){
				create(uuid);
			}
			return get(uuid);
		}

		public static User get(UUID uuid) {
			return userDataHashMap.get(uuid);
		}
	}
}
