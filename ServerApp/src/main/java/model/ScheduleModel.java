package model;

import common.ScheduleEntry;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ScheduleModel {
    private static final String DATA_FILE = "resources/schedule_data.txt";
    private final List<ScheduleEntry> scheduleList = new ArrayList<>();

    public ScheduleModel() throws IOException {
        load();
    }

    private void load() throws IOException {
        scheduleList.clear();
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) Files.createFile(path);

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] tokens = line.split(",");
            if (tokens.length == 3) {
                ScheduleEntry entry = new ScheduleEntry(tokens[0], tokens[1], tokens[2]);
                scheduleList.add(entry);
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
        for (ScheduleEntry s : scheduleList) {
            lines.add(String.join(",", s.getDay().toString(), s.getStartTime() + "~" + s.getEndTime(), s.getCourseName()));
        }
        Files.write(Paths.get(DATA_FILE), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}