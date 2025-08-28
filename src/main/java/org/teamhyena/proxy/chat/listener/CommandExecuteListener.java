package org.teamhyena.proxy.chat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fun.qu_an.lib.util.CharacterUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.teamhyena.proxy.chat.HyenaProxyChatPlugin;
import org.teamhyena.proxy.chat.command.Commands;
import org.teamhyena.proxy.chat.text.Translates;
import org.teamhyena.proxy.chat.util.ComponentUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult.denied;
import static com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult.forwardToServer;
import static org.teamhyena.proxy.chat.config.HyenaProxyChatConfig.CONFIG;
import static org.teamhyena.proxy.chat.util.Utils.PLAYER_UTIL;

public class CommandExecuteListener {
	private static final JoinConfiguration COMMA_AND_SPACE = JoinConfiguration.separator(Translates.COMMA_AND_SPACE.color(NamedTextColor.GRAY));
	private final ProxyServer proxyServer = HyenaProxyChatPlugin.getProxyServer();

	@Subscribe
	public void onCommandExecute(@NotNull CommandExecuteEvent event) {
		if (!event.getResult().isAllowed()
			|| !(event.getCommandSource() instanceof Player sourcePlayer)) {
			return;
		}

		// 接管一些指令

		String rawCommand = event.getCommand().trim();
		List<String> command = List.of(rawCommand.split(" "));
		int size = command.size();

		String rootCommand = command.get(0);
		if (rootCommand.equals("execute")) return;

		if (rawCommand.equals("server") && CONFIG.isCustomServerCommand()) {
			event.setResult(denied());

			Optional<RegisteredServer> serverOptional = sourcePlayer.getCurrentServer()
				.map(ServerConnection::getServer);
			RegisteredServer currentServer = serverOptional.orElse(null);
			String serverName = serverOptional
				.map(RegisteredServer::getServerInfo)
				.map(ServerInfo::getName).orElse(null);
			if (currentServer != null) {
				sourcePlayer.sendMessage(Translates.COMMAND_SERVER_CURRENT
					.args(ComponentUtils.getServerComponent(currentServer, 0, serverName)));
			}
			Collection<RegisteredServer> allServers = proxyServer.getAllServers();
			if (allServers.size() > 50) {
				sourcePlayer.sendMessage(Component.translatable("velocity.command.server-too-many", NamedTextColor.RED));
				return;
			}
			List<Component> servers = allServers.stream()
				.map(server -> ComponentUtils.getServerComponent(server, 0, serverName))
				.toList();
			sourcePlayer.sendMessage(Translates.COMMAND_SERVER_AVAILABLE
				.args(Component.join(COMMA_AND_SPACE, servers)));
			return;
		}

		if (rootCommand.equals("glist") && CONFIG.isCustomGlistCommand()) {
			event.setResult(denied());
			if (size == 1) { // glist
				sendTotalProxyCount(sourcePlayer);
				sourcePlayer.sendMessage(Translates.GLIST_TOTAL_COUNT);
			} else {
				String serverName = command.get(1);
				if (serverName.equalsIgnoreCase("all")) {  // glist all
					proxyServer.getAllServers().forEach(server ->
						sendServerPlayers(sourcePlayer, server, false));
				} else { // glist <server_id>
					proxyServer.getServer(serverName).ifPresentOrElse(
						server -> sendServerPlayers(sourcePlayer, server, true),
						() -> sourcePlayer.sendMessage(Component.translatable(
							"velocity.command.server-does-not-exist",
							NamedTextColor.RED,
							Component.text(serverName))));
				}
				sendTotalProxyCount(sourcePlayer);
			}
			return;
		}

		if (CharacterUtils.equalsAny(rootCommand, Commands.TELEPORT)) {
			if (!CONFIG.isEnableCommandTp()) {
				return;
			}
			if (size == 2) { // /tp <target>
				// 跨服tp
				if (PLAYER_UTIL.tpWithServerSwitch(sourcePlayer, command.get(1))) {
					event.setResult(denied());
				}
			} else if (size == 3 // /tp <source> <target>
					   && command.get(size - 2).equals(sourcePlayer.getUsername())) {
				// 跨服tp
				if (PLAYER_UTIL.tpWithServerSwitch(sourcePlayer, command.get(2))) {
					event.setResult(denied());
				}
			} // else: /tp <x> <y> <z>
			return;
		}

		if (CharacterUtils.equalsAny(rootCommand, Commands.TELL) && size >= 3) { // /tell <target> <message>...
			if (!CONFIG.isEnableCommandTell()) {
				event.setResult(forwardToServer());
				return;
			}
			String targetPlayerName = command.get(1);
			PLAYER_UTIL.getPlayerByName(targetPlayerName).ifPresentOrElse(targetPlayer -> {
				// 如果不在同个服务器则接管该指令的执行
				if (PLAYER_UTIL.hasTheSameServer(sourcePlayer, targetPlayer)) {
					event.setResult(forwardToServer());
				} // else use our own tell command
			}, () -> event.setResult(forwardToServer()));
		}
	}

	private void sendTotalProxyCount(Player target) {
		final int online = proxyServer.getPlayerCount();
		target.sendMessage((online == 1
			? Translates.GLIST_PLAYER_COUNT_SINGULAR
			: Translates.GLIST_PLAYER_COUNT_PLURAL)
			.args(Component.text(online, NamedTextColor.GREEN)));
	}

	private void sendServerPlayers(final Player target,
								   final RegisteredServer server, boolean force) {
		Collection<Player> playersConnected = server.getPlayersConnected();
		List<Component> components = playersConnected.stream()
			.map(ComponentUtils::getPlayerComponent)
			.toList();
		Component players = Component.join(COMMA_AND_SPACE, components);
		if (!players.children().isEmpty() || force) {
			target.sendMessage(Translates.GLIST_ENTRY.args(
				ComponentUtils.getServerComponent(server),
				Component.text(playersConnected.size(), NamedTextColor.GREEN),
				players));
		}
	}
}
