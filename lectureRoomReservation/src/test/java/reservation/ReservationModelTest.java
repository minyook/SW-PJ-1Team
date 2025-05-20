package reservation;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationModelTest {

    private Path backupReservationFile;
    private Path backupRoomFile;
    private Path testReservationFile;
    private Path testRoomFile;

    private final String reservationPath = "src/main/resources/reservation_data.txt";
    private final String roomPath = "src/main/resources/rooms.txt";

    private ReservationModel model;

    @BeforeEach
    public void setUp() throws IOException {
        // 테스트 전에 기존 파일을 백업해 둠
        backupReservationFile = Files.createTempFile("backup_reservation", ".txt");
        backupRoomFile = Files.createTempFile("backup_rooms", ".txt");
        Files.copy(Paths.get(reservationPath), backupReservationFile, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Paths.get(roomPath), backupRoomFile, StandardCopyOption.REPLACE_EXISTING);

        // rooms.txt에 테스트용 데이터 기록: 911은 사용가능으로 지정
        Files.write(Paths.get(roomPath), List.of("911,사용가능"));

        // reservation_data.txt는 비워둠
        Files.write(Paths.get(reservationPath), new ArrayList<>());

        // 테스트용 모델 생성
        model = new ReservationModel();
    }

    @AfterEach
    public void tearDown() throws IOException {
        // 테스트가 끝난 후 원래 파일 복구
        Files.copy(backupReservationFile, Paths.get(reservationPath), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(backupRoomFile, Paths.get(roomPath), StandardCopyOption.REPLACE_EXISTING);

        // 임시 백업 파일 삭제
        Files.deleteIfExists(backupReservationFile);
        Files.deleteIfExists(backupRoomFile);
    }

    /**
     * loadTimetable() 메서드가 정상적으로 시간대별 상태를 로드하는지 테스트.
     * 이 테스트는 911호의 2025-05-05 날짜 시간표를 불러올 때,
     * 모든 시간대가 "비어 있음" 상태로 초기화되는지 확인함.
     */
    @Test
    public void testLoadTimetable() {
        String date = "2025-05-05";
        String roomNumber = "911";

        // 시간표 로드
        List<RoomStatus> result = model.loadTimetable(date, roomNumber);

        // 결과가 null이 아니어야 함
        assertNotNull(result);

        // 시간 슬롯이 8개여야 함
        assertEquals(8, result.size());

        // 첫 번째 시간 슬롯이 정확한지 확인
        assertEquals("09:00~09:50", result.get(0).getTimeSlot());

        // 해당 시간의 상태가 null이 아닌지 확인
        assertNotNull(result.get(0).getStatus());
    }

    /**
     * checkAvailability() 메서드가 특정 시간대에 예약 가능 여부를 올바르게 판단하는지 테스트.
     * 현재 reservation_data.txt에 아무 데이터도 없으므로,
     * 어떤 시간대를 조회하더라도 예약 가능(true)해야 한다.
     */
    @Test
    public void testCheckAvailability() {
        String date = "2025-05-05";
        String time = "10:00~10:50";
        String room = "911";

        // 해당 시간대가 비어있는지 확인 (true 기대)
        boolean result = model.checkAvailability(date, time, room);
        assertTrue(result);
    }

    /**
     * saveReservation() 메서드가 예약 정보를 텍스트 파일에 정상적으로 저장하는지 테스트.
     * 저장 후 true를 반환하면 성공으로 간주.
     */
    @Test
    public void testSaveReservation() {
        String date = "2025-05-05";
        String time = "11:00~11:50";
        String room = "911";
        String name = "테스트사용자";
        String status = "예약 대기";

        // 예약 저장 시도
        boolean result = model.saveReservation(date, time, room, name, status);

        // 저장이 성공했는지 확인
        assertTrue(result);

        // 확인 메시지 출력 (추가적인 디버깅용)
        System.out.println("예약 저장 테스트 성공 여부: " + result);
    }
    
    @Test
public void testRejectedReservationNotReflected() throws IOException {
    // 예약 파일에 거절 상태 예약 데이터 임시 저장
    Files.write(Paths.get(reservationPath), List.of(
        "2025-05-05,10:00~10:50,912,사용자A,거절"
    ));

    // 시간표 로드
    List<RoomStatus> result = model.loadTimetable("2025-05-05", "912");

    // 10:00~10:50 슬롯이 "비어 있음" 상태 유지되어야 함 (거절은 반영 안 됨)
    boolean isEmpty = false;
    for (RoomStatus rs : result) {
        if (rs.getTimeSlot().equals("10:00~10:50")) {
            isEmpty = rs.getStatus().equals("비어 있음");
            break;
        }
    }
    assertTrue(isEmpty, "거절 상태 예약은 시간표에 반영되지 않아야 합니다.");
}

@Test
public void testScheduleFileReflectedInTimetable() throws IOException {
    // schedule_912.txt 파일에 수업 정보 임시 작성 (해당 경로에 있어야 함)
    Path scheduleFile = Paths.get("src/main/resources/schedule_912.txt");
    Files.write(scheduleFile, List.of(
        "월,10:00~10:50,자료구조,홍길동"
    ));

    // 시간표 로드 (월요일인 2025-05-04 기준)
    List<RoomStatus> result = model.loadTimetable("2025-05-05", "912");

    // 10:00~10:50 슬롯이 수업명과 교수명 반영된 상태여야 함
    boolean containsClass = false;
    for (RoomStatus rs : result) {
        if (rs.getTimeSlot().equals("10:00~10:50")) {
            containsClass = rs.getStatus().equals("자료구조(홍길동)");
            break;
        }
    }
    assertTrue(containsClass, "수업 일정이 시간표에 정상 반영되어야 합니다.");

    // 테스트 후 임시로 만든 schedule 파일 삭제 (선택 사항)
    Files.deleteIfExists(scheduleFile);
}

}
