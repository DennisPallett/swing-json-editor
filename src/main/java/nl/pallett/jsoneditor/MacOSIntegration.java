package nl.pallett.jsoneditor;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.desktop.OpenFilesEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MacOSIntegration {

    private static final List<File> pendingFiles = new ArrayList<>();
    private static FileOpenHandler fileHandler;
    private static Runnable reopenHandler;
    private static boolean javafxReady = false;

    public static void init(String[] args) {

        // Collect files passed at startup
        for (String arg : args) {
            pendingFiles.add(new File(arg));
        }

        // Force AWT initialization
        Toolkit.getDefaultToolkit();

        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();

        System.out.println("INSTALLED HANDLER");
        desktop.setOpenFileHandler((OpenFilesEvent e) -> {
            System.out.println("RECEIVED EVENT");
            for (File file : e.getFiles()) {
                dispatchFile(file);
            }
        });
    }

    public static void setFileOpenHandler(FileOpenHandler handler) {
        fileHandler = handler;

        // process pending files
        if (javafxReady) {
            flushPending();
        }
    }

    public static void setReopenHandler(Runnable handler) {
        reopenHandler = handler;
    }

    public static void markJavaFXReady() {
        javafxReady = true;
        flushPending();
    }

    private static void dispatchFile(File file) {
        if (!javafxReady || fileHandler == null) {
            pendingFiles.add(file);
        } else {
            //Platform.runLater(() -> fileHandler.openFile(file));
        }
    }

    private static void flushPending() {
        if (fileHandler == null) return;

        for (File file : pendingFiles) {
            //Platform.runLater(() -> fileHandler.openFile(file));
        }

        pendingFiles.clear();
    }

    public interface FileOpenHandler {
        void openFile(File file);
    }
}