package org.teamhyena.proxy.chat.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fun.qu_an.lib.velocity.api.util.PlayerUtil;
import fun.qu_an.lib.velocity.api.util.TaskUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teamhyena.proxy.chat.HyenaProxyChatPlugin;
import org.teamhyena.proxy.chat.text.Translates;

import java.util.function.Consumer;

public class Utils {
	public static final ProxyServer PROXY_SERVER = HyenaProxyChatPlugin.getProxyServer();
	public static final PlayerUtil PLAYER_UTIL = PlayerUtil.create(PROXY_SERVER, HyenaProxyChatPlugin.getInstance());
	public static final TaskUtil TASK_UTIL = TaskUtil.create(HyenaProxyChatPlugin.getInstance(), PROXY_SERVER);

	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

	public static boolean hasCustomTranslation(String key) {
		return Translates.CUSTOM_LANG.contains(key);
	}

	public static void sendGlobalPlayerChat(Player player, String playerMessage) {
		player.getCurrentServer().ifPresentOrElse(
			currentServer -> sendGlobalPlayerChat(player, playerMessage, currentServer.getServer(), currentServer.getServerInfo().getName()),
			() -> sendGlobalPlayerChat(player, playerMessage, null, ""));
	}

	public static void sendGlobalPlayerChat(Player player, String playerMessage, RegisteredServer currentServer, String serverId) {
		assembleAndConsume(player, playerMessage, currentServer, serverId,
			message -> sendToAllPlayers(message, "[global:{}]<{}> {}", serverId, player.getUsername(), playerMessage));
	}

	public static void sendToAllPlayers(@NotNull Component text) {
		sendToAllPlayers(text, null);
	}

	public static void sendToAllPlayers(@NotNull Component text, String log, Object... args) {
//		PROXY_SERVER.sendMessage(text);
		PROXY_SERVER.getAllPlayers().forEach(player -> player.sendMessage(text));
		if (log != null) HyenaProxyChatPlugin.getLogger().info(log, args);
	}

	// 仅发送全局消息的装配
	public static void assembleAndConsume(Player player,
										  String playerMessage,
										  @Nullable RegisteredServer currentServer,
										  String serverId,
										  Consumer<Component> globalConsumer) {
		Component playerNameComponent = ComponentUtils.getPlayerComponent(player);
		Component chatMessage = LEGACY.deserialize(playerMessage); // 允许使用格式化代码
		String serverChatFormatTranslationKey = Translates.SERVER_CHAT + serverId;
		if (hasCustomTranslation(serverChatFormatTranslationKey)) {
			globalConsumer.accept(Component.translatable(
				serverChatFormatTranslationKey, // 服务器专属格式
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		} else {
			globalConsumer.accept(Component.translatable(
				Translates.DEFAULT_CHAT.key(),
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		}
	}

	// 同时发送全局与本地消息的装配
	public static void assembleAndConsume(Player player,
										  String playerMessage,
										  @Nullable RegisteredServer currentServer,
										  String serverId,
										  Consumer<Component> globalConsumer,
										  Consumer<Component> localConsumer) {
		Component playerNameComponent = ComponentUtils.getPlayerComponent(player);
		Component chatMessage = LEGACY.deserialize(playerMessage);
		String serverChatFormatTranslationKey = Translates.SERVER_CHAT + serverId;

		// 全局
		if (hasCustomTranslation(serverChatFormatTranslationKey)) {
			globalConsumer.accept(Component.translatable(
				serverChatFormatTranslationKey,
				Translates.PROXY_NAME,
				ComponentUtils.getServerComponent(currentServer),
				playerNameComponent,
				chatMessage
			));
		} else {
			globalConsumer.accept(Component.translatable(
				Translates.DEFAULT_CHAT.key(),
				Translates.PROXY_NAME,
				ComponentUtils.getServerComponent(currentServer),
				playerNameComponent,
				chatMessage
			));
		}

		// 本地
		String localChatKey = serverChatFormatTranslationKey + Translates.SERVER_CHAT_LOCAL_SUFFIX;
		if (hasCustomTranslation(localChatKey)) {
			localConsumer.accept(Component.translatable(
				localChatKey,
				Translates.PROXY_NAME,
				ComponentUtils.getServerComponent(currentServer),
				playerNameComponent,
				chatMessage
			));
		} else {
			localConsumer.accept(Component.translatable(
				Translates.DEFAULT_LOCAL_CHAT.key(),
				Translates.PROXY_NAME,
				ComponentUtils.getServerComponent(currentServer),
				playerNameComponent,
				chatMessage
			));
		}
	}
}
