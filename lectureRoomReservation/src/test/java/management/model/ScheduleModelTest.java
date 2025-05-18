package management.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleModelTest {

    private ScheduleModel modelWithDir(Path dir) {
        return new ScheduleModel(true) {
            @Override
            public List<ScheduleEntry> load(String roomId) throws IOException {
                Path p = dir.resolve(String.format("schedule_%s.txt", roomId));
                if (!Files.exists(p)) return List.of();
                return Files.readAllLines(p).stream()
                            .map(this::parseLine)
                            .collect(Collectors.toList());
            }

            @Override
            public void saveAppend(String roomId, ScheduleEntry e) throws IOException {
                Path p = dir.resolve(String.format("schedule_%s.txt", roomId));
                if (!Files.exists(p)) Files.createFile(p);
                try (BufferedWriter w = Files.newBufferedWriter(p, StandardOpenOption.APPEND)) {
                    w.write(this.toLine(e));
                    w.newLine();
                }
            }
        };
    }

    @Test
    void testLoadEmptyWhenNoFile(@TempDir Path dir) throws IOException {
        ScheduleModel m = modelWithDir(dir);
        List<ScheduleEntry> list = m.load("NONE");
        assertTrue(list.isEmpty());
    }

    @Test
    void testSaveAppendAndLoad(@TempDir Path dir) throws IOException {
        ScheduleModel m = modelWithDir(dir);
        ScheduleEntry e1 = new ScheduleEntry(
            DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(9,50),
            true, "", "알고리즘", "김교수"
        );
        m.saveAppend("R1", e1);
        List<ScheduleEntry> out1 = m.load("R1");
        assertEquals(1, out1.size());

        ScheduleEntry e2 = new ScheduleEntry(
            DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(14,50),
            false, "점검", "DB", "이교수"
        );
        m.saveAppend("R1", e2);
        List<ScheduleEntry> out2 = m.load("R1");
        assertEquals(2, out2.size());
    }
}
