package timetable;

import java.nio.file.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TimeTableModelTest {

    // 실제 리소스 파일 경로
    private final Path reservationPath = Paths.get("src/main/resources/reservation_data.txt");

    // 임시 백업 파일 경로
    private Path backupReservationFile;

    @BeforeEach
    public void setUp() throws Exception {
        // 원본 예약 파일 백업 (임시 파일 생성)
        backupReservationFile = Files.createTempFile("backup_reservation", ".txt");
        Files.copy(reservationPath, backupReservationFile, StandardCopyOption.REPLACE_EXISTING);

        // 테스트용 예약 데이터 작성 (5월 3주차 금요일 09:00~09:50 예약 정보 포함)
        String testData = "2025-05-16,09:00~09:50,911,사용자A,예약"; // 2025-05-16은 5월 3주차 금요일
        Files.write(reservationPath, List.of(testData));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // 테스트 후 원본 예약 파일 복원
        Files.copy(backupReservationFile, reservationPath, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(backupReservationFile);
    }

    /**
     * generateWeeklySchedule() 메서드 테스트
     * - 2025년 5월 3주차 (5/16~5/22), 911호 시간표를 불러왔을 때
     * - 금요일 09:00~09:50 슬롯에 "예약" 상태가 포함되어 있어야 함
     */
    @Test
    public void testGenerateWeeklySchedule_WithReservation() {
        int month = 5;
        int week = 3;
        String roomNumber = "911";

        TimeTableModel model = new TimeTableModel();
        Map<String, List<String>> schedule = model.generateWeeklySchedule(month, week, roomNumber);

        assertNotNull(schedule);

        // 금요일("금") 첫 번째 시간 슬롯(09:00~09:50) 상태 확인
        String status = schedule.get("금").get(0);
        System.out.println("금요일 09:00~09:50 상태: " + status);

        // 상태에 예약 관련 단어가 포함되어야 테스트 성공
        assertTrue(status.contains("예약"), "금요일 09:00~09:50에 예약이 있어야 합니다.");
    }
}
