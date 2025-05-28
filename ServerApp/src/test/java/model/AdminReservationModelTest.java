// src/test/java/model/AdminReservationModelTest.java
package model;

import common.Reservation;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminReservationModelTest {

    private static final String TEST_FILE = "src/test/resources/reservation_data.txt";
    private AdminReservationModel model;

    @BeforeEach
    void setUp() throws IOException {
        Path path = Paths.get(TEST_FILE);
        // resources 폴더에 테스트 파일 생성
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, List.of(
            "R001,2025-06-01,10:00~10:50,901,홍길동,예약대기",
            "R002,2025-06-02,11:00~11:50,902,김철수,예약대기"
        ), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 커스텀 경로로 모델 생성
        model = new AdminReservationModel(TEST_FILE);
    }

    @Test
    void testListAllLoadsAllReservations() throws IOException {
        List<Reservation> list = model.listAll();
        assertEquals(2, list.size(), "초기 예약 건수는 2건이어야 합니다");

        Reservation first = list.get(0);
        assertEquals("R001", first.getReservationId());
        assertEquals("2025-06-01", first.getDate());
        assertEquals("10:00~10:50", first.getTime());
        assertEquals("901", first.getRoomNumber());
        assertEquals("홍길동", first.getUserName());
        assertEquals("예약대기", first.getStatus());
    }

    @Test
    void testUpdateStatusPersistsChange() throws IOException {
        model.updateStatus(0, "예약완료");

        // 모델 레벨 확인
        List<Reservation> updated = model.listAll();
        assertEquals("예약완료", updated.get(0).getStatus());

        // 파일 레벨 확인
        List<String> lines = Files.readAllLines(Paths.get(TEST_FILE));
        String[] tokens = lines.get(0).split(",");
        assertEquals("예약완료", tokens[5]);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }
}
