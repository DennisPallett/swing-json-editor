package nl.pallett.jsoneditor;

import java.io.InputStream;
import java.util.Properties;

public class VersionInfo {

    private static String version;
    private static String build;
    private static String commit;
    private static String branch;

    static {
        try (InputStream is = VersionInfo.class
            .getResourceAsStream("/version.properties")) {

            Properties props = new Properties();
            props.load(is);

            version = resolve(props.getProperty("app.version"), "dev-SNAPSHOT");
            build = resolve(props.getProperty("app.build"), "local");
            commit = resolve(props.getProperty("app.commit"), "none");
            branch = resolve(props.getProperty("app.branch"), "none");

        } catch (Exception e) {
            version = build = commit = branch = "unknown";
        }
    }

    public static String getFullVersion() {
        if (version.contains("SNAPSHOT")) {
            return version +
                " (build " + build +
                ", commit " + commit +
                ", branch " + branch + ")";
        } else {
            return version;
        }
    }

    private static String resolve(String value, String fallback) {
        if (value == null || value.contains("${")) {
            return fallback;
        }
        return value;
    }
}