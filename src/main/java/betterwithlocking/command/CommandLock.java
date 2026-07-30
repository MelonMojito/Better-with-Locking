package betterwithlocking.command;

import melib.command.ArgumentTypeUsername;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.entity.player.PlayerServer;

@SuppressWarnings("UnusedReturnValue")
public class CommandLock implements CommandManager.CommandRegistry{
	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockToggle(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		ArgumentBuilderLiteral<CommandSource> toggle = ArgumentBuilderLiteral.<CommandSource>literal("toggle");

		toggle.then(ArgumentBuilderLiteral.<CommandSource>literal("onblockplaced")
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicLock.lockOnBlockPlaced(sender);
				}
			)
		);

		toggle.then(ArgumentBuilderLiteral.<CommandSource>literal("onblockpunched")
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicLock.lockOnBlockPunched(sender);
				}
			)
		);

		toggle.then(ArgumentBuilderLiteral.<CommandSource>literal("bypass").requires(CommandSource::hasAdmin)
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicLock.lockBypass(sender);
				}
			)
		);

		toggle.then(ArgumentBuilderLiteral.<CommandSource>literal("feedback")
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicLock.toggleFeedback(sender);
				}
			)
		);

		builder.then(toggle);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockTrust(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.then(ArgumentBuilderLiteral.<CommandSource>literal("trust")
			.then(ArgumentBuilderRequired.<CommandSource, String>argument("username", ArgumentTypeUsername.string())
				.executes(context ->
					{
						PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
						String targetUsername = context.getArgument("username", String.class).toLowerCase();
						return CommandLogicLock.lockTrust(sender, targetUsername);
					}
				)
			)
		);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockTrustAll(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.then(ArgumentBuilderLiteral.<CommandSource>literal("trustall")
			.then(ArgumentBuilderRequired.<CommandSource, String>argument("username", ArgumentTypeUsername.string())
				.executes(context ->
					{
						PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
						String targetUsername = context.getArgument("username", String.class).toLowerCase();
						return CommandLogicLock.lockTrustAll(sender, targetUsername);
					}
				)
			)
		);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockTrustCommunity(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.then(ArgumentBuilderLiteral.<CommandSource>literal("trustcommunity")
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicLock.lockTrustCommunity(sender);
				}
			)
		);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockUntrust(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.then(ArgumentBuilderLiteral.<CommandSource>literal("untrust")
			.then(ArgumentBuilderRequired.<CommandSource, String>argument("username", ArgumentTypeUsername.string())
				.executes(context ->
					{
						PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
						String targetUsername = context.getArgument("username", String.class).toLowerCase();
						return CommandLogicLock.lockUntrust(sender, targetUsername);
					}
				)
			)
		);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockUntrustAll(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.then(ArgumentBuilderLiteral.<CommandSource>literal("untrustall")
			.then(ArgumentBuilderRequired.<CommandSource, String>argument("username", ArgumentTypeUsername.string())
				.executes(context ->
					{
						PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
						String targetUsername = context.getArgument("username", String.class).toLowerCase();
						return CommandLogicLock.lockUntrustAll(sender, targetUsername);
					}
				)
			)
		);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockUntrustCommunity(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.then(ArgumentBuilderLiteral.<CommandSource>literal("untrustcommunity")
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicLock.lockUntrustCommunity(sender);
				}
			)
		);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lockInfo(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.then(ArgumentBuilderLiteral.<CommandSource>literal("info")
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicLock.lockInfo(sender);
				}
			)
		);
		return builder;
	}

	public static ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> lock(ArgumentBuilder<CommandSource, ArgumentBuilderLiteral<CommandSource>> builder) {
		builder.executes(context ->
			{
				PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
				return CommandLogicLock.lock(sender);
			}
		);
		return builder;
	}


	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		ArgumentBuilderLiteral<CommandSource> builder = ArgumentBuilderLiteral.<CommandSource>literal("lock");

		lock(builder);
		lockToggle(builder);
		lockTrust(builder);
		lockTrustAll(builder);
		lockTrustCommunity(builder);
		lockUntrust(builder);
		lockUntrustAll(builder);
		lockUntrustCommunity(builder);
		lockInfo(builder);

		dispatcher.register(builder);
	}
}
