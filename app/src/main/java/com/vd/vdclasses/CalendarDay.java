package com.vd.vdclasses;

public class CalendarDay {

    public enum Type { HEADER, EMPTY, DAY }

    private final Type type;
    private String label;       // for HEADER: "S", "M", etc.
    private int dayNumber;      // for DAY: 1-31
    private String dateString;  // for DAY: "yyyy-MM-dd"
    private boolean isPresent;
    private boolean isFuture;
    private boolean isToday;
    private long checkInTimestamp;

    // Header constructor
    public CalendarDay(String label) {
        this.type = Type.HEADER;
        this.label = label;
    }

    // Empty cell constructor
    public CalendarDay() {
        this.type = Type.EMPTY;
    }

    // Day cell constructor
    public CalendarDay(int dayNumber, String dateString, boolean isPresent, boolean isFuture, boolean isToday, long checkInTimestamp) {
        this.type = Type.DAY;
        this.dayNumber = dayNumber;
        this.dateString = dateString;
        this.isPresent = isPresent;
        this.isFuture = isFuture;
        this.isToday = isToday;
        this.checkInTimestamp = checkInTimestamp;
    }

    public Type getType() { return type; }
    public String getLabel() { return label; }
    public int getDayNumber() { return dayNumber; }
    public String getDateString() { return dateString; }
    public boolean isPresent() { return isPresent; }
    public boolean isFuture() { return isFuture; }
    public boolean isToday() { return isToday; }
    public long getCheckInTimestamp() { return checkInTimestamp; }
}
