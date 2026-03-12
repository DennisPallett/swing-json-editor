package nl.pallett.jsoneditor;

import javafx.application.Platform;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Captures raw macOS 'odoc' Apple Events for JavaFX apps.
 * Queues files until JavaFX UI is ready.
 */
public class MacOSAppleEventListener {

    private static final List<File> pendingFiles = new ArrayList<>();
    private static FileOpenHandler fileHandler;
    private static boolean javafxReady = false;

    public interface FileOpenHandler {
        void openFile(File file);
    }

    /**
     * Call this as early as possible, before Application.launch()
     */
    public static void init() {
        try {
            // Load the Apple EAWT Application class dynamically
            Class<?> appClass = Class.forName("com.apple.eawt.Application");
            Method getAppMethod = appClass.getMethod("getApplication");
            Object application = getAppMethod.invoke(null);

            // Set the open file handler
            Method setHandler = appClass.getMethod("setOpenFileHandler", Class.forName("com.apple.eawt.OpenFilesHandler"));
            Object handlerProxy = java.lang.reflect.Proxy.newProxyInstance(
                MacOSAppleEventListener.class.getClassLoader(),
                new Class[]{Class.forName("com.apple.eawt.OpenFilesHandler")},
                (proxy, method, args) -> {
                    Object event = args[0];
                    Method getFiles = event.getClass().getMethod("getFiles");
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) getFiles.invoke(event);

                    for (File file : files) {
                        dispatchFile(file);
                    }
                    return null;
                }
            );
            setHandler.invoke(application, handlerProxy);

            System.out.println("Apple Event handler installed");

        } catch (ClassNotFoundException e) {
            System.out.println("Not running on macOS or Apple EAWT missing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dispatchFile(File file) {
        if (!javafxReady || fileHandler == null) {
            pendingFiles.add(file);
        } else {
            Platform.runLater(() -> fileHandler.openFile(file));
        }
    }

    public static void setFileOpenHandler(FileOpenHandler handler) {
        fileHandler = handler;
        if (javafxReady) {
            flushPending();
        }
    }

    public static void markJavaFXReady() {
        javafxReady = true;
        flushPending();
    }

    private static void flushPending() {
        if (fileHandler == null) return;
        for (File f : pendingFiles) {
            Platform.runLater(() -> fileHandler.openFile(f));
        }
        pendingFiles.clear();
    }
}