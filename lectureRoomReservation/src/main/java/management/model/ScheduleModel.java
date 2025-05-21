package management.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * src/main/resources/ 폴더 내 schedule_<roomId>.txt 파일을
 * 읽고, append 방식으로 저장하는 모델 클래스
 */
// ScheduleModel.java

// ... 생략 imports ...

public class ScheduleModel {
    // 기존 resources 폴더 대신
    private static final Path DATA_DIR = Paths.get("data", "schedules");
    private static final String SCHEDULE_FMT = "schedule_%s.txt";

    public ScheduleModel() throws IOException {
        // 첫 실행 때 데이터 폴더가 없으면 생성
        Files.createDirectories(DATA_DIR);
    }
    
    protected ScheduleModel(boolean skipLoad) {
        // 아무 것도 하지 않음 → load() 호출을 막기만 하면 됩니다
    }

    public List<ScheduleEntry> load(String roomId) throws IOException {
        Path p = DATA_DIR.resolve(String.format(SCHEDULE_FMT, roomId));
        if (!Files.exists(p)) return List.of();
        return Files.readAllLines(p).stream()
                        .map(this::parseLine)
                        .collect(Collectors.toList());
    }

    public void saveAppend(String roomId, ScheduleEntry e) throws IOException {
        Path p = DATA_DIR.resolve(String.format(SCHEDULE_FMT, roomId));
        if (!Files.exists(p)) Files.createFile(p);
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardOpenOption.APPEND)) {
            w.write(toLine(e));
            w.newLine();
        }
    }
    

    protected ScheduleEntry parseLine(String ln) {
        // 먼저 comma split
        String[] f = ln.split(",", -1);
        DayOfWeek day  = parseDay(f[0]);
        String[] ts   = f[1].split("~");
        LocalTime st  = LocalTime.parse(ts[0]);
        LocalTime et  = LocalTime.parse(ts[1]);

        if (f.length == 4) {
            // 사용 가능: [요일,시간,과목명,교수명]
            String course = f[2];
            String prof   = f[3];
            return new ScheduleEntry(day, st, et, true, "", course, prof);
        } else {
            // 사용 불가능: [요일,시간,사용불가능,사유,과목명,교수명]
            boolean avail    = "사용가능".equals(f[2]);
            String reason    = (!avail && f.length > 3) ? f[3] : "";
            String course    = (f.length > 4) ? f[4] : "";
            String professor = (f.length > 5) ? f[5] : "";
            return new ScheduleEntry(day, st, et, avail, reason, course, professor);
        }
    }

    protected String toLine(ScheduleEntry e) {
        String dayK = switch (e.getDay()) {
            case MONDAY    -> "월";
            case TUESDAY   -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY  -> "목";
            case FRIDAY    -> "금";
            default        -> e.getDay().name();
        };
        String slot = e.getStartTime() + "~" + e.getEndTime();

        if (e.isAvailable()) {
            // 사용 가능: 4개 필드만
            return String.join(",",
                dayK,
                slot,
                e.getCourseName(),
                e.getProfessorName()
            );
        } else {
            // 사용 불가능: 기존 6개 필드
            return String.join(",",
                dayK,
                slot,
                "사용불가능",
                e.getReason(),
                e.getCourseName(),
                e.getProfessorName()
            );
        }
    }

    private DayOfWeek parseDay(String ko) {
        return switch (ko) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            default   -> throw new IllegalArgumentException("잘못된 요일: " + ko);
        };
    }
}

