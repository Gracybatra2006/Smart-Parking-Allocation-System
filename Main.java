
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main extends JFrame {

    private ArrayList<ParkingSlot> slots =
            new ArrayList<>();

    private JTextField vehicleField;
    private JSpinner timeSpinner;
    private JTextField lengthField;
    private JTextField widthField;

    private JLabel capacityLabel;
    private JLabel availableLabel;
    private JLabel occupiedLabel;

    private ParkingMapPanel slotsPanel;

    // Professional dashboard palette.
    private static final Color NAVY =
            new Color(15, 23, 42);

    private static final Color SIDEBAR =
            new Color(30, 41, 59);

    private static final Color BACKGROUND =
            new Color(235, 241, 248);

    private static final Color CARD =
            Color.WHITE;

    private static final Color TEXT =
            new Color(30, 41, 59);

    private static final Color MUTED =
            new Color(90, 105, 125);

    private static final Color BLUE =
            new Color(37, 99, 235);

    private static final Color GREEN =
            new Color(5, 150, 105);

    private static final Color RED =
            new Color(220, 38, 38);

    private static final Color BORDER =
            new Color(203, 213, 225);

    public Main() {

        setTitle("Smart Parking Allocation System");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);

        setSize(1450, 900);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        createHeader();
        createDashboard();

        loadSlotsFromDatabase();
    }

    // ------------------------------------------
    // HEADER
    // ------------------------------------------

    private void createHeader() {

        JPanel header = new JPanel(
                new BorderLayout());

        header.setBackground(NAVY);

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30));

        JPanel textPanel = new JPanel(
                new GridLayout(2, 1, 0, 5));

        textPanel.setOpaque(false);

        JLabel title = new JLabel(
                "SMART PARKING");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 27));

        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Intelligent Parking Allocation Dashboard");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));

        subtitle.setForeground(
                new Color(180, 198, 220));

        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(
                textPanel,
                BorderLayout.WEST);

        JLabel status = new JLabel(
                "PARKING MANAGEMENT SYSTEM");

        status.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        status.setForeground(
                new Color(125, 211, 252));

        header.add(status, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // ------------------------------------------
    // DASHBOARD
    // ------------------------------------------

    private void createDashboard() {

        JPanel dashboard = new JPanel(
                new BorderLayout(18, 18));

        dashboard.setBackground(BACKGROUND);

        dashboard.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 22, 20, 22));

        JPanel sidebar = createSidebar();

        sidebar.setPreferredSize(
                new Dimension(335, 600));

        dashboard.add(
                sidebar,
                BorderLayout.WEST);

        JPanel rightPanel = new JPanel(
                new BorderLayout(0, 16));

        rightPanel.setOpaque(false);

        rightPanel.add(
                createStatisticsPanel(),
                BorderLayout.NORTH);

        rightPanel.add(
                createMapCard(),
                BorderLayout.CENTER);

        dashboard.add(
                rightPanel,
                BorderLayout.CENTER);

        add(dashboard, BorderLayout.CENTER);
    }

    // ------------------------------------------
    // SIDEBAR
    // ------------------------------------------

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setLayout(
                new BoxLayout(
                        sidebar,
                        BoxLayout.Y_AXIS));

        sidebar.setBackground(SIDEBAR);

        sidebar.setBorder(
                BorderFactory.createEmptyBorder(
                        25, 20, 25, 20));

        JLabel heading = new JLabel(
                "PARKING CONTROLS");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 19));

        heading.setForeground(Color.WHITE);

        heading.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        sidebar.add(heading);

        sidebar.add(
                Box.createVerticalStrut(25));

        sidebar.add(
                createSidebarHeading(
                        "VEHICLE MANAGEMENT"));

        sidebar.add(
                Box.createVerticalStrut(14));

        sidebar.add(
                createInputLabel("Vehicle Number"));

        vehicleField = createTextField("");

        sidebar.add(vehicleField);

        sidebar.add(
                Box.createVerticalStrut(15));

        sidebar.add(
                createInputLabel(
                        "Available Time (Minutes)"));

        timeSpinner = new JSpinner(
                new SpinnerNumberModel(
                        3, 1, 10, 1));

        timeSpinner.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE, 42));

        timeSpinner.setFont(
                new Font("Segoe UI", Font.BOLD, 15));

        JSpinner.DefaultEditor editor =
                (JSpinner.DefaultEditor)
                        timeSpinner.getEditor();

        editor.getTextField()
                .setHorizontalAlignment(
                        JTextField.CENTER);

        sidebar.add(timeSpinner);

        sidebar.add(
                Box.createVerticalStrut(20));

        JButton allocateButton =
                createButton(
                        "ALLOCATE PARKING",
                        BLUE);

        allocateButton.addActionListener(
                e -> allocateParking());

        sidebar.add(allocateButton);

        sidebar.add(
                Box.createVerticalStrut(10));

        JButton releaseButton =
                createButton(
                        "RELEASE PARKING",
                        RED);

        releaseButton.addActionListener(
                e -> releaseParking());

        sidebar.add(releaseButton);

        sidebar.add(
                Box.createVerticalStrut(30));

        JSeparator separator = new JSeparator();

        separator.setForeground(
                new Color(70, 85, 105));

        separator.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE, 2));

        sidebar.add(separator);

        sidebar.add(
                Box.createVerticalStrut(25));

        sidebar.add(
                createSidebarHeading(
                        "PARKING AREA SETUP"));

        sidebar.add(
                Box.createVerticalStrut(15));

        sidebar.add(
                createInputLabel(
                        "Plot Length (Metres)"));

        lengthField = createTextField("40");

        sidebar.add(lengthField);

        sidebar.add(
                Box.createVerticalStrut(15));

        sidebar.add(
                createInputLabel(
                        "Plot Width (Metres)"));

        widthField = createTextField("32");

        sidebar.add(widthField);

        sidebar.add(
                Box.createVerticalStrut(20));

        JButton generateButton =
                createButton(
                        "GENERATE LAYOUT",
                        GREEN);

        generateButton.addActionListener(
                e -> generateParkingLayout());

        sidebar.add(generateButton);

        sidebar.add(
                Box.createVerticalGlue());

        JLabel footer = new JLabel(
                "Smart Parking | Dashboard");

        footer.setForeground(
                new Color(150, 165, 185));

        footer.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        footer.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        sidebar.add(footer);

        JScrollPane scrollPane =
                new JScrollPane(sidebar);

        scrollPane.setBorder(null);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        JPanel wrapper = new JPanel(
                new BorderLayout());

        wrapper.add(
                scrollPane,
                BorderLayout.CENTER);

        return wrapper;
    }

    // ------------------------------------------
    // STATISTICS
    // ------------------------------------------

    private JPanel createStatisticsPanel() {

        JPanel panel = new JPanel(
                new GridLayout(1, 3, 15, 0));

        panel.setOpaque(false);

        capacityLabel =
                createStatisticValue("0");

        availableLabel =
                createStatisticValue("0");

        occupiedLabel =
                createStatisticValue("0");

        panel.add(
                createStatCard(
                        "TOTAL PARKING SLOTS",
                        capacityLabel,
                        BLUE));

        panel.add(
                createStatCard(
                        "AVAILABLE SLOTS",
                        availableLabel,
                        GREEN));

        panel.add(
                createStatCard(
                        "OCCUPIED SLOTS",
                        occupiedLabel,
                        RED));

        return panel;
    }

    private JPanel createStatCard(
            String title,
            JLabel value,
            Color accent) {

        JPanel card = new JPanel(
                new BorderLayout(0, 12));

        card.setBackground(CARD);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 5, 0, 0, accent),
                        BorderFactory.createEmptyBorder(
                                18, 20, 18, 20)));

        JLabel heading = new JLabel(title);

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        heading.setForeground(MUTED);

        value.setForeground(accent);

        card.add(
                heading,
                BorderLayout.NORTH);

        card.add(
                value,
                BorderLayout.CENTER);

        return card;
    }

    private JLabel createStatisticValue(String value) {

        JLabel label = new JLabel(value);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 32));

        return label;
    }

    // ------------------------------------------
    // PARKING MAP
    // ------------------------------------------

    private JPanel createMapCard() {

        JPanel card = new JPanel(
                new BorderLayout(12, 12));

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20));

        JLabel title = new JLabel(
                "LIVE PARKING LAYOUT");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 19));

        title.setForeground(TEXT);

        card.add(
                title,
                BorderLayout.NORTH);

        slotsPanel = new ParkingMapPanel();

        JScrollPane scrollPane =
                new JScrollPane(slotsPanel);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER));

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(20);

        scrollPane.getHorizontalScrollBar()
                .setUnitIncrement(20);

        card.add(
                scrollPane,
                BorderLayout.CENTER);

        JPanel legend = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT, 20, 5));

        legend.setBackground(Color.WHITE);

        legend.add(
                createLegend(
                        "Available",
                        new Color(34, 197, 94)));

        legend.add(
                createLegend(
                        "Occupied",
                        new Color(239, 68, 68)));

        legend.add(
                createLegend(
                        "Driving Area",
                        new Color(100, 116, 139)));

        legend.add(
                createLegend(
                        "Pedestrian Path",
                        new Color(245, 158, 11)));

        card.add(
                legend,
                BorderLayout.SOUTH);

        return card;
    }

    // ------------------------------------------
    // ALLOCATE PARKING
    // ------------------------------------------

    private void allocateParking() {

        String vehicle =
                vehicleField.getText()
                        .trim()
                        .toUpperCase();

        vehicleField.setText(vehicle);

        if (!vehicle.matches(
                "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$")) {

            showWarning(
                    "Enter a valid vehicle number.\n"
                    + "Example: PB65AB1234");

            return;
        }

        int availableMinutes;

        try {

            timeSpinner.commitEdit();

            availableMinutes =
                    (Integer) timeSpinner.getValue();

        } catch (java.text.ParseException e) {

            showWarning(
                    "Enter a valid available time.");

            return;
        }

        try {

            String result =
                    ParkingDatabase.allocateParking(
                            vehicle,
                            availableMinutes);

            loadSlotsFromDatabase();

            JOptionPane.showMessageDialog(
                    this,
                    result,
                    "Parking Allocation",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    // ------------------------------------------
    // RELEASE PARKING
    // ------------------------------------------

    private void releaseParking() {

        String vehicle =
                vehicleField.getText()
                        .trim()
                        .toUpperCase();

        vehicleField.setText(vehicle);

        if (vehicle.isEmpty()) {

            showWarning(
                    "Please enter the vehicle number.");

            return;
        }

        try {

            String result =
                    ParkingDatabase.releaseParking(
                            vehicle);

            loadSlotsFromDatabase();

            JOptionPane.showMessageDialog(
                    this,
                    result,
                    "Vehicle Exit",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    // ------------------------------------------
    // GENERATE PARKING LAYOUT
    // ------------------------------------------

    private void generateParkingLayout() {

        double length;
        double width;

        try {

            length = Double.parseDouble(
                    lengthField.getText().trim());

            width = Double.parseDouble(
                    widthField.getText().trim());

        } catch (NumberFormatException e) {

            showWarning(
                    "Enter valid plot dimensions.");

            return;
        }

        if (!Double.isFinite(length)
                || !Double.isFinite(width)
                || length < 15.5
                || width < 13.8
                || length > 200
                || width > 200) {

            showWarning(
                    "Minimum plot: 15.5 m x 13.8 m.\n"
                    + "Maximum plot: 200 m x 200 m.");

            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Generate a new parking layout?\n"
                        + "All vehicles must be released "
                        + "before replacing the layout.",
                        "Confirm Layout",
                        JOptionPane.YES_NO_OPTION);

        if (confirm !=
                JOptionPane.YES_OPTION) {

            return;
        }

        try {

            int count =
                    ParkingDatabase.createLayout(
                            length, width);

            loadSlotsFromDatabase();

            JOptionPane.showMessageDialog(
                    this,
                    "Parking layout generated successfully!\n"
                    + "Plot: " + length + " m x "
                    + width + " m\n"
                    + "Total Slots: " + count,
                    "Layout Generated",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    // ------------------------------------------
    // DATABASE REFRESH
    // ------------------------------------------

    private void loadSlotsFromDatabase() {

        try {

            slots = ParkingDatabase.getSlots();

            double[] dimensions =
                    ParkingDatabase
                    .getActiveAreaDimensions();

            slotsPanel.setPlotDimensions(
                    dimensions[0],
                    dimensions[1]);

            slotsPanel.setSlots(slots);

            updateStatistics();

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    private void updateStatistics() {

        int total = slots.size();
        int occupied = 0;

        for (ParkingSlot slot : slots) {

            if (slot.isOccupied) {
                occupied++;
            }
        }

        capacityLabel.setText(
                String.valueOf(total));

        availableLabel.setText(
                String.valueOf(total - occupied));

        occupiedLabel.setText(
                String.valueOf(occupied));
    }

    // ------------------------------------------
    // UI HELPER METHODS
    // ------------------------------------------

    private JLabel createSidebarHeading(
            String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        label.setForeground(
                new Color(125, 211, 252));

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        return label;
    }

    private JLabel createInputLabel(
            String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 13));

        label.setForeground(
                new Color(226, 232, 240));

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        label.setBorder(
                BorderFactory.createEmptyBorder(
                        0, 0, 7, 0));

        return label;
    }

    private JTextField createTextField(
            String text) {

        JTextField field =
                new JTextField(text);

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 15));

        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);

        field.setCaretColor(BLUE);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(
                                8, 10, 8, 10)));

        field.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE, 42));

        field.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        return field;
    }

    private JButton createButton(
            String text,
            Color color) {

        JButton button =
                new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 13));

        button.setForeground(Color.WHITE);
        button.setBackground(color);

        button.setOpaque(true);
        button.setBorderPainted(false);

        button.setFocusPainted(false);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE, 44));

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        return button;
    }

    private JPanel createLegend(
            String title,
            Color color) {

        JPanel panel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT, 7, 0));

        panel.setOpaque(false);

        JLabel square = new JLabel(" ");

        square.setOpaque(true);
        square.setBackground(color);

        square.setPreferredSize(
                new Dimension(15, 15));

        JLabel label = new JLabel(title);

        label.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        label.setForeground(TEXT);

        panel.add(square);
        panel.add(label);

        return panel;
    }

    private void showWarning(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Attention",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(
            SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Database Error:\n"
                + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------
    // MAIN METHOD
    // ------------------------------------------

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                UIManager.setLookAndFeel(
                        UIManager
                        .getSystemLookAndFeelClassName());

            } catch (Exception ignored) {
            }

            Main window = new Main();
            window.setVisible(true);
        });
    }
}

// ------------------------------------------
// PARKING SLOT MODEL
// ------------------------------------------

class ParkingSlot {

    String slotId;
    String displayName;
    int walkingTime;
    boolean isOccupied;

    ParkingSlot(
            String slotId,
            String displayName,
            int walkingTime) {

        this.slotId = slotId;
        this.displayName = displayName;
        this.walkingTime = walkingTime;
        this.isOccupied = false;
    }
}
