/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleEntry {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;     // true=사용가능, false=불가능
    private String reason;         // 불가능 사유

    public ScheduleEntry(DayOfWeek day, LocalTime start, LocalTime end,
                         boolean available, String reason) {
        this.day = day;
        this.startTime = start;
        this.endTime = end;
        this.available = available;
        this.reason = reason;
    }
    // --- getters / setters 생략 ---
    public DayOfWeek getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isAvailable() { return available; }
    public String getReason() { return reason; }
}

