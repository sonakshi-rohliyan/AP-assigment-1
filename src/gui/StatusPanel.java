package gui;

import dispatch.DispatchManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class StatusPanel extends JComponent {
    private static final int DEFAULT_WIDTH = 260;
    private static final int DEFAULT_HEIGHT = 300;
    private static final int BAR_WIDTH = 50;

    private final DispatchManager manager;

    public StatusPanel(DispatchManager manager) {
        this.manager = manager;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int available = manager.getAvailableUnits().size();
        int busy = manager.getUnitCount() - available;
        int openIncidents = manager.getIncidentsByStatus("OPEN").size();

        int[] values = {available, busy, openIncidents};
        String[] labels = {"Available", "Busy", "Open inc."};
        Color[] colors = {new Color(46, 160, 67), Color.ORANGE, Color.RED};

        int max = Math.max(1, Math.max(available, Math.max(busy, openIncidents)));

        int width = getWidth();
        int height = getHeight();
        double baseY = height - 40;
        double maxBarHeight = height - 100;
        double gap = (width - 3 * BAR_WIDTH) / 4.0;

        g2.setPaint(Color.WHITE);
        g2.fill(new Rectangle2D.Double(0, 0, width, height));

        g2.setPaint(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("System Summary", 15, 35);

        for (int i = 0; i < 3; i++) {
            double barHeight = maxBarHeight * values[i] / max;
            double x = gap + i * (BAR_WIDTH + gap);
            var bar = new Rectangle2D.Double(x, baseY - barHeight, BAR_WIDTH, barHeight);

            g2.setPaint(colors[i]);
            g2.fill(bar);
            g2.setPaint(Color.BLACK);
            g2.draw(bar);

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(String.valueOf(values[i]), (int) x + BAR_WIDTH / 2 - 5, (int) (baseY - barHeight) - 5);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(labels[i], (int) x, (int) baseY + 18);
        }

        // baseline
        g2.setPaint(Color.BLACK);
        g2.draw(new java.awt.geom.Line2D.Double(10, baseY, width - 10, baseY));
    }
}

