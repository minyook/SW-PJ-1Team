/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.model;

public class Room {
    public enum Availability { OPEN, CLOSED } 

    private String roomId;           // ex. "911"
    private Availability availability;
    private String closeReason;      // 막혔을 때 설명

    public Room(String roomId, Availability avail, String reason) {
        this.roomId = roomId;
        this.availability = avail;
        this.closeReason = reason;
    }
    // --- getters / setters 생략 ---
    public String getRoomId() { return roomId; }
    public Availability getAvailability() { return availability; }
    public void setAvailability(Availability a) { this.availability = a; }
    public String getCloseReason() { return closeReason; }
    public void setCloseReason(String r) { this.closeReason = r; }
}

