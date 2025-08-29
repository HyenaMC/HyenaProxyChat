package org.teamhyena.proxy.chat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.teamhyena.proxy.chat.text.Translates;
import org.teamhyena.proxy.chat.util.ComponentUtils;
import org.teamhyena.proxy.chat.util.TabListUtils;
import org.teamhyena.proxy.chat.util.Utils;

import java.util.concurrent.TimeUnit;

import static org.teamhyena.proxy.chat.config.HyenaProxyChatConfig.CONFIG;
import static org.teamhyena.proxy.chat.util.Utils.TASK_UTIL;

public class ServerConnectedListener {
	@Subscribe()
	public void onPlayerConnected(@NotNull ServerConnectedEvent event) {
		Player player = event.getPlayer();
		RegisteredServer targetServer = event.getServer();
		// 获取目标服务器消息组件
		Component targetServerComponent = ComponentUtils.getServerComponent(targetServer, +1);
		// 玩家名
		Component playerNameComponent = ComponentUtils.getPlayerComponent(player);
		// 判断是否刚刚连接至服务器（是否没有来源服务器）
		event.getPreviousServer().ifPresentOrElse(
			server -> {
				// 发送服务器切换消息
				Utils.sendToAllPlayers(Component.translatable(
					Translates.SERVER_SWITCH.key(),
					playerNameComponent,
					ComponentUtils.getServerComponent(server),
					targetServerComponent)
				);
			}, () -> {
				// 发送服务器连接消息
				Utils.sendToAllPlayers(Component.translatable(
					Translates.CONNECTED.key(),
					playerNameComponent,
					targetServerComponent
				));
			});
		if (CONFIG.isShowGlobalTabList()) {
			TASK_UTIL.delay(2, TimeUnit.SECONDS, TabListUtils::refresh);
		}
	}
}
