package nl.pallett.jsoneditor.view;

import java.io.File;
import org.jspecify.annotations.Nullable;

public interface MainView {
    File[] showOpenFileDialog();
    @Nullable File showSaveFileDialog();
}
