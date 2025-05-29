package common;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleEntry implements Serializable {
    private static final long serialVersionUID = 1L;
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

    // 기존 생성자들 아래에 추가
    public ScheduleEntry(String dayStr, String timeStr, String courseName) {
        this.day = DayOfWeek.valueOf(dayStr.toUpperCase());
        String[] times = timeStr.split("~");
        this.startTime = LocalTime.parse(times[0]);
        this.endTime = LocalTime.parse(times[1]);

        this.available = true;
        this.reason = "";
        this.courseName = courseName;
        this.professorName = "";
    }

    // 🔸 텍스트 파일로부터 로딩할 때 사용할 생성자
    public ScheduleEntry(String dayKor, String timeRange, String courseName, String professorName) {
        this.day = convertKorDayToEnum(dayKor);
        String[] times = timeRange.split("~");
        this.startTime = LocalTime.parse(times[0]);
        this.endTime = LocalTime.parse(times[1]);
        this.available = true;
        this.reason = "";
        this.courseName = courseName;
        this.professorName = professorName;
    }

    // 🔸 파일로 저장 시 사용
    public String toTextLine() {
        String dayKor = convertEnumToKorDay(day);
        return String.format("%s,%s~%s,%s,%s",
            dayKor,
            startTime.toString(),
            endTime.toString(),
            courseName,
            professorName
        );
    }

    // 🔸 한글 요일 → DayOfWeek 변환
    public static DayOfWeek convertKorDayToEnum(String kor) {
        return switch (kor) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            case "토" -> DayOfWeek.SATURDAY;
            case "일" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("잘못된 요일: " + kor);
        };
    }

    // 🔸 DayOfWeek → 한글 요일
    private String convertEnumToKorDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    // 오버라이드 toString() 추가
    @Override
    public String toString() {
        String dayKor = convertEnumToKorDay(day);
        return String.format("%s %s~%s | %s | %s",
                dayKor,
                startTime,
                endTime,
                courseName,
                professorName
        );
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
