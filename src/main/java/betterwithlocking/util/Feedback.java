package betterwithlocking.util;

import betterwithlocking.config.Data;
import net.minecraft.core.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Melib's feedback, gated on the player's {@code /lock toggle feedback} setting.
 *
 * <p>{@code /lock info} deliberately bypasses this by sending its lines directly.
 */
public class Feedback {

	public static void success(@NotNull Player player, @NotNull String message, Object... args) {
		if (muted(player)) return;
		melib.util.Feedback.success(player, message, args);
	}

	public static void successSilent(@NotNull Player player, @NotNull String message, Object... args) {
		if (muted(player)) return;
		melib.util.Feedback.successSilent(player, message, args);
	}

	public static void error(@NotNull Player player, @NotNull String message, Object... args) {
		if (muted(player)) return;
		melib.util.Feedback.error(player, message, args);
	}

	public static void destructive(@NotNull Player player, @NotNull String message, Object... args) {
		if (muted(player)) return;
		melib.util.Feedback.destructive(player, message, args);
	}

	/** Ungated: used by /lock info, which is exempt from the feedback toggle. */
	public static void playSound(@NotNull Player player, @NotNull String soundPath) {
		melib.util.Feedback.playSound(player, soundPath);
	}

	private static boolean muted(@NotNull Player player) {
		return !Data.Users.getOrCreate(player.uuid).lockFeedback;
	}
}
