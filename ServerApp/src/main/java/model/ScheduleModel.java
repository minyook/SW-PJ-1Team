package model;

import common.ScheduleEntry;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ScheduleModel {
    private final String dataFile;
    private final List<ScheduleEntry> scheduleList = new ArrayList<>();

    public ScheduleModel() throws IOException {
        this("resources/schedule_data.txt");
    }

    public ScheduleModel(String dataFile) throws IOException {
        this.dataFile = dataFile;
        load();
    }

    private void load() throws IOException {
        scheduleList.clear();
        Path path = Paths.get(dataFile).toAbsolutePath();
        if (!Files.exists(path)) Files.createFile(path);

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] tokens = line.split(",");
            if (tokens.length == 4) {
                // 요일, 10:00~10:50, 과목명, 교수명
                try {
                    scheduleList.add(new ScheduleEntry(
                        tokens[0],       // 한글 요일
                        tokens[1],       // 10:00~10:50
                        tokens[2],       // 과목명
                        tokens[3]        // 교수명
                    ));
                } catch (Exception e) {
                    System.err.println("❌ 파싱 실패: " + line + " → " + e.getMessage());
                }
            } else {
                System.err.println("❌ 잘못된 형식: " + line);
            }
        }
    }

    public List<ScheduleEntry> listAll() throws IOException {
        load();
        return new ArrayList<>(scheduleList);
    }

    public void create(ScheduleEntry entry) throws IOException {
        scheduleList.add(entry);
        save();
    }

    public void delete(int index) throws IOException {
        if (index >= 0 && index < scheduleList.size()) {
            scheduleList.remove(index);
            save();
        }
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (ScheduleEntry entry : scheduleList) {
            lines.add(entry.toTextLine()); // ScheduleEntry 내부 메서드로 포맷 저장
        }
        Files.write(Paths.get(dataFile), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
