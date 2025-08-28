package org.teamhyena.proxy.chat.text;

import fun.qu_an.lib.velocity.api.language.LanguageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.teamhyena.proxy.chat.HyenaProxyChatPlugin;

import static org.teamhyena.proxy.chat.HyenaProxyChatPlugin.DATA_DIRECTORY;

public class Translates {
	public static final Component DASH_AND_SPACE = Component.text("- ");
	public static final Component COMMA_AND_SPACE = Component.text(", ");
	// 私聊消息

	public static final TranslatableComponent TELL_MESSAGE = Component.translatable("qu_an.chat.message.tell");
	public static final TranslatableComponent TELL_RESPONSE = Component.translatable("qu_an.chat.command.tell.response");

	// 连接消息

	public static final TranslatableComponent CONNECTED = Component.translatable("qu_an.chat.message.connected");
	public static final TranslatableComponent SERVER_SWITCH = Component.translatable("qu_an.chat.message.server_switch");
	public static final TranslatableComponent DISCONNECT = Component.translatable("qu_an.chat.message.disconnect");
	public static final TranslatableComponent SERVER_NOT_FOUND = Component.translatable("qu_an.chat.player.server_not_found");

	// 聊天消息

	public static final TranslatableComponent DEFAULT_CHAT = Component.translatable("qu_an.chat.message.chat.default");
	public static final TranslatableComponent DEFAULT_LOCAL_CHAT = Component.translatable("qu_an.chat.message.chat.default.local");

	// help

	public static final TranslatableComponent HELP = Component.translatable("qu_an.chat.command.help");
	public static final TranslatableComponent HELP_BROADCAST = Component.translatable("qu_an.chat.command.help.broadcast");
	public static final TranslatableComponent HELP_LOCAL = Component.translatable("qu_an.chat.command.help.local");

	// glist

	public static final TranslatableComponent GLIST_ENTRY = Component.translatable("qu_an.chat.command.glist.entry");
	public static final Component GLIST_TOTAL_COUNT = Component.translatable("velocity.command.glist-view-all");
	public static final TranslatableComponent GLIST_PLAYER_COUNT_SINGULAR =
		Component.translatable("velocity.command.glist-player-singular");
	public static final TranslatableComponent GLIST_PLAYER_COUNT_PLURAL =
		Component.translatable("velocity.command.glist-player-plural");

	// server

	public static final TranslatableComponent COMMAND_SERVER_CURRENT = Component.translatable("qu_an.chat.command.server.current");
	public static final TranslatableComponent COMMAND_SERVER_AVAILABLE = Component.translatable("qu_an.chat.command.server.available");

	// 群组名

	public static final Component PROXY_NAME = Component.translatable("qu_an.chat.proxy.name");

	// vc自带

	/* 需要实时补全的 */

	// 服务器聊天消息

	public static final String SERVER_CHAT = "qu_an.chat.message.chat.server.";
	public static final String SERVER_CHAT_LOCAL_SUFFIX = ".local";

	// 服务器名

	public static final String SERVER_NAME = "qu_an.chat.server.name.";
	public static final Component SERVER_NOT_FOUND_NAME = Component.text("<unknown>");

	public static final LanguageManager DEFAULT_LANG = LanguageManager.create(
		HyenaProxyChatPlugin.getInstance(),
		Key.key("qu_an", "chat_default"),
		DATA_DIRECTORY.resolve("langs/default"),
		"langs/default",
		true
	);

	public static final LanguageManager CUSTOM_LANG = LanguageManager.create(
		HyenaProxyChatPlugin.getInstance(),
		Key.key("qu_an", "chat_custom"),
		DATA_DIRECTORY.resolve("langs/custom"),
		"langs/custom"
	);
}
