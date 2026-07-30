package betterwithlocking.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.entity.player.PlayerServer;

public class CommandUnlock implements CommandManager.CommandRegistry{
	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(ArgumentBuilderLiteral.<CommandSource>literal("unlock")
			.executes(context ->
				{
					PlayerServer sender = (PlayerServer) context.getSource().getSender(); if(sender == null){return 0;}
					return CommandLogicUnlock.unlock(sender);
				}
			)
		);
	}
}
