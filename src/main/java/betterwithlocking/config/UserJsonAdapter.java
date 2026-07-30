package betterwithlocking.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

/**
 * Reads/writes users in the same "Lock Data"/"User Data" layout MelonUtilities used,
 * so user files from the original mod can be dropped in unchanged.
 */
public class UserJsonAdapter implements JsonDeserializer<User>, JsonSerializer<User> {
	@Override
	public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();

		JsonObject userDataObj = obj.getAsJsonObject("User Data");
		User user = new User(UUID.fromString(userDataObj.get("userUUID").getAsString()));

		if(obj.has("Lock Data")){
			JsonObject lockDataObj = obj.getAsJsonObject("Lock Data");
			user.lockOnBlockPlaced = lockDataObj.get("lockOnBlockPlaced").getAsBoolean();
			user.lockOnBlockPunched = lockDataObj.get("lockOnBlockPunched").getAsBoolean();
			user.lockBypass = lockDataObj.get("lockBypass").getAsBoolean();
			if(lockDataObj.has("lockFeedback")){
				user.lockFeedback = lockDataObj.get("lockFeedback").getAsBoolean();
			}
			JsonObject usersTrustedToAllContainers = lockDataObj.getAsJsonObject("usersTrustedToAllContainers");
			for(Map.Entry<String, JsonElement> entry : usersTrustedToAllContainers.entrySet()){
				user.usersTrustedToAllContainers.put(UUID.fromString(entry.getKey()), entry.getValue().getAsString());
			}
		}

		return user;
	}

	@Override
	public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonObject lockData = new JsonObject();
		JsonObject usersTrustedToAllContainers = new JsonObject();
		JsonObject user = new JsonObject();

		lockData.addProperty("lockOnBlockPlaced", src.lockOnBlockPlaced);
		lockData.addProperty("lockOnBlockPunched", src.lockOnBlockPunched);
		lockData.addProperty("lockBypass", src.lockBypass);
		lockData.addProperty("lockFeedback", src.lockFeedback);

		for(Map.Entry<UUID, String> entry : src.usersTrustedToAllContainers.entrySet()){
			usersTrustedToAllContainers.addProperty(entry.getKey().toString(), entry.getValue());
		}
		lockData.add("usersTrustedToAllContainers", usersTrustedToAllContainers);
		obj.add("Lock Data", lockData);

		user.addProperty("userUUID", String.valueOf(src.uuid));
		obj.add("User Data", user);

		return obj;
	}
}
