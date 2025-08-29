package org.teamhyena.proxy.chat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fun.qu_an.lib.mc.util.FormatUtils;
import fun.qu_an.lib.util.CharacterUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.teamhyena.proxy.chat.HyenaProxyChatPlugin;
import org.teamhyena.proxy.chat.util.Utils;

import java.util.List;
import java.util.Optional;

import static com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied;
import static org.teamhyena.proxy.chat.config.HyenaProxyChatConfig.CONFIG;

public class PlayerChatListener {
	private static final Logger logger = HyenaProxyChatPlugin.getLogger();

	@Subscribe()
	public void onPlayerChat(@NotNull PlayerChatEvent event) {
		if (!CONFIG.isDefaultGlobalChat()) { // 是否接管聊天
			return;
		}
		// 获取玩家发送的消息
		String playerMessage = event.getMessage();
		if (CONFIG.isColorableChat()) {
			playerMessage = FormatUtils.replaceFormattingCode(playerMessage);
		}

		// 获取玩家信息
		Player player = event.getPlayer();
		// 获取玩家名称
		String playerName = player.getUsername();
		// 获取服务器ID
		Optional<ServerConnection> currentServerOptional = player.getCurrentServer();
		RegisteredServer currentServer;
		String serverId;
		if (currentServerOptional.isPresent()) {
			currentServer = currentServerOptional.get().getServer();
			serverId = currentServer.getServerInfo().getName();
		} else {
			currentServer = null;
			serverId = "";
		}

		// 如果是MCDR命令直接返回
		List<String> mcdrCommandPrefixes = CONFIG.getMcdrCommandPrefix();
		if (!mcdrCommandPrefixes.isEmpty()
			&& CharacterUtils.startsWithAny(playerMessage, mcdrCommandPrefixes)) {
			if (CONFIG.isLogMcdrCommands()) {
				logger.info("[mcdr:{}]<{}> {}", serverId, playerName, playerMessage);
			}
			return;
		}

		if (CONFIG.isOverwriteLocalChat()) {
			// 取消消息发送！
			event.setResult(denied());
			Utils.assembleAndConsume(player, playerMessage, currentServer, serverId,
				text -> Utils.sendToAllServersExcept(currentServer, text),
				text -> { if (currentServer != null) Utils.sendToServerPlayers(currentServer, text); }
			);
		} else {
			// 发送全局消息！
			Utils.assembleAndConsume(player, playerMessage, currentServer, serverId,
				text -> Utils.sendToAllServersExcept(currentServer, text)
			);
		}
		logger.info("[global:{}]<{}> {}", serverId, playerName, playerMessage);
	}
}
