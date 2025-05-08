/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.model;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class ScheduleModel {
    private static final String SCHEDULE_FMT = "src/main/resources/schedule_%s.txt";

    public List<ScheduleEntry> load(String roomId) throws IOException {
        Path p = Paths.get(String.format(SCHEDULE_FMT, roomId));
        if (!Files.exists(p)) return new ArrayList<>();
        return Files.readAllLines(p).stream().map(ln -> {
            // ex. "월,09:00~09:50,사용불가능,사유"
            String[] f = ln.split(",");
            DayOfWeek day = DayOfWeek.valueOf(convertKoreanDay(f[0]));
            String[] times = f[1].split("~");
            LocalTime st = LocalTime.parse(times[0]), et = LocalTime.parse(times[1]);
            boolean avail = f[2].equals("사용가능");
            String reason = (!avail && f.length>3) ? f[3] : "";
            return new ScheduleEntry(day, st, et, avail, reason);
        }).collect(Collectors.toList());
    }

    public void save(String roomId, List<ScheduleEntry> entries) throws IOException {
        List<String> out = new ArrayList<>();
        // … build out list
        Files.write(
            Paths.get(String.format(SCHEDULE_FMT, roomId)),
            out,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private String convertKoreanDay(String ko) {
        switch(ko) {
            case "월": return "MONDAY";
            case "화": return "TUESDAY";
            case "수": return "WEDNESDAY";
            case "목": return "THURSDAY";
            case "금": return "FRIDAY";
            default: throw new IllegalArgumentException(ko);
        }
    }
    private String toKoreanDay(DayOfWeek d) {
        switch(d) {
            case MONDAY: return "월";
            case TUESDAY: return "화";
            case WEDNESDAY: return "수";
            case THURSDAY: return "목";
            case FRIDAY: return "금";
            default: throw new IllegalArgumentException(d.toString());
        }
    }
}

