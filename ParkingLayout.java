
import java.util.ArrayList;

public class ParkingLayout {

    // ------------------------------------------
    // PARKING DIMENSIONS (METRES)
    // ------------------------------------------

    private static final double SLOT_WIDTH = 2.5;
    private static final double SLOT_DEPTH = 5.0;

    private static final double DRIVING_AISLE = 6.0;
    private static final double ENTRANCE_LANE = 6.0;

    private static final double PEDESTRIAN_PATH = 1.8;
    private static final double TURNING_CLEARANCE = 6.0;

    private static final double EDGE_MARGIN = 0.5;

    // Walking speed: 0.8 metres per second.
    private static final double WALKING_SPEED = 0.8;

    // ------------------------------------------
    // GENERATED SLOT MODEL
    // ------------------------------------------

    public static class GeneratedSlot {

        String name;
        int walkingTime;

        double x;
        double y;
        double width;
        double depth;

        GeneratedSlot(
                String name,
                int walkingTime,
                double x,
                double y,
                double width,
                double depth) {

            this.name = name;
            this.walkingTime = walkingTime;

            this.x = x;
            this.y = y;

            this.width = width;
            this.depth = depth;
        }
    }

    // ------------------------------------------
    // GENERATE PARKING LAYOUT
    // ------------------------------------------

    public static ArrayList<GeneratedSlot> generate(
            double length,
            double width) {

        ArrayList<GeneratedSlot> result =
                new ArrayList<>();

        if (!Double.isFinite(length)
                || !Double.isFinite(width)
                || length <= 0
                || width <= 0) {

            return result;
        }

        // --------------------------------------
        // RESERVED AREAS
        // --------------------------------------

        double parkingStartX =
                EDGE_MARGIN + ENTRANCE_LANE;

        double parkingEndX =
                length - EDGE_MARGIN
                - TURNING_CLEARANCE;

        double parkingStartY =
                EDGE_MARGIN + PEDESTRIAN_PATH;

        double parkingEndY =
                width - EDGE_MARGIN;

        double usableLength =
                parkingEndX - parkingStartX;

        double usableWidth =
                parkingEndY - parkingStartY;

        if (usableLength < SLOT_WIDTH
                || usableWidth < SLOT_DEPTH
                + DRIVING_AISLE) {

            return result;
        }

        // --------------------------------------
        // CALCULATE PARKING COLUMNS
        // --------------------------------------

        int columns = (int) Math.floor(
                usableLength / SLOT_WIDTH);

        // --------------------------------------
        // CALCULATE PARKING ROWS
        // --------------------------------------

        int rows = 0;

        for (int candidate = 1;
                candidate <= 26;
                candidate++) {

            int roads = (candidate + 1) / 2;

            double requiredWidth =
                    candidate * SLOT_DEPTH
                    + roads * DRIVING_AISLE;

            if (requiredWidth <= usableWidth) {
                rows = candidate;
            } else {
                break;
            }
        }

        if (rows == 0 || columns == 0) {
            return result;
        }

        // --------------------------------------
        // ENTRANCE POSITION
        // --------------------------------------

        double entranceX =
                EDGE_MARGIN + ENTRANCE_LANE;

        double entranceY =
                width - EDGE_MARGIN;

        // --------------------------------------
        // GENERATE PARKING SLOTS
        // --------------------------------------

        for (int row = 0; row < rows; row++) {

            char rowLetter =
                    (char) ('A' + row);

            int pairIndex = row / 2;

            boolean secondRow =
                    (row % 2 == 1);

            // ----------------------------------
            // CALCULATE ROW POSITION
            // ----------------------------------

            double pairStartY =
                    parkingStartY
                    + pairIndex * (
                            2 * SLOT_DEPTH
                            + DRIVING_AISLE);

            double rowY;

            if (secondRow) {

                rowY =
                        pairStartY
                        + SLOT_DEPTH
                        + DRIVING_AISLE;

            } else {

                rowY = pairStartY;
            }

            // ----------------------------------
            // CALCULATE ACCESSIBLE AISLE
            // ----------------------------------

            double aisleCenterY;

            if (secondRow) {

                // Second row uses the aisle above it.
                aisleCenterY =
                        rowY - DRIVING_AISLE / 2.0;

            } else {

                // First row uses the aisle below it.
                aisleCenterY =
                        rowY + SLOT_DEPTH
                        + DRIVING_AISLE / 2.0;
            }

            // ----------------------------------
            // GENERATE PARKING COLUMNS
            // ----------------------------------

            for (int col = 0;
                    col < columns;
                    col++) {

                double slotX =
                        parkingStartX
                        + col * SLOT_WIDTH;

                String slotName =
                        rowLetter
                        + String.valueOf(col + 1);

                // Centre of parking slot.
                double slotCenterX =
                        slotX + SLOT_WIDTH / 2.0;

                double slotCenterY =
                        rowY + SLOT_DEPTH / 2.0;

                // ----------------------------------
                // CALCULATE WALKING ROUTE
                // ----------------------------------

                // 1. Slot centre to accessible aisle.
                double slotToAisle =
                        Math.abs(
                                slotCenterY
                                - aisleCenterY);

                // 2. Along aisle to entrance lane.
                double aisleToEntranceLane =
                        Math.abs(
                                slotCenterX
                                - entranceX);

                // 3. Along entrance lane to entrance.
                double entranceLaneDistance =
                        Math.abs(
                                aisleCenterY
                                - entranceY);

                // Total estimated walking distance.
                double walkingDistance =
                        slotToAisle
                        + aisleToEntranceLane
                        + entranceLaneDistance;

                // ----------------------------------
                // CONVERT DISTANCE TO MINUTES
                // ----------------------------------

                double walkingSeconds =
                        walkingDistance
                        / WALKING_SPEED;

                double walkingMinutesExact =
                        walkingSeconds / 60.0;

                // Round up to the next whole minute.
                int walkingMinutes =
                        Math.max(
                                1,
                                (int) Math.ceil(
                                        walkingMinutesExact));

                // ----------------------------------
                // ADD GENERATED SLOT
                // ----------------------------------

                result.add(
                        new GeneratedSlot(
                                slotName,
                                walkingMinutes,
                                slotX,
                                rowY,
                                SLOT_WIDTH,
                                SLOT_DEPTH));
            }
        }

        return result;
    }
}
