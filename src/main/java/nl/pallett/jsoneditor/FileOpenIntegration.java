package nl.pallett.jsoneditor;

import java.awt.*;
import java.awt.desktop.OpenFilesEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileOpenIntegration {

    private static final List<File> pendingFiles = new ArrayList<>();
    private static FileOpenHandler fileHandler;
    private static boolean appReady = false;

    public static void init(String[] args) {

        // Collect files passed at startup
        for (String arg : args) {
            pendingFiles.add(new File(arg));
        }

        Desktop desktop = Desktop.getDesktop();

        desktop.setOpenFileHandler((OpenFilesEvent e) -> {
            for (File file : e.getFiles()) {
                dispatchFile(file);
            }
        });
    }

    public static void setFileOpenHandler(FileOpenHandler handler) {
        fileHandler = handler;

        // process pending files
        if (appReady) {
            flushPending();
        }
    }

    public static void markReady() {
        appReady = true;
        flushPending();
    }

    private static void dispatchFile(File file) {
        if (!appReady || fileHandler == null) {
            pendingFiles.add(file);
        } else {
            fileHandler.openFile(file);
        }
    }

    private static void flushPending() {
        if (fileHandler == null) return;

        for (File file : pendingFiles) {
            fileHandler.openFile(file);
        }

        pendingFiles.clear();
    }

    public interface FileOpenHandler {
        void openFile(File file);
    }
}