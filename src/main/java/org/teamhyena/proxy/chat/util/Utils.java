package org.teamhyena.proxy.chat.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fun.qu_an.lib.velocity.api.util.PlayerUtil;
import fun.qu_an.lib.velocity.api.util.TaskUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teamhyena.proxy.chat.HyenaProxyChatPlugin;
import org.teamhyena.proxy.chat.text.Translates;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static final ProxyServer PROXY_SERVER = HyenaProxyChatPlugin.getProxyServer();
	public static final PlayerUtil PLAYER_UTIL = PlayerUtil.create(PROXY_SERVER, HyenaProxyChatPlugin.getInstance());
	public static final TaskUtil TASK_UTIL = TaskUtil.create(HyenaProxyChatPlugin.getInstance(), PROXY_SERVER);

	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

	// 用于 MessageFormat 参数占位的罕见分隔符，避免与正常文本冲突
	private static final String TOKEN_PREFIX = "\u001E";
	private static final String TOKEN_SUFFIX = "\u001F";

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
		// 逐玩家渲染并发送（避免 Adventure 对插件自定义翻译键中的 § 触发告警）
		PROXY_SERVER.getAllPlayers().forEach(player -> player.sendMessage(renderForPlayer(text, player)));
		if (log != null) HyenaProxyChatPlugin.getLogger().info(log, args);
	}

	public static void sendToPlayer(@NotNull Player player, @NotNull Component text) {
		player.sendMessage(renderForPlayer(text, player));
	}

	public static @NotNull Component renderForDefaultLocale(@NotNull Component text) {
		return renderPluginTranslatables(text, java.util.Locale.getDefault());
	}

	public static void sendToServerPlayers(@NotNull RegisteredServer server, @NotNull Component text) {
		server.getPlayersConnected().forEach(player -> player.sendMessage(renderForPlayer(text, player)));
	}

	public static void sendToAllServersExcept(@Nullable RegisteredServer exclude, @NotNull Component text) {
		PROXY_SERVER.getAllServers().forEach(server -> {
			if (exclude != null && server.equals(exclude)) return;
			server.getPlayersConnected().forEach(player -> player.sendMessage(renderForPlayer(text, player)));
		});
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

	// === 私有：逐玩家渲染 ===
	private static @NotNull Component renderForPlayer(@NotNull Component original, @NotNull Player player) {
		Locale locale = resolvePlayerLocale(player);
		return renderPluginTranslatables(original, locale);
	}

	private static Locale resolvePlayerLocale(@NotNull Player player) {
		// 通过反射兼容不同 Velocity 版本：优先使用 player.getPlayerSettings().getLocale()，其次 getEffectiveLocale()
		try {
			Method mGetSettings = player.getClass().getMethod("getPlayerSettings");
			Object settings = mGetSettings.invoke(player);
			if (settings != null) {
				try {
					Method mGetLocale = settings.getClass().getMethod("getLocale");
					Object loc = mGetLocale.invoke(settings);
					if (loc instanceof Locale) return (Locale) loc;
				} catch (NoSuchMethodError | NoSuchMethodException ignored) { }
			}
		} catch (Throwable ignored) { }
		try {
			Method mEffective = player.getClass().getMethod("getEffectiveLocale");
			Object loc = mEffective.invoke(player);
			if (loc instanceof Locale) return (Locale) loc;
		} catch (Throwable ignored) { }
		return Locale.getDefault();
	}

	private static @NotNull Component renderPluginTranslatables(@NotNull Component component, @NotNull Locale locale) {
		if (component instanceof TranslatableComponent tc) {
			String key = tc.key();
			// 仅处理本插件命名空间，其他交给 Adventure 默认本地化
			if (key.startsWith("qu_an.")) {
				return renderPluginTranslatable(tc, locale);
			} else {
				// 处理参数中可能包含的插件翻译
				if (!tc.args().isEmpty()) {
					List<Component> newArgs = new ArrayList<>(tc.args().size());
					for (Component arg : tc.args()) newArgs.add(renderPluginTranslatables(arg, locale));
					return tc.args(newArgs);
				}
				return tc;
			}
		}
		return component; // 其他情况原样返回
	}

	private static @NotNull Component renderPluginTranslatable(@NotNull TranslatableComponent tc, @NotNull Locale locale) {
		String key = tc.key();
		List<Component> args = tc.args();
		List<Component> renderedArgs = new ArrayList<>(args.size());
		for (Component a : args) renderedArgs.add(renderPluginTranslatables(a, locale));

		MessageFormat mf = null;
		try {
			if (Translates.CUSTOM_LANG.contains(key)) mf = Translates.CUSTOM_LANG.translate(key, locale);
			if (mf == null) mf = Translates.DEFAULT_LANG.translate(key, locale);
		} catch (Throwable ignored) { }

		if (mf == null) {
			// 找不到翻译，退回原始 translatable（其内参数已递归处理）
			return tc.args(renderedArgs);
		}

		// 用罕见 token 占位参数，以便格式化后再替换回组件，期间文本部分走 legacy 解析
		Object[] tokenArgs = new Object[renderedArgs.size()];
		for (int i = 0; i < renderedArgs.size(); i++) tokenArgs[i] = tokenOf(i);
		String formatted = mf.format(tokenArgs);

		// 构造正则捕获所有 token
		Pattern tokenPattern = Pattern.compile(Pattern.quote(TOKEN_PREFIX) + "(\\d+)" + Pattern.quote(TOKEN_SUFFIX));
		Matcher matcher = tokenPattern.matcher(formatted);
		int last = 0;
		Component result = Component.empty();
		while (matcher.find()) {
			String before = formatted.substring(last, matcher.start());
			if (!before.isEmpty()) {
				result = result.append(LEGACY.deserialize(before));
			}
			int idx = Integer.parseInt(matcher.group(1));
			if (idx >= 0 && idx < renderedArgs.size()) {
				result = result.append(renderedArgs.get(idx));
			}
			last = matcher.end();
		}
		if (last < formatted.length()) {
			result = result.append(LEGACY.deserialize(formatted.substring(last)));
		}
		return result;
	}

	private static String tokenOf(int i) {
		return TOKEN_PREFIX + i + TOKEN_SUFFIX;
	}
}
