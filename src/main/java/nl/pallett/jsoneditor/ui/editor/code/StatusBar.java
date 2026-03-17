package nl.pallett.jsoneditor.ui.editor.code;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBar extends JPanel {
    private final JLabel validLabel;

    private final JLabel positionLabel;

    public StatusBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        validLabel = new JLabel(" ");
        positionLabel = new JLabel(" ");

        // "Valid JSON ✅"
        // ❌ Invalid JSON

        add(validLabel);
        add(Box.createHorizontalGlue()); // pushes next items right
        //add(new JLabel("UTF-8"));
        add(Box.createHorizontalStrut(10));
        add(positionLabel);

        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8) );
    }

    public void updateStatusBar(int line, int column) {
        positionLabel.setText("Ln " + line + ", Col " + column);
    }

}
