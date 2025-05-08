/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.model;

/**
 *
 * @author limmi
 */
public class Reservation {
    private String id;
    private String roomId;
    private String user;
    private String status;
    private String reason;

    public Reservation(String id, String roomId, String user,
                       String status, String reason) {
        this.id     = id;
        this.roomId = roomId;
        this.user   = user;
        this.status = status;
        this.reason = reason;
    }

    // ← 여기를 꼭 추가!
    public String getId()       { return id; }
    public String getRoomId()   { return roomId; }
    public String getUser()     { return user; }
    public String getStatus()   { return status; }
    public String getReason()   { return reason; }

    public void setStatus(String status)   { this.status = status; }
    public void setReason(String reason)   { this.reason = reason; }
}
