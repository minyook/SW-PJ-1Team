
package reservation;

/**
 *
 * @author rbcks
 */
public class RoomStatus {
    private String timeSlot; // 예: "10:00~10:50"
    private String status;   // 예: "비어 있음", "수업", "예약 대기", "예약"

    public RoomStatus(String timeSlot, String status) {
        this.timeSlot = timeSlot;
        this.status = status;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
