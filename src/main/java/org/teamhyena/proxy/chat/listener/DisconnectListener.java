package org.teamhyena.proxy.chat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.teamhyena.proxy.chat.text.Translates;
import org.teamhyena.proxy.chat.util.ComponentUtils;
import org.teamhyena.proxy.chat.util.TabListUtils;
import org.teamhyena.proxy.chat.util.Utils;

import static org.teamhyena.proxy.chat.config.HyenaProxyChatConfig.CONFIG;

public class DisconnectListener {
	@Subscribe
	public void onPlayerDisconnect(@NotNull DisconnectEvent event) {
		if (event.getLoginStatus() != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) {
			return;
		}
		Player player = event.getPlayer();
		// 玩家名
		Component playerNameComponent = ComponentUtils.getPlayerComponent(player);
		Component serverNameComponent = ComponentUtils.getServerComponent(player.getCurrentServer().map(ServerConnection::getServer).orElse(null), -1);
		// 将玩家退出群组的消息发送给所有人
		Utils.sendToAllPlayers(Component.translatable(Translates.DISCONNECT.key(), playerNameComponent, serverNameComponent));
		ComponentUtils.removeFromCache(player); // 移除玩家消息组件缓存
		if (CONFIG.isShowGlobalTabList()) {
			TabListUtils.remove(player); // 从 tab list 移除
		}
	}
}
