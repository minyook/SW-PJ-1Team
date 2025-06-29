package common;

import java.io.Serializable;

public class Reservation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String reservationId;
    private String date;
    private String time;
    private String roomNumber;
    private String username;
    private String status;
    
    public Reservation(String reservationId,
                       String date,
                       String time,
                       String roomNumber,
                       String username,
                       String status) {
        this.reservationId = reservationId;
        this.date          = date;
        this.time          = time;
        this.roomNumber    = roomNumber;
        this.username      = username;
        this.status        = status;
    }

    // Getter & Setter
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    // ▶ 로그인한 사용자 아이디를 꺼낼 getter/setter 추가
    public String getUserId() {
        return username;
    }
    public void setUserId(String userId) {
        this.username = userId;
    }

    // ▶ 기존 getUserName()는 username 필드를 그대로 반환하도록 유지
    public String getUserName() {
        return username;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return date + "," + time + "," + roomNumber + "," + username + "," + status;
    }
}
