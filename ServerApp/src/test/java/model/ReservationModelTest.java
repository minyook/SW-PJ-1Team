package model;

import common.Reservation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationModelTest {

    @TempDir
    Path tempDir;              // 테스트마다 새로 생성되는 임시 디렉토리

    private Path testFile;
    private ReservationModel model;

    @BeforeEach
    void setUp() throws IOException {
        // 1) 임시 디렉토리 안에 테스트용 파일 생성
        testFile = tempDir.resolve("reservation_data.txt");
        Files.write(testFile, List.of(
            "R001,2025-06-01,10:00~10:50,901,홍길동,예약",
            "R002,2025-06-02,11:00~11:50,902,김철수,예약",
            "R003,2025-06-03,12:00~12:50,903,홍길동,거절"
        ));

        // 2) 파일 시스템 경로를 직접 넘겨서 모델 초기화
        model = new ReservationModel(testFile.toString());
    }

    @Test
    void testListAllReturnsAllReservations() {
        List<Reservation> list = model.listAll();
        assertNotNull(list);
        assertEquals(3, list.size());

        // 값이 제대로 로드됐는지 눈으로도 확인
        list.forEach(System.out::println);
    }

    @Test
    void testGetByUserReturnsCorrectReservations() {
        List<Reservation> list = model.getByUser("홍길동");
        assertNotNull(list);
        assertEquals(2, list.size());
        list.forEach(System.out::println);
    }

    @Test
    void testUnsupportedOperationsThrow() {
        Reservation dummy = new Reservation("X","2025-01-01","09:00~10:00","000","테스트","예약");
        assertThrows(UnsupportedOperationException.class, () -> model.create(dummy));
        assertThrows(UnsupportedOperationException.class, () -> model.update(0, dummy));
        assertThrows(UnsupportedOperationException.class, () -> model.delete(0));
    }
}
