package fun.qu_an.lib.mc.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 格式化代码相关工具
 */
@SuppressWarnings("unused")
public class FormatUtils {
	// 颜色代码：{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r'};
	/**
	 * 用于判断玩家名是否合法的判据
	 */
	private static final Predicate<String> PLAYER_NAME_PREDICATE = Pattern.compile("[A-Za-z0-9_]+").asMatchPredicate();
	private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&(?=[0-9a-fk-or])");

	/**
	 * 删除格式化符号“§”
	 *
	 * @param raw 待替换的字符串
	 * @return 替换后的字符串
	 */
	public static @NotNull String ignoreFormattingCode(@NotNull String raw) {
		return COLOR_CODE_PATTERN.matcher(raw).replaceAll("");
	}

	/**
	 * 将格式化符号“&”替换为“§”
	 *
	 * @param raw 待替换的字符串
	 * @return 替换后的字符串
	 */
	public static @NotNull String replaceFormattingCode(@NotNull String raw) {
		return COLOR_CODE_PATTERN.matcher(raw).replaceAll("§");
	}

	public static boolean testPlayerName(String name) {
		return PLAYER_NAME_PREDICATE.test(name);
	}
}
