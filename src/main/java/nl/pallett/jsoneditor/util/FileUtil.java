package nl.pallett.jsoneditor.util;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@NullMarked
public class FileUtil {

    public static String getExtension(@Nullable Path path) {
        if (path == null) return "";

        String fileName = path.getFileName().toString().toLowerCase();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex + 1);
    }

    public static boolean isYamlFile(@Nullable Path path) {
        String extension = getExtension(path);
        return extension.equals("yaml") || extension.equals("yml");
    }
}
