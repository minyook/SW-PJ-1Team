/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.time.LocalDate;
import java.time.LocalTime;

public class AdminReservation {
    public enum Status { PENDING, APPROVED, REJECTED }

    private int id;                  // 예약 번호
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomId;
    private String userName;
    private Status status;

    public AdminReservation(int id, LocalDate date, LocalTime start, LocalTime end,
                       String roomId, String userName, Status status) {
        this.id = id;
        this.date = date;
        this.startTime = start;
        this.endTime = end;
        this.roomId = roomId;
        this.userName = userName;
        this.status = status;
    }
    public int getReservationId() {
        return id;
    }
    // --- getters / setters 생략 ---
    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getRoomId() { return roomId; }
    public String getUserName() { return userName; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}

