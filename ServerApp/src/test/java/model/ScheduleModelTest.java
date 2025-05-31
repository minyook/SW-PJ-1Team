package model;

import common.ScheduleEntry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleModelTest {

    @TempDir
    Path tempDir;             // 매 테스트마다 새 임시 폴더

    private Path testFile;
    private ScheduleModel model;

    @BeforeEach
    void setUp() throws IOException {
        // 1) tempDir 안에 테스트용 파일 생성
        testFile = tempDir.resolve("test_schedule.txt");
        Files.write(testFile, List.of(
            "수,16:00~16:50,동계캡스톤,이규찬교수님",
            "목,13:00~14:15,데이터베이스,김민지교수님"
        ), StandardOpenOption.CREATE);

        // 2) 파일 경로 넘겨서 모델 초기화
        model = new ScheduleModel(testFile.toString());
    }

    @Test
    void testListAllLoadsSchedule() throws IOException {
        List<ScheduleEntry> list = model.listAll();
        assertEquals(2, list.size(), "초기 스케줄 항목은 2개여야 합니다");

        // ▶ System.err 출력 추가
        System.err.println("=== 전체 스케줄 목록 ===");
        list.forEach(e -> System.err.println(
            e.getDay() + " " +
            e.getStartTime() + "~" + e.getEndTime() + "  " +
            e.getCourseName() + " / " +
            e.getProfessorName()
        ));
        System.err.println("=======================");

        // 기존 검증
        assertEquals("동계캡스톤", list.get(0).getCourseName());
        assertEquals("이규찬교수님", list.get(0).getProfessorName());
    }

    @Test
    void testCreateAddsNewScheduleEntry() throws IOException {
        ScheduleEntry newEntry = new ScheduleEntry("금", "09:00~10:00", "알고리즘", "최교수님");
        model.create(newEntry);

        List<ScheduleEntry> list = model.listAll();
        assertEquals(3, list.size(), "새 항목 추가 후 총 3개여야 합니다");

        // ▶ System.err 출력
        System.err.println("=== 항목 추가 후 스케줄 ===");
        list.forEach(e -> System.err.println(e));
        System.err.println("========================");

        assertEquals("알고리즘", list.get(2).getCourseName());
        assertEquals("최교수님", list.get(2).getProfessorName());
    }

    @Test
    void testDeleteRemovesScheduleEntry() throws IOException {
        model.delete(0);

        List<ScheduleEntry> list = model.listAll();
        assertEquals(1, list.size(), "삭제 후 남은 항목은 1개여야 합니다");

        // ▶ System.err 출력
        System.err.println("=== 삭제 후 스케줄 ===");
        list.forEach(System.err::println);
        System.err.println("===================");

        assertEquals("데이터베이스", list.get(0).getCourseName());
    }
}
