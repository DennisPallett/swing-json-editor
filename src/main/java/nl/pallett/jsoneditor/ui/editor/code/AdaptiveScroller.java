package nl.pallett.jsoneditor.ui.editor.code;

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class AdaptiveScroller {

    private Timer timer;

    public void scrollToOffsetAdaptive(RSyntaxTextArea textArea, int offset, Runnable runWhenFinished) {
        try {
            Rectangle target = textArea.modelToView2D(offset).getBounds();
            JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, textArea);
            if (viewport == null) {
                runWhenFinished.run();
                return;
            }

            Rectangle visible = viewport.getViewRect();
            int margin = 40;
            visible.grow(0, -margin);

            // Already visible → instant
            if (visible.contains(target)) {
                textArea.setCaretPosition(offset);
                runWhenFinished.run();
                return;
            }

            Point start = viewport.getViewPosition();
            int targetY = target.y - viewport.getHeight() / 2;
            targetY = Math.max(0, targetY);
            Point end = new Point(start.x, targetY);

            // Distance-adaptive duration (pixels)
            int distance = Math.abs(end.y - start.y);
            int baseDuration = 300;  // minimum duration in ms
            int maxDuration = 700;   // max duration for very long jumps
            int duration = Math.min(maxDuration, baseDuration + distance / 2);

            // Stop previous animation
            if (timer != null && timer.isRunning()) timer.stop();

            long startTime = System.currentTimeMillis();

            timer = new Timer(15, e -> {
                float t = (System.currentTimeMillis() - startTime) / (float) duration;
                t = Math.min(1f, t);

                // easeOutCubic
                float eased = 1 - (float) Math.pow(1 - t, 3);

                int y = (int) (start.y + (end.y - start.y) * eased);
                viewport.setViewPosition(new Point(start.x, y));

                if (t >= 1f) {
                    ((Timer) e.getSource()).stop();
                    viewport.setViewPosition(end);
                    textArea.setCaretPosition(offset); // caret at end
                    runWhenFinished.run();
                }
            });

            timer.start();

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}