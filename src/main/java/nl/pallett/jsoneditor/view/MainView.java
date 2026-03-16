package nl.pallett.jsoneditor.view;

import org.jspecify.annotations.Nullable;

import java.io.File;

public interface MainView {
    File[] showOpenFileDialog();
    @Nullable File showSaveFileDialog(String initialFilename);
}
