package model;

import common.Reservation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminReservationModelTest {

    @TempDir
    Path tempDir;            // 매 테스트마다 새 임시 디렉토리

    private Path testFile;
    private AdminReservationModel model;

    @BeforeEach
    void setUp() throws IOException {
        // 1) tempDir 아래에 테스트용 파일 생성
        testFile = tempDir.resolve("reservation_data.txt");
        Files.write(testFile, List.of(
            "R001,2025-06-01,10:00~10:50,901,홍길동,예약대기",
            "R002,2025-06-02,11:00~11:50,902,김철수,예약대기"
        ), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 2) 파일 경로를 넘겨서 모델 초기화
        model = new AdminReservationModel(testFile.toString());
    }

    @Test
    void testListAllLoadsAllReservations() throws IOException {
        List<Reservation> list = model.listAll();
        assertEquals(2, list.size(), "초기 예약 건수는 2건이어야 합니다");

        // ▶ System.err로 출력: 실제 로드된 내용을 확인
        System.err.println("=== 전체 관리자 예약 목록 ===");
        list.forEach(System.err::println);
        System.err.println("============================");

        // 필드별 검증
        Reservation first = list.get(0);
        assertEquals("R001",    first.getReservationId());
        assertEquals("2025-06-01", first.getDate());
        assertEquals("10:00~10:50", first.getTime());
        assertEquals("901",     first.getRoomNumber());
        assertEquals("홍길동",   first.getUserName());
        assertEquals("예약대기", first.getStatus());
    }

    @Test
    void testUpdateStatusPersistsChange() throws IOException {
        // 상태 변경
        model.updateStatus(0, "예약");

        // 모델 레벨 확인
        List<Reservation> list = model.listAll();
        assertEquals("예약", list.get(0).getStatus(), "업데이트 후 상태가 '예약'이어야 합니다");

        // ▶ System.err로 출력: 업데이트 결과 확인
        System.err.println("=== 상태 업데이트 후 목록 ===");
        list.forEach(System.err::println);
        System.err.println("============================");
    }
}
