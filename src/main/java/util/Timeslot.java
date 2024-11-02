package util;

/**
 * Represents timeslots for each appointment
 *
 * @author Paldeep Sekhon
 * @author Aditya Ponni
 */

public class Timeslot implements Comparable<Timeslot> {
    private int hour;
    private int minute;

    public Timeslot(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public String toString() {
        return String.format("%d:%02d %s",
                (hour == 12 || hour == 0) ? 12 : hour % 12, // No leading zero for hours
                minute, // Keep leading zero for minutes
                (hour >= 12) ? "PM" : "AM"); // AM/PM indicator
    }

    @Override
    public int compareTo(Timeslot other) {
        if (this.hour != other.hour) {
            return this.hour - other.hour;
        }
        return this.minute - other.minute;
    }

    // Implementing the equals() method to compare hour and minute
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true; // Same reference
        if (obj == null || getClass() != obj.getClass())
            return false; // Null or different class
        Timeslot timeslot = (Timeslot) obj; // Cast to Timeslot
        return hour == timeslot.hour && minute == timeslot.minute; // Compare hour and minute
    }

    // Implementing the hashCode() method to ensure consistency when using in
    // hash-based collections
    @Override
    public int hashCode() {
        return 31 * hour + minute; // Generate unique hash based on hour and minute
    }

    public static Timeslot fromString(String input) {
        try {
            // Split the time and period parts (e.g., "9:00 AM" -> ["9:00", "AM"])
            String[] parts = input.split(" ");
            String time = parts[0];
            String period = parts[1];

            // Split hours and minutes (e.g., "9:00" -> ["9", "00"])
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Convert hour to 24-hour format if necessary
            if ("PM".equalsIgnoreCase(period) && hour != 12) {
                hour += 12;
            } else if ("AM".equalsIgnoreCase(period) && hour == 12) {
                hour = 0;
            }

            return new Timeslot(hour, minute);
        } catch (Exception e) {
            return null; // Return null if the input format is invalid
        }
    }


    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    /**
     * Sets the hour of this timeslot.
     *
     * @param hour the hour to set (24-hour format).
     */
    public void setHour(int hour) {
        this.hour = hour;
    }

    /**
     * Sets the minute of this timeslot.
     *
     * @param minute the minute to set.
     */
    public void setMinute(int minute) {
        this.minute = minute;
    }
}