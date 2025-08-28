package org.teamhyena.proxy.chat.config;

import com.electronwill.nightconfig.core.Config;
import fun.qu_an.lib.config.AnnotationConfig;
import fun.qu_an.lib.config.ConfigKey;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.teamhyena.proxy.chat.HyenaProxyChatPlugin.DATA_DIRECTORY;
import static org.teamhyena.proxy.chat.command.Commands.*;

public final class HyenaProxyChatConfig extends AnnotationConfig {
	public static final HyenaProxyChatConfig CONFIG = new HyenaProxyChatConfig(DATA_DIRECTORY.resolve("config.toml"));
	@Getter
	@ConfigKey(comment = """
		在此处填写 MCDR 命令的前缀，支持多个MCDR命令前缀
		如果没有使用 MCDR 开服请保持默认
		如果使用 MCDR 开服请根据实际情况填写，一般为“!!”
		全局聊天不会接管以列表中字符串为开头的聊天消息
		Send to current dedicated server if chat message is starts with matched string.""")
	@NotNull
	private List<String> mcdrCommandPrefix = List.of();
	@Getter
	@ConfigKey(comment = """
		是否开启默认全局聊天
		Enable default global chat.""")
	private boolean defaultGlobalChat = true;
	@Getter
	@ConfigKey(comment = """
		全局聊天时是否接管本地聊天
		Whether to take over local chats when global chats.
		""")
	private boolean overwriteLocalChat = false;
	@Getter
	@ConfigKey(comment = """
		是否打印MCDR命令日志
		Log MCDR commands.""")
	private boolean logMcdrCommands = true;
	@Getter
	@ConfigKey(comment = """
		是否在ping时发送玩家列表（在客户端服务器列表显示玩家列表）
		Send sample players when client refreshing multiplayer games.""")
	private boolean sendPlayersOnPing = false;
	@Getter
	@ConfigKey(comment = """
		Tab列表是否显示全部群组玩家
		已过时，将要移除
		无法在没有bug的要求下简单实现
		Show all proxy players on tab list.
		Deprecated, for removal.""")
	private boolean showGlobalTabList = false;
	@Getter
	@ConfigKey(comment = """
		是否启用跨服TP
		Enable cross-server teleport.""")
	private boolean enableCommandTp = false;
	@Getter
	@ConfigKey(comment = """
		是否对所有玩家启用“/glist”指令
		enable command `glist`.""")
	private boolean enableCommandGlist = true;
	@Getter
	@ConfigKey(comment = """
		是否启用跨服Tell
		Enable cross-server tell.""")
	private boolean enableCommandTell = true;
	@Getter
	@ConfigKey(comment = """
		自定义“/glist”反馈消息
		Custom command `/glist` response.""")
	private boolean customGlistCommand = true;
	@Getter
	@ConfigKey(comment = """
		自定义“/server”反馈消息
		Custom command `/server` response.""")
	private boolean customServerCommand = true;
	@Getter
	@ConfigKey(comment = """
		是否可以使用“&”作为聊天格式化代码
		Enable color code in chat.""")
	private boolean colorableChat = true;
	@ConfigKey(comment = """
		设置命令别名
		broadcast：全局聊天
		local：本地聊天
		注：“vchat local” 仅对玩家可用
		修改并重载后玩家需要重新加入游戏才会生效
		Set aliases.
		note: Command "vchat local" is player only.""")
	private Config commandAlias = Config.wrap(Map.of(
		LOCAL, LOCAL_DEFAULT_ALIAS,
		BROADCAST, BROADCAST_DEFAULT_ALIAS
	), Config.inMemory().configFormat());
	@Getter
	private String commandBroadcastAlias;
	@Getter
	private String commandLocalAlias;

	private HyenaProxyChatConfig(@NotNull Path configPath) {
		super(configPath);
	}

	/**
	 * 加载/重载配置文件
	 */
	@Override
	public synchronized void load() {
		super.load();
		boolean shouldSave = false;
		Config commandAlias = this.commandAlias;
		String broadcastAlias = commandAlias.get(BROADCAST);
		if (broadcastAlias == null) {
			commandAlias.set(BROADCAST, BROADCAST_DEFAULT_ALIAS);
			shouldSave = true;
		}
		String localAlias = commandAlias.get(LOCAL);
		if (localAlias == null) {
			commandAlias.set(LOCAL, LOCAL_DEFAULT_ALIAS);
			shouldSave = true;
		}
		if (shouldSave) {
			this.save();
		}
		commandBroadcastAlias = broadcastAlias;
		commandLocalAlias = localAlias;
	}
}
