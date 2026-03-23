package nl.pallett.jsoneditor.util;


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ClipboardUtil {
    private ClipboardUtil() {
        /* This utility class should not be instantiated */
    }

    public static void copyToClipboard(String textToCopy) {
        StringSelection selection = new StringSelection(textToCopy);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
