package nl.pallett.jsoneditor;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import nl.pallett.jsoneditor.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class SwingJsonEditorApp {
    public static final String APP_ID = "nl.pallett.jsoneditor";

    static void main(String[] args) {
        // macOS specific settings BEFORE Swing starts
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Test");
        System.setProperty("flatlaf.useWindowDecorations", "true");

        // Start Swing app on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();
            setupMacHandlers();
            FlatMacLightLaf.setup();
            new MainFrame();
        });
    }


    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupMacHandlers() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            desktop.setAboutHandler(e ->
                JOptionPane.showMessageDialog(null,
                    "My Swing App\nVersion 1.0",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE)
            );

            desktop.setOpenFileHandler(e -> {
                e.getFiles().forEach(f -> JOptionPane.showMessageDialog(null,
                    "Open file: " + e.getFiles(),
                    "Open file",
                    JOptionPane.INFORMATION_MESSAGE));
            });


            desktop.setQuitHandler((e, response) -> {
                int result = JOptionPane.showConfirmDialog(
                    null,
                    "Quit application?",
                    "Quit",
                    JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    response.performQuit();
                } else {
                    response.cancelQuit();
                }
            });
        }
    }
}