package xyz.fcidd.velocity.chat.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.fcidd.velocity.chat.VelocityChatPlugin;
import xyz.fcidd.velocity.chat.text.Translates;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ComponentUtils {
	private static final Map<Player, Component> PLAYER_COMPONENT_CACHE = new ConcurrentHashMap<>();
	private static final Logger logger = VelocityChatPlugin.getLogger();

	public static @NotNull Component getPlayerComponent(@NotNull Player player) {
		Component component = PLAYER_COMPONENT_CACHE.get(player);
		if (component != null) return component;
		String playerName = player.getUsername();
		Component playerComponent = Component.text(playerName)
			.hoverEvent(player.asHoverEvent())
			.clickEvent(ClickEvent.clickEvent(
				ClickEvent.Action.SUGGEST_COMMAND,
				"/tell " + playerName + " "
			));
		PLAYER_COMPONENT_CACHE.put(player, playerComponent);
		return playerComponent;
	}

	public static void removeFromCache(@NotNull Player player) {
		PLAYER_COMPONENT_CACHE.remove(player);
	}

	public static @NotNull Component getServerComponent(@Nullable RegisteredServer server) {
		return getServerComponent0(server, 0, null);
	}

	public static @NotNull Component getServerComponent(@Nullable RegisteredServer server, int playerCountOffset) {
		return getServerComponent0(server, playerCountOffset, null);
	}

	public static @NotNull Component getServerComponent(@Nullable RegisteredServer server, int playerCountOffset, @NotNull String currentServerId) {
		if(server == null) return Component.empty();
		return getServerComponent0(server, playerCountOffset, currentServerId);
	}

	private static @NotNull Component getServerComponent0(@NotNull RegisteredServer server, int playerCountOffset, String currentServerId) {
		String serverId = server.getServerInfo().getName();

		int onlinePlayers = server.getPlayersConnected().size() + playerCountOffset;

		TranslatableComponent playerCountComponent;
		if (onlinePlayers == 1) {
			playerCountComponent = Component.translatable("velocity.command.server-tooltip-player-online");
		} else {
			playerCountComponent = Component.translatable("velocity.command.server-tooltip-players-online");
		}
		playerCountComponent = playerCountComponent.args(Component.text(onlinePlayers));

		Component serverComponent = getNonEventedServerComponent(serverId);

		if (serverId.equals(currentServerId)) {
			return serverComponent
					.hoverEvent(HoverEvent
						.showText(Component
							.translatable("velocity.command.server-tooltip-current-server")
							.append(Component.newline())
							.append(playerCountComponent)));
		} else {
			return serverComponent
					.clickEvent(ClickEvent.runCommand("/server " + serverId))
					.hoverEvent(HoverEvent
						.showText(Component
							.translatable("velocity.command.server-tooltip-offer-connect-server")
							.append(Component.newline())
							.append(playerCountComponent)));
		}
	}

	public static @NotNull Component getNonEventedServerComponent(String serverId) {
		String serverTranslationKey = Translates.SERVER_NAME + serverId;
		if (Utils.hasTranslation(serverTranslationKey)) {
			return Component.translatable(serverTranslationKey);
		} else {
			return Component.text(serverId);
		}
	}

	public static void resetCache() {
		PLAYER_COMPONENT_CACHE.clear();
	}
}
