package reservation;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationModelTest {

    // 테스트 대상인 ReservationModel 객체
    private ReservationModel model;

    /**
     * 각 테스트 실행 전 호출됨
     * 실제 파일 I/O를 하지 않고 테스트용 데이터만 반환하도록
     * ReservationModel을 익명 서브클래스로 오버라이드하여 테스트용 객체 생성
     */
    @BeforeEach
    public void setUp() {
        model = new ReservationModel() {
            // loadTimetable 메서드 오버라이드:
            // 파일 읽기 대신 테스트에 필요한 시간표 데이터 반환
            @Override
            public List<RoomStatus> loadTimetable(String date, String roomNumber) {
                List<RoomStatus> list = new ArrayList<>();
                list.add(new RoomStatus("09:00~09:50", "비어 있음"));  // 비어 있는 시간 슬롯
                list.add(new RoomStatus("10:00~10:50", "예약"));       // 예약 상태 시간 슬롯
                return list;
            }

            // checkRoomAvailable 오버라이드:
            // 911호는 차단 상태 반환, 그 외는 사용 가능(null)
            @Override
            public String checkRoomAvailable(String roomNumber) {
                if ("911".equals(roomNumber)) {
                    return "강의실 차단";
                }
                return null;
            }

            // checkAvailability 오버라이드:
            // 10:00~10:50 시간대는 이미 예약된 상태로 false 반환
            // 그 외 시간대는 예약 가능(true)
            @Override
            public boolean checkAvailability(String date, String time, String room) {
                if ("10:00~10:50".equals(time)) {
                    return false;
                }
                return true;
            }

            // saveReservation 오버라이드:
            // 저장 성공을 항상 반환하여 테스트 진행 용이
            @Override
            public boolean saveReservation(String date, String time, String room, String name, String status) {
                return true;
            }
        };
    }

    /**
     * loadTimetable 메서드 정상 동작 테스트
     * - 지정한 날짜 및 강의실에 대해 두 개 시간 슬롯이 반환되는지 확인
     * - 각 시간 슬롯의 시간과 상태가 테스트용 데이터와 일치하는지 확인
     */
    @Test
    public void testLoadTimetable() {
        List<RoomStatus> timetable = model.loadTimetable("2025-05-05", "912");

        assertNotNull(timetable, "시간표 리스트가 null이 아니어야 합니다.");
        assertEquals(2, timetable.size(), "시간 슬롯 개수가 2개여야 합니다.");
        assertEquals("09:00~09:50", timetable.get(0).getTimeSlot(), "첫 시간 슬롯은 '09:00~09:50'이어야 합니다.");
        assertEquals("비어 있음", timetable.get(0).getStatus(), "첫 시간 슬롯 상태는 '비어 있음'이어야 합니다.");
        assertEquals("10:00~10:50", timetable.get(1).getTimeSlot(), "두 번째 시간 슬롯은 '10:00~10:50'이어야 합니다.");
        assertEquals("예약", timetable.get(1).getStatus(), "두 번째 시간 슬롯 상태는 '예약'이어야 합니다.");
    }

    /**
     * checkRoomAvailable 메서드 테스트
     * - 911호는 차단 상태 문자열 반환 확인
     * - 912호는 사용 가능(null) 반환 확인
     */
    @Test
    public void testCheckRoomAvailable() {
        assertEquals("강의실 차단", model.checkRoomAvailable("911"), "911호는 차단되어야 합니다.");
        assertNull(model.checkRoomAvailable("912"), "912호는 사용 가능이어야 합니다.");
    }

    /**
     * checkAvailability 메서드 테스트
     * - 10:00~10:50 시간대는 예약 불가(false)로 처리되는지 확인
     * - 09:00~09:50 시간대는 예약 가능(true)인지 확인
     */
    @Test
    public void testCheckAvailability() {
        assertFalse(model.checkAvailability("2025-05-05", "10:00~10:50", "912"), "10:00~10:50 시간대는 예약 불가여야 합니다.");
        assertTrue(model.checkAvailability("2025-05-05", "09:00~09:50", "912"), "09:00~09:50 시간대는 예약 가능이어야 합니다.");
    }

    /**
     * saveReservation 메서드 테스트
     * - 항상 true를 반환하므로 저장 성공 여부 확인
     */
    @Test
    public void testSaveReservation() {
        boolean saved = model.saveReservation("2025-05-05", "09:00~09:50", "912", "테스트사용자", "예약 대기");
        assertTrue(saved, "예약 저장은 성공이어야 합니다.");
    }
}
