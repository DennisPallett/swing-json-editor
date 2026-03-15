package nl.pallett.jsoneditor.ui.tabs;

import nl.pallett.jsoneditor.model.EditorDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class IDETab extends JPanel {
    private final JButton closeButton;

    private final JLabel tabLabel;

    public IDETab(JTabbedPane pane, EditorDocument editorDocument) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        tabLabel = new JLabel(createTitle(editorDocument), JLabel.LEFT);
        tabLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        add(tabLabel);

        addDirtyListener(editorDocument);

        closeButton = new JButton("\u2715");
        closeButton.setPreferredSize(new Dimension(16, 16));
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);

        closeButton.setText("");

        closeButton.addActionListener(e -> {
            int i = pane.indexOfTabComponent(IDETab.this);
            if (i != -1) pane.remove(i);
        });
        add(closeButton);

        // Fixed hover behavior
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setText("\u2715"); // show ×
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(mousePos, IDETab.this);
                if (!IDETab.this.contains(mousePos)) closeButton.setText(""); // hide ×
            }
        };

        // Mouse adapter for click selection & drag forwarding
        MouseAdapter tabMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int i = pane.indexOfTabComponent(IDETab.this);
                if (i != -1) pane.setSelectedIndex(i);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Forward drag to parent tabbed pane so dragging works on label
                MouseEvent newEvent = SwingUtilities.convertMouseEvent(IDETab.this, e, pane);
                for (MouseMotionListener ml : pane.getMouseMotionListeners()) {
                    ml.mouseDragged(newEvent);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setText("\u2715"); // show ×
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(mousePos, IDETab.this);
                if (!IDETab.this.contains(mousePos)) closeButton.setText(""); // hide ×
            }
        };

        addMouseListener(hoverListener);
        addMouseMotionListener(tabMouseAdapter);
        tabLabel.addMouseListener(tabMouseAdapter);
        tabLabel.addMouseMotionListener(tabMouseAdapter);

        closeButton.addMouseListener(hoverListener);
    }

    private void addDirtyListener(EditorDocument editorDocument) {
        editorDocument.addPropertyChangeListener(_ -> tabLabel.setText(createTitle(editorDocument)));
    }

    private String createTitle(EditorDocument editorDocument) {
        String title = editorDocument.getName();
        if (editorDocument.isDirty()) {
            title += "*";
        }
        return title;
    }
}

