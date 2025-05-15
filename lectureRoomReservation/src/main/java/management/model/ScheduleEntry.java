package management.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleEntry {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private String reason;

    // 새로 추가할 필드
    private String courseName;
    private String professorName;

    // 기존 생성자 (가능하다면 Deprecated 처리)
    public ScheduleEntry(DayOfWeek day, LocalTime startTime, LocalTime endTime,
                         boolean available, String reason) {
        this(day, startTime, endTime, available, reason, "", "");
    }

    // 새 생성자: 과목명·교수명 포함
    public ScheduleEntry(DayOfWeek day,
                         LocalTime startTime,
                         LocalTime endTime,
                         boolean available,
                         String reason,
                         String courseName,
                         String professorName) {
        this.day            = day;
        this.startTime      = startTime;
        this.endTime        = endTime;
        this.available      = available;
        this.reason         = reason;
        this.courseName     = courseName;
        this.professorName  = professorName;
    }

    // 기존 getter
    public DayOfWeek getDay()            { return day; }
    public LocalTime getStartTime()      { return startTime; }
    public LocalTime getEndTime()        { return endTime; }
    public boolean isAvailable()         { return available; }
    public String getReason()            { return reason; }

    // 새 getter
    public String getCourseName()        { return courseName; }
    public String getProfessorName()     { return professorName; }
}
