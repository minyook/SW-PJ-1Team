package model;

import common.ScheduleEntry;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.DayOfWeek;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleModelTest {

    private static final String TEST_FILE = "src/test/resources/test_schedule.txt";
    private ScheduleModel model;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 데이터 초기화
        Files.write(Paths.get(TEST_FILE), List.of(
            "수,16:00~16:50,동계캡스톤,이규찬교수님",
            "목,13:00~14:15,데이터베이스,김민지교수님"
        ));
        model = new ScheduleModel(TEST_FILE);
    }

    @Test
    void testListAllLoadsSchedule() throws IOException {
        List<ScheduleEntry> list = model.listAll();
        assertEquals(2, list.size());

        ScheduleEntry first = list.get(0);
        assertEquals(DayOfWeek.WEDNESDAY, first.getDay());
        assertEquals("동계캡스톤", first.getCourseName());
        assertEquals("이규찬교수님", first.getProfessorName());
    }

    @Test
    void testCreateAddsSchedule() throws IOException {
        ScheduleEntry newEntry = new ScheduleEntry("금", "09:00~10:00", "알고리즘", "최교수님");
        model.create(newEntry);

        List<ScheduleEntry> list = model.listAll();
        assertEquals(3, list.size());
        ScheduleEntry last = list.get(2);
        assertEquals("알고리즘", last.getCourseName());
        assertEquals("최교수님", last.getProfessorName());
    }

    @Test
    void testDeleteScheduleEntry() throws IOException {
        model.delete(0);
        List<ScheduleEntry> list = model.listAll();
        assertEquals(1, list.size());
        assertEquals("데이터베이스", list.get(0).getCourseName());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }
}
