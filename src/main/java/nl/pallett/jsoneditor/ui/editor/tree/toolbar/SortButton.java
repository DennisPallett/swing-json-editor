package nl.pallett.jsoneditor.ui.editor.tree.toolbar;

import nl.pallett.jsoneditor.ui.editor.tree.SortState;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SortButton extends JButton {
    private SortState state = SortState.NONE;

    private final Icon alphaUpIcon = FontIcon.of(FontAwesomeSolid.SORT_ALPHA_UP, 16);
    private final Icon alphaDownIcon = FontIcon.of(FontAwesomeSolid.SORT_ALPHA_DOWN, 16);

    private final List<SortStateChangedListener> sortStateChangedListenerList = new ArrayList<>();

    public SortButton() {
        setMargin(new Insets(5, 5, 5, 5));

        updateUIState();
        addActionListener(e -> {
            state = switch (state) {
                case NONE -> SortState.ASCENDING;
                case ASCENDING -> SortState.DESCENDING;
                case DESCENDING -> SortState.NONE;
            };
            updateUIState();
            sortStateChangedListenerList.forEach(l -> l.onSortStateChanged(state));
        });
    }

    public void addOnSortChangeListener(SortStateChangedListener listener) {
        sortStateChangedListenerList.add(listener);
    }

    private void updateUIState() {
        switch (state) {
            case NONE -> {
                setIcon(alphaDownIcon);
                setSelected(false);
            }
            case ASCENDING -> {
                setIcon(alphaDownIcon);
                setSelected(true);
            }
            case DESCENDING -> {
                setIcon(alphaUpIcon);
                setSelected(true);
            }
        }
    }

    public SortState getState() {
        return state;
    }
}
