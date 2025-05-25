package common;

import java.io.Serializable;

public class Room implements Serializable {
    public enum Availability { OPEN, CLOSED }

    private String roomId;
    private Availability availability;
    private String closeReason;

    public Room(String roomId, Availability avail) {
        this.roomId = roomId;
        this.availability = avail;
    }

    public Room(String roomId) {
        this.roomId = roomId;
        this.availability = Availability.OPEN;
        this.closeReason = "";
    }

    public String getRoomId() { return roomId; }
    public Availability getAvailability() { return availability; }
    public void setAvailability(Availability a) { this.availability = a; }
    public String getCloseReason() { return closeReason; }
    public void setCloseReason(String r) { this.closeReason = r; }
}
