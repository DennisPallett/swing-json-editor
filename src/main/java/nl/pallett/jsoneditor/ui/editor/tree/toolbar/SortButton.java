package nl.pallett.jsoneditor.ui.editor.tree.toolbar;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

public class SortButton extends JButton {
    private SortState state = SortState.NONE;

    private final Icon alphaUpIcon = FontIcon.of(FontAwesomeSolid.SORT_ALPHA_UP, 24);
    private final Icon alphaDownIcon = FontIcon.of(FontAwesomeSolid.SORT_ALPHA_DOWN, 24);
    //private final Icon sortIcon = FontIcon.of(FontAwesomeSolid.SORT, 24);

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
        });
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
