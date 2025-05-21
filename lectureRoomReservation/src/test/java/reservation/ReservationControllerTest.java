package reservation;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationControllerTest {

    // 기존 데이터를 보존하기 위한 백업 파일 경로
    private Path backupReservationFile;
    private Path backupRoomFile;

    // 테스트에 사용할 실제 파일 경로 (테스트 중 임시 데이터로 덮어씀)
    private Path testReservationFile;
    private Path testRoomFile;

    // 실제 파일 경로 상수
    private final String reservationPath = "src/main/resources/reservation_data.txt";
    private final String roomPath = "src/main/resources/rooms.txt";

    // 테스트 대상 객체
    private ReservationController controller;

    /**
     * 각 테스트 실행 전 실행되는 메서드
     * 기존 파일을 백업하고, 테스트용 데이터로 파일 내용을 교체
     */
    @BeforeEach
    public void setUp() throws IOException {
        // 원본 reservation_data.txt와 rooms.txt 파일을 백업 (임시 파일 생성)
        backupReservationFile = Files.createTempFile("backup_reservation", ".txt");
        backupRoomFile = Files.createTempFile("backup_rooms", ".txt");

        // 원본 파일 내용을 백업 파일로 복사
        Files.copy(Paths.get(reservationPath), backupReservationFile, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Paths.get(roomPath), backupRoomFile, StandardCopyOption.REPLACE_EXISTING);

        // 테스트용 파일로 다시 접근 (실제 파일 경로에 덮어씀)
        testReservationFile = Paths.get(reservationPath);
        testRoomFile = Paths.get(roomPath);

        // 테스트용 rooms.txt 내용 작성 (912는 사용가능, 911은 사용불가능)
        Files.write(testRoomFile, List.of("912,사용가능", "911,사용불가능"));

        // 테스트용 reservation_data.txt 초기화 (빈 상태로 시작)
        Files.write(testReservationFile, new ArrayList<>());

        // 테스트용 컨트롤러 인스턴스 생성
        controller = new ReservationController();

        // 팝업 방지 설정 (테스트 환경에서는 JOptionPane 사용 안 함)
        controller.setShowDialog(false);
    }

    /**
     * 각 테스트 실행 후 실행되는 메서드
     * 테스트 중 덮어쓴 파일을 원래 상태로 복구
     */
    @AfterEach
    public void tearDown() throws IOException {
        // 백업한 파일 내용을 원래 위치로 복원
        Files.copy(backupReservationFile, Paths.get(reservationPath), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(backupRoomFile, Paths.get(roomPath), StandardCopyOption.REPLACE_EXISTING);

        // 임시 백업 파일 삭제
        Files.deleteIfExists(backupReservationFile);
        Files.deleteIfExists(backupRoomFile);
    }

    /**
     * 테스트 1: 시간표 로드 테스트
     * - 912호의 시간표를 불러왔을 때 9개의 시간 슬롯이 정상적으로 생성되는지 확인
     * - 각 시간은 "비어 있음" 상태여야 함 (초기 예약 없음)
     */
    @Test
    public void testLoadTimetable() {
        List<RoomStatus> result = controller.loadTimetable("2025", "05", "05", "912");

        // 리스트가 null이 아니고
        assertNotNull(result);

        // 시간대가 총 8개인지 확인
        assertEquals(8, result.size());

        // 첫 시간 슬롯이 "09:00~09:50"인지 확인
        assertEquals("09:00~09:50", result.get(0).getTimeSlot());
    }

    /**
     * 테스트 2: 예약 성공 테스트
     * - 예약 가능한 시간대에 예약을 시도하면 SUCCESS가 반환되어야 함
     */
    @Test
    public void testProcessReservationRequest() {
        ReservationResult result = controller.processReservationRequest(
            "2025-05-05", "16:00~16:50", "912", "테스트사용자"
        );

        // 예약 성공 결과가 반환되어야 함
        assertEquals(ReservationResult.SUCCESS, result);
    }

    /**
     * 테스트 3: 사용 불가능한 강의실에 예약 시도
     * - rooms.txt에서 911호는 사용불가능으로 설정되어 있음
     * - 이 경우 ROOM_BLOCKED가 반환되어야 함
     */
    @Test
    public void testRoomBlockedReservation() {
        ReservationResult result = controller.processReservationRequest(
            "2025-05-10", "09:00~09:50", "911", "테스트사용자"
        );

        // 강의실 차단 결과가 반환되어야 함
        assertEquals(ReservationResult.ROOM_BLOCKED, result);
    }

    /**
     * 테스트 4: 예약 중복 테스트
     * - 동일한 시간, 동일한 강의실에 두 번 예약을 시도했을 때
     * - 첫 번째는 SUCCESS, 두 번째는 TIME_OCCUPIED가 되어야 함
     */
    @Test
    public void testDuplicateReservation() {
        // 첫 번째 사용자 예약 (성공)
        ReservationResult first = controller.processReservationRequest(
            "2025-05-06", "13:00~13:50", "912", "사용자A"
        );
        assertEquals(ReservationResult.SUCCESS, first);

        // 두 번째 사용자 같은 시간에 예약 시도 (실패)
        ReservationResult second = controller.processReservationRequest(
            "2025-05-06", "13:00~13:50", "912", "사용자B"
        );
        assertEquals(ReservationResult.TIME_OCCUPIED, second);
    }

    /**
     * 테스트 5: 시간 선택 안 하고 예약 시도
     * - 시간 값이 빈 문자열일 경우 NOT_SELECTED가 반환되어야 함
     */
    @Test
    public void testNoTimeSelected() {
        ReservationResult result = controller.processReservationRequest(
            "2025-05-07", "", "912", "사용자C"
        );

        // 시간 미선택 경고 결과
        assertEquals(ReservationResult.NOT_SELECTED, result);
    }
}
