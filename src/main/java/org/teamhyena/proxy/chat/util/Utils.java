package org.teamhyena.proxy.chat.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fun.qu_an.lib.velocity.api.util.PlayerUtil;
import fun.qu_an.lib.velocity.api.util.TaskUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teamhyena.proxy.chat.HyenaProxyChatPlugin;
import org.teamhyena.proxy.chat.text.Translates;

import java.util.function.Consumer;

public class Utils {
	public static final ProxyServer PROXY_SERVER = HyenaProxyChatPlugin.getProxyServer();
	public static final PlayerUtil PLAYER_UTIL = PlayerUtil.create(PROXY_SERVER, HyenaProxyChatPlugin.getInstance());
	public static final TaskUtil TASK_UTIL = TaskUtil.create(HyenaProxyChatPlugin.getInstance(), PROXY_SERVER);

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

	public static void assembleAndConsume(Player player,
										  String playerMessage,
										  @Nullable RegisteredServer currentServer,
										  String serverId,
										  Consumer<Component> globalConsumer) {
		// 玩家名
		Component playerNameComponent = ComponentUtils.getPlayerComponent(player);
		// 构建玩家消息，Velocity API 居然把玩家队伍颜色阻断掉了，导致不能显示玩家队伍颜色
		TextComponent chatMessage = Component.text(playerMessage);
		// 发送消息
		String serverChatFormatTranslationKey = Translates.SERVER_CHAT + serverId;
		if (hasCustomTranslation(serverChatFormatTranslationKey)) {
			globalConsumer.accept(Component.translatable(
				serverChatFormatTranslationKey, // 追加子服务器id
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		} else {
			globalConsumer.accept(Translates.DEFAULT_CHAT.args(
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		}
	}

	public static void assembleAndConsume(Player player,
										  String playerMessage,
										  RegisteredServer currentServer,
										  String serverId,
										  Consumer<Component> globalConsumer,
										  Consumer<Component> localConsumer) {
		// 玩家名
		Component playerNameComponent = ComponentUtils.getPlayerComponent(player);
		// 构建玩家消息，Velocity API 居然把玩家队伍颜色阻断掉了，导致不能显示玩家队伍颜色
		TextComponent chatMessage = Component.text(playerMessage);
		// 发送消息
		String serverChatFormatTranslationKey = Translates.SERVER_CHAT + serverId;
		if (hasCustomTranslation(serverChatFormatTranslationKey)) {
			globalConsumer.accept(Component.translatable(
				serverChatFormatTranslationKey, // 追加子服务器id
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		} else {
			globalConsumer.accept(Translates.DEFAULT_CHAT.args(
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		}
		String localChatKey = serverChatFormatTranslationKey + Translates.SERVER_CHAT_LOCAL_SUFFIX;
		if (hasCustomTranslation(localChatKey)) {
			localConsumer.accept(Component.translatable(
				localChatKey, // 追加子服务器id
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		} else {
			localConsumer.accept(Translates.DEFAULT_LOCAL_CHAT.args(
				Translates.PROXY_NAME, // 群组名称
				ComponentUtils.getServerComponent(currentServer), // 服务器名称
				playerNameComponent, // 玩家名
				chatMessage // 聊天内容
			));
		}
	}
}
