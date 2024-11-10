package fun.qu_an.lib.config;

import fun.qu_an.lib.util.reflect.FieldAccessor;
import org.jetbrains.annotations.NotNull;

public record ConfigFieldRecord(@NotNull FieldAccessor accessor, @NotNull String path, @NotNull String comment) {
}
