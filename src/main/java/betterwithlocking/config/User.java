package betterwithlocking.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User {

	public boolean lockOnBlockPlaced = true;
	public boolean lockOnBlockPunched = false;
	public boolean lockBypass = false;
	public boolean lockFeedback = true;
	public UUID uuid;
	public Map<UUID, String> usersTrustedToAllContainers = new HashMap<>();

	public User(UUID uuid) {
		this.uuid = uuid;
	}
}
