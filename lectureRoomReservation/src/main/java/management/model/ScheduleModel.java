// src/main/java/management/model/ScheduleModel.java
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
public class ScheduleModel {
    // resources 폴더 안 경로
    private static final String RES_DIR = "src/main/resources/";
    private static final String SCHEDULE_FMT = RES_DIR + "schedule_%s.txt";

    public List<ScheduleEntry> load(String roomId) throws IOException {
        Path p = Paths.get(String.format(SCHEDULE_FMT, roomId));
        if (!Files.exists(p)) {
            return List.of();
        }
        return Files.readAllLines(p).stream()
            .map(this::parseLine)
            .collect(Collectors.toList());
    }

    public void saveAppend(String roomId, ScheduleEntry e) throws IOException {
        Path p = Paths.get(String.format(SCHEDULE_FMT, roomId));
        if (!Files.exists(p)) {
            Files.createFile(p);
        }
        try (BufferedWriter w = Files.newBufferedWriter(
                p,
                StandardOpenOption.APPEND
        )) {
            w.write(toLine(e));
            w.newLine();
        }
    }

    private ScheduleEntry parseLine(String ln) {
        String[] f = ln.split(",", 4);
        DayOfWeek day = parseDay(f[0]);
        String[] ts = f[1].split("~");
        LocalTime st = LocalTime.parse(ts[0]), et = LocalTime.parse(ts[1]);
        boolean avail = "사용가능".equals(f[2]);
        String reason = (!avail && f.length>3) ? f[3] : "";
        return new ScheduleEntry(day, st, et, avail, reason);
    }

    private String toLine(ScheduleEntry e) {
        String day = switch (e.getDay()) {
            case MONDAY    -> "월";
            case TUESDAY   -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY  -> "목";
            case FRIDAY    -> "금";
            default        -> e.getDay().toString();
        };
        return String.join(",",
            day,
            e.getStartTime() + "~" + e.getEndTime(),
            e.isAvailable() ? "사용가능" : "사용불가능",
            e.isAvailable() ? "" : e.getReason()
        );
    }

    private DayOfWeek parseDay(String ko) {
        return switch (ko) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            default   -> throw new IllegalArgumentException("잘못된 요일: "+ko);
        };
    }
}
