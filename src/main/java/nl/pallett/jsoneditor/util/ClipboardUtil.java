package nl.pallett.jsoneditor.util;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ClipboardUtil {
    public static void copyToClipboard(String textToCopy) {
        // Get system clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();

        // Create content
        ClipboardContent content = new ClipboardContent();
        content.putString(textToCopy);

        // Set content to clipboard
        clipboard.setContent(content);
    }
}
