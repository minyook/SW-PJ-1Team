package common;

import java.io.Serializable; // 직렬화를 위한 import

/**
 *
 * @author rbcks
 */
public class RoomStatus implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID

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
