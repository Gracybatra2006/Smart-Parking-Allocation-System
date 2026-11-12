
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class ParkingMapPanel extends JPanel {

    private List<ParkingSlot> slots = new ArrayList<>();

    private double plotLength = 0;
    private double plotWidth = 0;

    // Same dimensions as ParkingLayout.java
    private static final double SLOT_DEPTH = 5.0;
    private static final double AISLE_WIDTH = 6.0;
    private static final double ENTRANCE_WIDTH = 6.0;
    private static final double WALKWAY_WIDTH = 1.8;
    private static final double TURNING_WIDTH = 6.0;
    private static final double EDGE_MARGIN = 0.5;

    // Screen scaling
    private static final double SCALE = 24.0;
    private static final int MARGIN = 75;

    // Professional high-contrast colours
    private static final Color FREE_COLOR =
            new Color(34, 197, 94);

    private static final Color OCCUPIED_COLOR =
            new Color(239, 68, 68);

    private static final Color ROAD_COLOR =
            new Color(148, 163, 184);

    private static final Color TURNING_COLOR =
            new Color(100, 116, 139);

    private static final Color WALKWAY_COLOR =
            new Color(245, 158, 11);

    private static final Color PLOT_COLOR =
            new Color(226, 232, 240);

    private static final Color TEXT_COLOR =
            new Color(15, 23, 42);

    public ParkingMapPanel() {

        setBackground(new Color(248, 250, 252));

        setOpaque(true);

        updatePanelSize();
    }

    // ------------------------------------------
    // 1. UPDATE PLOT DIMENSIONS
    // ------------------------------------------

    public void setPlotDimensions(
            double length,
            double width) {

        this.plotLength = length;
        this.plotWidth = width;

        updatePanelSize();
        repaint();
    }

    // ------------------------------------------
    // 2. UPDATE SLOT OCCUPANCY
    // ------------------------------------------

    public void setSlots(
            List<ParkingSlot> updatedSlots) {

        if (updatedSlots == null) {
            this.slots = new ArrayList<>();
        } else {
            this.slots = new ArrayList<>(updatedSlots);
        }

        updatePanelSize();
        repaint();
    }

    // ------------------------------------------
    // 3. UPDATE SCROLLABLE PANEL SIZE
    // ------------------------------------------

    private void updatePanelSize() {

        int preferredWidth =
                (int) Math.ceil(
                        plotLength * SCALE
                        + 2 * MARGIN);

        int preferredHeight =
                (int) Math.ceil(
                        plotWidth * SCALE
                        + 2 * MARGIN);

        setPreferredSize(
                new Dimension(
                        Math.max(700, preferredWidth),
                        Math.max(500, preferredHeight)));

        revalidate();
    }

    // ------------------------------------------
    // 4. MAIN DRAWING METHOD
    // ------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (plotLength <= 0 || plotWidth <= 0) {

            drawEmptyMessage(g2);

            g2.dispose();
            return;
        }

        // Convert metres into screen pixels.
        g2.translate(MARGIN, MARGIN);
        g2.scale(SCALE, SCALE);

        drawPlot(g2);
        drawEntranceLane(g2);
        drawTurningArea(g2);
        drawPedestrianPath(g2);

        // Reconstruct geometry from active plot.
        List<ParkingLayout.GeneratedSlot> generated =
                ParkingLayout.generate(
                        plotLength,
                        plotWidth);

        if (!generated.isEmpty()) {

            drawDrivingAisles(g2, generated);
            drawParkingSlots(g2, generated);
        }

        drawEntranceMarker(g2);

        g2.dispose();
    }

    // ------------------------------------------
    // 5. EMPTY MAP MESSAGE
    // ------------------------------------------

    private void drawEmptyMessage(
            Graphics2D g2) {

        g2.setColor(TEXT_COLOR);

        g2.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22));

        String message =
                "No Parking Layout Generated";

        FontMetrics fm =
                g2.getFontMetrics();

        int x =
                (getWidth()
                        - fm.stringWidth(message)) / 2;

        int y =
                getHeight() / 2;

        g2.drawString(message, x, y);

        g2.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14));

        g2.setColor(
                new Color(100, 116, 139));

        String subtitle =
                "Enter plot dimensions and click "
                + "Generate Layout.";

        FontMetrics subtitleMetrics =
                g2.getFontMetrics();

        int subtitleX =
                (getWidth()
                        - subtitleMetrics.stringWidth(
                                subtitle)) / 2;

        g2.drawString(
                subtitle,
                subtitleX,
                y + 30);
    }

    // ------------------------------------------
    // 6. DRAW PLOT BACKGROUND
    // ------------------------------------------

    private void drawPlot(
            Graphics2D g2) {

        g2.setColor(PLOT_COLOR);

        g2.fill(
                new Rectangle2D.Double(
                        0,
                        0,
                        plotLength,
                        plotWidth));

        g2.setColor(
                new Color(71, 85, 105));

        g2.setStroke(
                new BasicStroke(0.12f));

        g2.draw(
                new Rectangle2D.Double(
                        0,
                        0,
                        plotLength,
                        plotWidth));

        drawText(
                g2,
                String.format(
                        "PLOT: %.1f m x %.1f m",
                        plotLength,
                        plotWidth),
                plotLength / 2.0,
                -0.9,
                0.75f,
                TEXT_COLOR);
    }

    // ------------------------------------------
    // 7. DRAW ENTRANCE / EXIT LANE
    // ------------------------------------------

    private void drawEntranceLane(
            Graphics2D g2) {

        double x = EDGE_MARGIN;
        double y = EDGE_MARGIN;

        double laneHeight =
                plotWidth - 2 * EDGE_MARGIN;

        if (laneHeight <= 0) {
            return;
        }

        g2.setColor(ROAD_COLOR);

        g2.fill(
                new Rectangle2D.Double(
                        x,
                        y,
                        ENTRANCE_WIDTH,
                        laneHeight));

        // Dashed centre line
        g2.setColor(Color.WHITE);

        g2.setStroke(
                new BasicStroke(
                        0.12f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10,
                        new float[]{0.8f, 0.5f},
                        0));

        double centreX =
                x + ENTRANCE_WIDTH / 2.0;

        g2.draw(
                new Line2D.Double(
                        centreX,
                        y + 0.7,
                        centreX,
                        y + laneHeight - 0.7));

        g2.setStroke(
                new BasicStroke(0.05f));

        drawVerticalText(
                g2,
                "ENTRANCE / EXIT LANE",
                x + 1.1,
                plotWidth / 2.0,
                0.55f,
                TEXT_COLOR);
    }

    // ------------------------------------------
    // 8. DRAW TURNING AREA
    // ------------------------------------------

    private void drawTurningArea(
            Graphics2D g2) {

        double x =
                plotLength
                - EDGE_MARGIN
                - TURNING_WIDTH;

        double y = EDGE_MARGIN;

        double height =
                plotWidth - 2 * EDGE_MARGIN;

        if (x < 0 || height <= 0) {
            return;
        }

        g2.setColor(TURNING_COLOR);

        g2.fill(
                new Rectangle2D.Double(
                        x,
                        y,
                        TURNING_WIDTH,
                        height));

        drawVerticalText(
                g2,
                "TURNING AREA",
                x + TURNING_WIDTH / 2.0,
                plotWidth / 2.0,
                0.60f,
                Color.WHITE);
    }

    // ------------------------------------------
    // 9. DRAW PEDESTRIAN WALKWAY
    // ------------------------------------------

    private void drawPedestrianPath(
            Graphics2D g2) {

        double startX =
                EDGE_MARGIN + ENTRANCE_WIDTH;

        double endX =
                plotLength
                - EDGE_MARGIN
                - TURNING_WIDTH;

        double pathLength =
                endX - startX;

        if (pathLength <= 0) {
            return;
        }

        g2.setColor(WALKWAY_COLOR);

        g2.fill(
                new Rectangle2D.Double(
                        startX,
                        EDGE_MARGIN,
                        pathLength,
                        WALKWAY_WIDTH));

        drawText(
                g2,
                "PEDESTRIAN WALKWAY",
                startX + pathLength / 2.0,
                EDGE_MARGIN
                        + WALKWAY_WIDTH / 2.0,
                0.58f,
                TEXT_COLOR);
    }

    // ------------------------------------------
    // 10. DRAW DRIVING AISLES
    // ------------------------------------------

    private void drawDrivingAisles(
            Graphics2D g2,
            List<ParkingLayout.GeneratedSlot> generated) {

        int rows = countRows(generated);

        int roads = (rows + 1) / 2;

        double startX =
                EDGE_MARGIN + ENTRANCE_WIDTH;

        double endX =
                plotLength
                - EDGE_MARGIN
                - TURNING_WIDTH;

        double roadLength =
                endX - startX;

        double parkingStartY =
                EDGE_MARGIN + WALKWAY_WIDTH;

        for (int i = 0; i < roads; i++) {

            double roadY =
                    parkingStartY
                    + i * (
                        2 * SLOT_DEPTH
                        + AISLE_WIDTH)
                    + SLOT_DEPTH;

            g2.setColor(ROAD_COLOR);

            g2.fill(
                    new Rectangle2D.Double(
                            startX,
                            roadY,
                            roadLength,
                            AISLE_WIDTH));

            // Dashed centre line
            g2.setColor(Color.WHITE);

            g2.setStroke(
                    new BasicStroke(
                            0.12f,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER,
                            10,
                            new float[]{0.8f, 0.5f},
                            0));

            double centreY =
                    roadY + AISLE_WIDTH / 2.0;

            g2.draw(
                    new Line2D.Double(
                            startX + 0.5,
                            centreY,
                            endX - 0.5,
                            centreY));

            g2.setStroke(
                    new BasicStroke(0.05f));

            drawText(
                    g2,
                    "ROAD " + (i + 1),
                    startX + roadLength / 2.0,
                    roadY + 1.3,
                    0.60f,
                    TEXT_COLOR);
        }
    }

    // ------------------------------------------
    // 11. COUNT PARKING ROWS
    // ------------------------------------------

    private int countRows(
            List<ParkingLayout.GeneratedSlot> generated) {

        Set<Character> rows =
                new HashSet<>();

        for (ParkingLayout.GeneratedSlot slot
                : generated) {

            rows.add(
                    slot.name.charAt(0));
        }

        return rows.size();
    }

    // ------------------------------------------
    // 12. DRAW PARKING SLOTS
    // ------------------------------------------

    private void drawParkingSlots(
            Graphics2D g2,
            List<ParkingLayout.GeneratedSlot> generated) {

        Map<String, ParkingSlot> databaseSlots =
                new HashMap<>();

        for (ParkingSlot slot : slots) {

            databaseSlots.put(
                    slot.displayName,
                    slot);
        }

        for (ParkingLayout.GeneratedSlot generatedSlot
                : generated) {

            ParkingSlot dbSlot =
                    databaseSlots.get(
                            generatedSlot.name);

            if (dbSlot == null) {
                continue;
            }

            boolean occupied =
                    dbSlot.isOccupied;

            double x = generatedSlot.x;
            double y = generatedSlot.y;

            double w = generatedSlot.width;
            double h = generatedSlot.depth;

            // Bright green or red slot
            g2.setColor(
                    occupied
                            ? OCCUPIED_COLOR
                            : FREE_COLOR);

            g2.fill(
                    new RoundRectangle2D.Double(
                            x + 0.07,
                            y + 0.07,
                            w - 0.14,
                            h - 0.14,
                            0.25,
                            0.25));

            // Darker outline
            g2.setColor(
                    occupied
                            ? new Color(153, 27, 27)
                            : new Color(21, 128, 61));

            g2.setStroke(
                    new BasicStroke(0.09f));

            g2.draw(
                    new RoundRectangle2D.Double(
                            x + 0.07,
                            y + 0.07,
                            w - 0.14,
                            h - 0.14,
                            0.25,
                            0.25));

            // White text gives better contrast
            // on saturated green/red backgrounds.
            drawText(
                    g2,
                    generatedSlot.name,
                    x + w / 2.0,
                    y + 1.3,
                    0.68f,
                    Color.WHITE);

            drawText(
                    g2,
                    occupied ? "OCCUPIED" : "FREE",
                    x + w / 2.0,
                    y + 2.6,
                    0.43f,
                    Color.WHITE);

            drawText(
                    g2,
                    dbSlot.walkingTime + " min",
                    x + w / 2.0,
                    y + 3.8,
                    0.48f,
                    Color.WHITE);
        }
    }

    // ------------------------------------------
    // 13. DRAW ENTRY MARKER
    // ------------------------------------------

    private void drawEntranceMarker(
            Graphics2D g2) {

        double markerX =
                EDGE_MARGIN;

        double markerY =
                plotWidth
                - EDGE_MARGIN
                - 2.2;

        g2.setColor(
                new Color(37, 99, 235));

        g2.fill(
                new RoundRectangle2D.Double(
                        markerX,
                        markerY,
                        ENTRANCE_WIDTH,
                        2.0,
                        0.25,
                        0.25));

        drawText(
                g2,
                "ENTRY",
                markerX + ENTRANCE_WIDTH / 2.0,
                markerY + 1.1,
                0.68f,
                Color.WHITE);
    }

    // ------------------------------------------
    // 14. DRAW CENTRED TEXT
    // ------------------------------------------

    private void drawText(
            Graphics2D g2,
            String text,
            double x,
            double y,
            float size,
            Color color) {

        Font oldFont = g2.getFont();
        Color oldColor = g2.getColor();

        g2.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        1).deriveFont(size));

        g2.setColor(color);

        FontMetrics fm =
                g2.getFontMetrics();

        float textWidth =
                fm.stringWidth(text);

        g2.drawString(
                text,
                (float) x - textWidth / 2.0f,
                (float) y + size / 3.0f);

        g2.setFont(oldFont);
        g2.setColor(oldColor);
    }

    // ------------------------------------------
    // 15. DRAW VERTICAL TEXT
    // ------------------------------------------

    private void drawVerticalText(
            Graphics2D g2,
            String text,
            double x,
            double y,
            float size,
            Color color) {

        AffineTransform oldTransform =
                g2.getTransform();

        g2.translate(x, y);

        g2.rotate(-Math.PI / 2.0);

        drawText(
                g2,
                text,
                0,
                0,
                size,
                color);

        g2.setTransform(oldTransform);
    }
}
