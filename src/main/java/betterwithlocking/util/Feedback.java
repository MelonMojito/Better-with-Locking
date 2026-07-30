package betterwithlocking.util;

import betterwithlocking.BetterWithLocking;
import betterwithlocking.config.Data;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketPlaySoundDirect;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.sound.SoundTypes;
import net.minecraft.server.entity.player.PlayerServer;
import org.jetbrains.annotations.NotNull;

public class Feedback {

	public static void success(@NotNull PlayerServer player, @NotNull String message, Object... args) {
		send(player, TextFormatting.LIME, "note.harp", message, args);
	}

	public static void successSilent(@NotNull PlayerServer player, @NotNull String message, Object... args) {
		send(player, TextFormatting.LIME, null, message, args);
	}

	public static void error(@NotNull PlayerServer player, @NotNull String message, Object... args) {
		send(player, TextFormatting.RED, "note.bd", message, args);
	}

	public static void destructive(@NotNull PlayerServer player, @NotNull String message, Object... args) {
		send(player, TextFormatting.ORANGE, "note.snare", message, args);
	}

	private static void send(@NotNull PlayerServer player, @NotNull TextFormatting color, String soundPath, @NotNull String message, Object... args) {
		//players who toggled lock feedback off get no text or sound (/lock info bypasses this by sending directly)
		if (!Data.Users.getOrCreate(player.uuid).lockFeedback) return;

		player.sendMessage(format(color, message, args));
		BetterWithLocking.LOGGER.info(String.format("Sent command feedback: [%s] to player: [username: %s, uuid: %s]", formatRaw(message, args), player.username, player.uuid));
		if (soundPath != null) {
			playSound(player, soundPath);
		}
	}

	public static @NotNull String format(@NotNull TextFormatting color, @NotNull String message, Object... args) {
		Object[] argStrings = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			argStrings[i] = "" + TextFormatting.GRAY + "[" + TextFormatting.LIGHT_GRAY + args[i] + TextFormatting.RESET + TextFormatting.GRAY + "]" + color;
		}
		return color + String.format(message, argStrings);
	}

	private static @NotNull String formatRaw(@NotNull String message, Object... args) {
		Object[] argStrings = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			argStrings[i] = "[" + args[i] + "]";
		}
		return String.format(message, argStrings);
	}

	public static void playSound(@NotNull PlayerServer player, @NotNull String soundPath) {
		player.playerNetServerHandler.sendPacket(
			new PacketPlaySoundDirect(
				SoundTypes.getSoundId(soundPath),
				SoundCategory.GUI_SOUNDS,
				player.x,
				player.y,
				player.z,
				1f,
				1f
			)
		);
	}
}
