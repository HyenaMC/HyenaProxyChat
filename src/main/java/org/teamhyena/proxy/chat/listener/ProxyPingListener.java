package org.teamhyena.proxy.chat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import org.jetbrains.annotations.NotNull;

import static org.teamhyena.proxy.chat.config.HyenaProxyChatConfig.CONFIG;
import static org.teamhyena.proxy.chat.util.Utils.PLAYER_UTIL;

public class ProxyPingListener {
	@Subscribe
	public void onProxyPing(@NotNull ProxyPingEvent event) {
		if (CONFIG.isSendPlayersOnPing()) {
			event.setPing(event
				.getPing()
				.asBuilder()
				.clearSamplePlayers()
				.samplePlayers(PLAYER_UTIL.getSamplePlayers())
				.build());
		}
	}
}
