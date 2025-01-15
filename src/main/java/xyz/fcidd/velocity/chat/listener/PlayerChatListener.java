package xyz.fcidd.velocity.chat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fun.qu_an.lib.mc.util.FormatUtils;
import fun.qu_an.lib.util.CharacterUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xyz.fcidd.velocity.chat.VelocityChatPlugin;
import xyz.fcidd.velocity.chat.util.Utils;

import java.util.List;
import java.util.Optional;

import static com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied;
import static xyz.fcidd.velocity.chat.config.VelocityChatConfig.CONFIG;
import static xyz.fcidd.velocity.chat.util.Utils.PROXY_SERVER;

public class PlayerChatListener {
	private static final Logger logger = VelocityChatPlugin.getLogger();

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
		RegisteredServer currentServer = null;
		String serverId;
		if (currentServerOptional.isPresent()) {
			currentServer = currentServerOptional.get().getServer();
			serverId = currentServer.getServerInfo().getName();
		} else {
			serverId = "";
		}

		// 如果是MCDR命令直接返回
		List<String> mcdrCommandPrefixes = CONFIG.getMcdrCommandPrefix();
		if (!mcdrCommandPrefixes.isEmpty()
			&& CharacterUtils.startsWithAny(playerMessage, mcdrCommandPrefixes)) {
			if (CONFIG.isLogPlayerCommand()) {
				logger.info("[mcdr][{}]<{}> {}", serverId, playerName, playerMessage);
			}
			return;
		}

		if (CONFIG.isOverwriteLocalChat()) {
			// 取消消息发送！
			event.setResult(denied());
			Utils.sendGlobalPlayerChat(player, playerMessage, currentServer, serverId);
		} else {
			// 发送全局消息！
			Utils.assembleAndComsume(player, playerMessage, currentServer, serverId,
				text -> PROXY_SERVER.getAllServers().forEach(server -> {
					if (server.getServerInfo().getName().equals(serverId)) {
						return;
					}
					server.sendMessage(text);
				}));
			logger.info("[global:{}]<{}> {}", serverId, playerName, playerMessage);
		}
	}
}
