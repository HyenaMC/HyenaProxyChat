package org.teamhyena.proxy.chat.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.teamhyena.proxy.chat.text.Translates;
import org.teamhyena.proxy.chat.util.ComponentUtils;

import static org.teamhyena.proxy.chat.config.HyenaProxyChatConfig.CONFIG;
import static org.teamhyena.proxy.chat.util.Utils.PLAYER_UTIL;
import static org.teamhyena.proxy.chat.util.Utils.PROXY_SERVER;

public class TellCommand {
	public static void register() {
		CommandManager commandManager = PROXY_SERVER.getCommandManager();
		BrigadierCommand command = new BrigadierCommand(LiteralArgumentBuilder
			.<CommandSource>literal("tell")
			.requires(source -> CONFIG.isEnableCommandTell())
			.then(RequiredArgumentBuilder
				.<CommandSource, String>argument("target", StringArgumentType.string())
				.suggests((context, builder) -> {
					PROXY_SERVER.getAllPlayers().stream().map(Player::getUsername).forEach(builder::suggest);
					return builder.buildFuture();
				})
				.then(RequiredArgumentBuilder
					.<CommandSource, String>argument("msg", StringArgumentType.greedyString())
					.executes(TellCommand::execute))));
		commandManager.register(command);
	}

	public static int execute(CommandContext<CommandSource> context) {
		if (!CONFIG.isEnableCommandTell()) {
			return 0;
		}
		CommandSource source = context.getSource();
		// 发送私聊
		Component sourceServerComponent;
		if (source instanceof Player player) {
			sourceServerComponent = player.getCurrentServer()
				.map(ServerConnection::getServer)
				.map(ComponentUtils::getServerComponent)
				.orElse(Translates.SERVER_NOT_FOUND_NAME);
		} else {
			sourceServerComponent = Translates.PROXY_NAME;
		}
		String targetPlayerName = context.getArgument("target", String.class);
		PLAYER_UTIL.getPlayerByName(targetPlayerName).ifPresent(targetPlayer -> {
			if (source instanceof Player player &&
				PLAYER_UTIL.hasTheSameServer(player, targetPlayer)) {
				return;
			}
			Component targetServerComponent = targetPlayer.getCurrentServer()
				.map(ServerConnection::getServer)
				.map(ComponentUtils::getServerComponent)
				.orElse(Translates.SERVER_NOT_FOUND_NAME);
			String msg = context.getArgument("msg", String.class);
			TextComponent tellMessage = Component.text(msg);
			targetPlayer.sendMessage(Translates.TELL_MESSAGE.args(
				source instanceof Player player ? ComponentUtils.getPlayerComponent(player)
					: Component.text("§4[Proxy]§r"),
				tellMessage,
				sourceServerComponent,
				targetServerComponent
			));
			// 发送反馈
			source.sendMessage(Translates.TELL_RESPONSE.args(
				ComponentUtils.getPlayerComponent(targetPlayer),
				tellMessage,
				sourceServerComponent,
				targetServerComponent
			));
		});
		return 1;
	}
}
