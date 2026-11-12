
import java.sql.*;
import java.util.ArrayList;

public class ParkingDatabase {

    // -------------------------------------------------
    // 1. FIND THE CURRENT ACTIVE PARKING AREA
    // -------------------------------------------------

    private static int getActiveAreaId(Connection con)
            throws SQLException {

        String sql =
                "SELECT area_id FROM parking_areas "
                + "WHERE is_active = TRUE "
                + "ORDER BY area_id DESC LIMIT 1";

        try (PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("area_id");
            }
        }

        return -1;
    }

    // -------------------------------------------------
    // 2. GET ACTIVE PLOT DIMENSIONS
    // -------------------------------------------------

    public static double[] getActiveAreaDimensions()
            throws SQLException {

        String sql =
                "SELECT length_m, width_m "
                + "FROM parking_areas "
                + "WHERE is_active = TRUE "
                + "ORDER BY area_id DESC LIMIT 1";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new double[]{
                        rs.getDouble("length_m"),
                        rs.getDouble("width_m")
                };
            }
        }

        return new double[]{0, 0};
    }

    // -------------------------------------------------
    // 3. LOAD PARKING SLOTS FROM MYSQL
    // -------------------------------------------------

    public static ArrayList<ParkingSlot> getSlots()
            throws SQLException {

        ArrayList<ParkingSlot> slots =
                new ArrayList<>();

        try (Connection con =
                     DBConnection.getConnection()) {

            int areaId = getActiveAreaId(con);

            if (areaId == -1) {
                return slots;
            }

            String sql =
                    "SELECT slot_id, display_name, "
                    + "walking_time, is_occupied "
                    + "FROM parking_slots "
                    + "WHERE area_id = ? "
                    + "ORDER BY "
                    + "LEFT(display_name, 1), "
                    + "CAST(SUBSTRING(display_name, 2) "
                    + "AS UNSIGNED)";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, areaId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        ParkingSlot slot =
                                new ParkingSlot(
                                    rs.getString("slot_id"),
                                    rs.getString("display_name"),
                                    rs.getInt("walking_time"));

                        slot.isOccupied =
                                rs.getBoolean("is_occupied");

                        slots.add(slot);
                    }
                }
            }
        }

        return slots;
    }

    // -------------------------------------------------
    // 4. GENERATE AND SAVE A NEW PARKING LAYOUT
    // -------------------------------------------------

    public static int createLayout(
            double length, double width)
            throws SQLException {

        // Minimum plot dimensions now support
        // a single row with one driving aisle.
        if (!Double.isFinite(length)
                || !Double.isFinite(width)
                || length < 15.5
                || width < 13.8
                || length > 200
                || width > 200) {

            throw new SQLException(
                    "Invalid plot dimensions. "
                    + "Minimum: 15.5 m x 13.8 m. "
                    + "Maximum: 200 m x 200 m.");
        }

        ArrayList<ParkingLayout.GeneratedSlot>
                generated =
                ParkingLayout.generate(length, width);

        if (generated.isEmpty()) {
            throw new SQLException(
                    "No parking slots can fit "
                    + "inside these plot dimensions.");
        }

        try (Connection con =
                     DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // Lock the active area while replacing it.
                int oldAreaId = getActiveAreaId(con);

                if (oldAreaId != -1) {

                    String lockArea =
                            "SELECT area_id "
                            + "FROM parking_areas "
                            + "WHERE area_id = ? FOR UPDATE";

                    try (PreparedStatement ps =
                                 con.prepareStatement(lockArea)) {

                        ps.setInt(1, oldAreaId);

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                // Row lock acquired.
                            }
                        }
                    }

                    // Check for vehicles still parked
                    // in the currently active area.
                    String activeVehicles =
                            "SELECT COUNT(*) "
                            + "FROM parking_records pr "
                            + "JOIN parking_slots ps "
                            + "ON pr.slot_id = ps.slot_id "
                            + "WHERE ps.area_id = ? "
                            + "AND pr.exited_at IS NULL";

                    try (PreparedStatement ps =
                                 con.prepareStatement(activeVehicles)) {

                        ps.setInt(1, oldAreaId);

                        try (ResultSet rs = ps.executeQuery()) {

                            rs.next();

                            if (rs.getInt(1) > 0) {
                                throw new SQLException(
                                        "Release all parked "
                                        + "vehicles before generating "
                                        + "a new layout.");
                            }
                        }
                    }

                    // Also check occupied slot flags.
                    String occupiedSql =
                            "SELECT COUNT(*) "
                            + "FROM parking_slots "
                            + "WHERE area_id = ? "
                            + "AND is_occupied = TRUE";

                    try (PreparedStatement ps =
                                 con.prepareStatement(occupiedSql)) {

                        ps.setInt(1, oldAreaId);

                        try (ResultSet rs = ps.executeQuery()) {

                            rs.next();

                            if (rs.getInt(1) > 0) {
                                throw new SQLException(
                                        "Some parking slots are still "
                                        + "occupied. Release them first.");
                            }
                        }
                    }
                }

                // Deactivate previous layouts.
                try (PreparedStatement ps =
                             con.prepareStatement(
                                "UPDATE parking_areas "
                                + "SET is_active = FALSE "
                                + "WHERE is_active = TRUE")) {

                    ps.executeUpdate();
                }

                // Create a new active parking area.
                int newAreaId;

                String insertArea =
                        "INSERT INTO parking_areas "
                        + "(length_m, width_m, is_active) "
                        + "VALUES (?, ?, TRUE)";

                try (PreparedStatement ps =
                             con.prepareStatement(
                                insertArea,
                                Statement.RETURN_GENERATED_KEYS)) {

                    ps.setDouble(1, length);
                    ps.setDouble(2, width);

                    ps.executeUpdate();

                    try (ResultSet keys =
                                 ps.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Failed to create parking area.");
                        }

                        newAreaId = keys.getInt(1);
                    }
                }

                // Save all generated slots.
                String insertSlot =
                        "INSERT INTO parking_slots "
                        + "(slot_id, display_name, "
                        + "walking_time, is_occupied, area_id) "
                        + "VALUES (?, ?, ?, FALSE, ?)";

                try (PreparedStatement ps =
                             con.prepareStatement(insertSlot)) {

                    int counter = 1;

                    for (ParkingLayout.GeneratedSlot slot
                            : generated) {

                        // Unique ID preserves old history.
                        String uniqueSlotId =
                                "A" + newAreaId
                                + "S" + counter;

                        ps.setString(1, uniqueSlotId);
                        ps.setString(2, slot.name);
                        ps.setInt(3, slot.walkingTime);
                        ps.setInt(4, newAreaId);

                        ps.addBatch();
                        counter++;
                    }

                    ps.executeBatch();
                }

                con.commit();

                return generated.size();

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;

            } finally {

                con.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------
    // 5. CHECK IF A VEHICLE IS ALREADY PARKED
    // -------------------------------------------------

    public static boolean vehicleAlreadyParked(
            String vehicleNumber)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) "
                + "FROM parking_records "
                + "WHERE vehicle_number = ? "
                + "AND exited_at IS NULL";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(
                    1, vehicleNumber.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {

                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // -------------------------------------------------
    // 6. SMART TIME-BASED PARKING ALLOCATION
    // -------------------------------------------------

    public static String allocateParking(
            String vehicleNumber,
            int availableMinutes)
            throws SQLException {

        vehicleNumber =
                vehicleNumber.trim().toUpperCase();

        if (availableMinutes < 1) {
            return "Available time must be positive.";
        }

        try (Connection con =
                     DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                int areaId = getActiveAreaId(con);

                if (areaId == -1) {
                    con.rollback();
                    return "Generate a parking layout first.";
                }

                // Lock the active area to coordinate
                // allocation with layout replacement.
                try (PreparedStatement ps =
                             con.prepareStatement(
                                "SELECT area_id "
                                + "FROM parking_areas "
                                + "WHERE area_id = ? FOR UPDATE")) {

                    ps.setInt(1, areaId);
                    ps.executeQuery().close();
                }

                // Check duplicate active vehicle.
                String duplicateSql =
                        "SELECT COUNT(*) "
                        + "FROM parking_records "
                        + "WHERE vehicle_number = ? "
                        + "AND exited_at IS NULL";

                try (PreparedStatement ps =
                             con.prepareStatement(duplicateSql)) {

                    ps.setString(1, vehicleNumber);

                    try (ResultSet rs = ps.executeQuery()) {

                        rs.next();

                        if (rs.getInt(1) > 0) {
                            con.rollback();
                            return "This vehicle is already parked.";
                        }
                    }
                }

                // Pick the farthest eligible free slot.
                String findSlot =
                        "SELECT slot_id, display_name, "
                        + "walking_time "
                        + "FROM parking_slots "
                        + "WHERE area_id = ? "
                        + "AND is_occupied = FALSE "
                        + "AND walking_time * 2 <= ? "
                        + "ORDER BY walking_time DESC, "
                        + "display_name ASC "
                        + "LIMIT 1 FOR UPDATE";

                String selectedId = null;
                String selectedName = null;
                int selectedWalkingTime = 0;

                try (PreparedStatement ps =
                             con.prepareStatement(findSlot)) {

                    ps.setInt(1, areaId);
                    ps.setInt(2, availableMinutes);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {

                            selectedId =
                                    rs.getString("slot_id");

                            selectedName =
                                    rs.getString("display_name");

                            selectedWalkingTime =
                                    rs.getInt("walking_time");
                        }
                    }
                }

                if (selectedId == null) {

                    con.rollback();

                    return "No suitable parking slot "
                            + "is available for your time.";
                }

                // Mark selected slot as occupied.
                String updateSlot =
                        "UPDATE parking_slots "
                        + "SET is_occupied = TRUE "
                        + "WHERE slot_id = ? "
                        + "AND is_occupied = FALSE";

                try (PreparedStatement ps =
                             con.prepareStatement(updateSlot)) {

                    ps.setString(1, selectedId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Slot was allocated by "
                                + "another operation.");
                    }
                }

                // Save vehicle parking record.
                String insertRecord =
                        "INSERT INTO parking_records "
                        + "(vehicle_number, slot_id, "
                        + "available_minutes) "
                        + "VALUES (?, ?, ?)";

                try (PreparedStatement ps =
                             con.prepareStatement(insertRecord)) {

                    ps.setString(1, vehicleNumber);
                    ps.setString(2, selectedId);
                    ps.setInt(3, availableMinutes);

                    ps.executeUpdate();
                }

                con.commit();

                return "Vehicle: " + vehicleNumber
                        + "\nAllocated Slot: " + selectedName
                        + "\nOne-way walking time: "
                        + selectedWalkingTime + " min"
                        + "\nRound-trip walking time: "
                        + (selectedWalkingTime * 2) + " min";

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;

            } finally {

                con.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------
    // 7. RELEASE PARKING SLOT / VEHICLE EXIT
    // -------------------------------------------------

    public static String releaseParking(
            String vehicleNumber)
            throws SQLException {

        vehicleNumber =
                vehicleNumber.trim().toUpperCase();

        try (Connection con =
                     DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // Lock active area to avoid concurrent
                // layout replacement.
                int areaId = getActiveAreaId(con);

                if (areaId == -1) {
                    con.rollback();
                    return "No active parking area.";
                }

                try (PreparedStatement ps =
                             con.prepareStatement(
                                "SELECT area_id "
                                + "FROM parking_areas "
                                + "WHERE area_id = ? FOR UPDATE")) {

                    ps.setInt(1, areaId);
                    ps.executeQuery().close();
                }

                String findRecord =
                        "SELECT pr.id, pr.slot_id, "
                        + "ps.display_name "
                        + "FROM parking_records pr "
                        + "JOIN parking_slots ps "
                        + "ON pr.slot_id = ps.slot_id "
                        + "WHERE pr.vehicle_number = ? "
                        + "AND pr.exited_at IS NULL "
                        + "AND ps.area_id = ? "
                        + "ORDER BY pr.id DESC "
                        + "LIMIT 1 FOR UPDATE";

                int recordId = -1;
                String slotId = null;
                String displayName = null;

                try (PreparedStatement ps =
                             con.prepareStatement(findRecord)) {

                    ps.setString(1, vehicleNumber);
                    ps.setInt(2, areaId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {

                            recordId = rs.getInt("id");
                            slotId = rs.getString("slot_id");
                            displayName =
                                    rs.getString("display_name");
                        }
                    }
                }

                if (recordId == -1) {

                    con.rollback();

                    return "No active parking record "
                            + "found for this vehicle.";
                }

                // Free the occupied slot.
                try (PreparedStatement ps =
                             con.prepareStatement(
                                "UPDATE parking_slots "
                                + "SET is_occupied = FALSE "
                                + "WHERE slot_id = ?")) {

                    ps.setString(1, slotId);
                    ps.executeUpdate();
                }

                // Record exit time.
                try (PreparedStatement ps =
                             con.prepareStatement(
                                "UPDATE parking_records "
                                + "SET exited_at = CURRENT_TIMESTAMP "
                                + "WHERE id = ? "
                                + "AND exited_at IS NULL")) {

                    ps.setInt(1, recordId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Failed to record vehicle exit.");
                    }
                }

                con.commit();

                return "Vehicle: " + vehicleNumber
                        + "\nReleased Slot: " + displayName
                        + "\nParking slot is now AVAILABLE.";

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;

            } finally {

                con.setAutoCommit(true);
            }
        }
    }
}
