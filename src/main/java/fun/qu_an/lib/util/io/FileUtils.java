package fun.qu_an.lib.util.io;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("unused")
public class FileUtils {
	/**
	 * 遍历输入的目录下第一层的所有文件
	 */
	public static void forEachChild(@NotNull Path folderPath, @NotNull Consumer<File> fileConsumer) {
		File[] files = folderPath.toFile().listFiles();
		if (files == null) return;
		for (File file : files) {
			fileConsumer.accept(file);
		}
	}

	/**
	 * 复制文件，不带缓存，一次性读取
	 */
	public static boolean copyWithoutBuffer(@NotNull File from, @NotNull File to) throws IOException {
		if (!from.exists()) return false;
		if (!create(to)) return false;
		try (InputStream is = new FileInputStream(from);
			 OutputStream os = new FileOutputStream(to)
		) {
			os.write(is.readAllBytes());
		}
		return true;
	}

	/**
	 * 解压文件，不带缓存，一次性读取
	 */
	public static void unpack(@NotNull ZipFile zipFile, @NotNull ZipEntry zipEntry, @NotNull File to) throws IOException {
		if (!create(to)) return;
		try (InputStream is = zipFile.getInputStream(zipEntry);
			 OutputStream os = new FileOutputStream(to)) {
			os.write(is.readAllBytes());
		}
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored", "UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
	public static boolean create(@NotNull File file) {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	public static FileSystem getZipFileSystem(@NotNull Path zipPath, boolean create) throws IOException {
		return FileSystems.newFileSystem(
			URI.create("jar:file:" + zipPath.toUri().getPath()),
			create ? Map.of("create", "true") : Map.of()
		);
	}
}
