package reservation;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationControllerTest {

    // 테스트 대상인 ReservationController 객체
    private ReservationController controller;

    /**
     * 각 테스트 메서드 실행 전에 호출됨.
     * 실제 파일 I/O를 수행하지 않고, 테스트에 필요한 데이터만 반환하도록
     * ReservationModel을 익명 서브클래스로 오버라이드하여 테스트용 모형 모델을 만듦.
     */
    @BeforeEach
    public void setUp() {
        ReservationModel testModel = new ReservationModel() {
            // loadTimetable 메서드 오버라이드:
            // 실제 파일 읽지 않고, 두 개 시간대 데이터를 직접 생성해서 반환
            @Override
            public List<RoomStatus> loadTimetable(String date, String roomNumber) {
                List<RoomStatus> list = new ArrayList<>();
                list.add(new RoomStatus("09:00~09:50", "비어 있음")); // 첫 시간 슬롯, 예약 안됨 상태
                list.add(new RoomStatus("10:00~10:50", "비어 있음")); // 두 번째 시간 슬롯, 예약 안됨 상태
                return list;
            }

            // checkRoomAvailable 오버라이드:
            // 911호는 강의실 차단 상태로 반환, 그 외는 사용 가능(null 반환)
            @Override
            public String checkRoomAvailable(String roomNumber) {
                if ("911".equals(roomNumber)) {
                    return "강의실 차단";
                }
                return null;
            }

            // checkAvailability 오버라이드:
            // 테스트 편의를 위해 모든 시간/강의실 예약 가능(true)로 반환
            @Override
            public boolean checkAvailability(String date, String time, String room) {
                return true;
            }

            // saveReservation 오버라이드:
            // 파일에 실제 저장하지 않고 항상 성공(true)로 처리
            @Override
            public boolean saveReservation(String date, String time, String room, String name, String status) {
                return true;
            }
        };

        // 위에서 만든 테스트용 모델을 주입해 ReservationController 인스턴스 생성
        controller = new ReservationController(testModel);

        // 테스트 중 팝업이 뜨지 않도록 설정
        controller.setShowDialog(false);
    }

    /**
     * loadTimetable 메서드 정상 동작 테스트
     *  - 지정한 날짜와 강의실에 대해 두 개 시간 슬롯이 반환되는지 확인
     *  - 첫 번째 슬롯이 "09:00~09:50"이고 상태가 "비어 있음"인지 확인
     */
    @Test
    public void testLoadTimetable() {
        List<RoomStatus> list = controller.loadTimetable("2025", "05", "05", "912");
        assertNotNull(list, "시간표 리스트가 null이 아니어야 합니다.");
        assertEquals(2, list.size(), "시간 슬롯 개수가 2개여야 합니다.");
        assertEquals("09:00~09:50", list.get(0).getTimeSlot(), "첫 시간 슬롯은 '09:00~09:50'이어야 합니다.");
        assertEquals("비어 있음", list.get(0).getStatus(), "첫 시간 슬롯 상태는 '비어 있음'이어야 합니다.");
    }

    /**
     * 예약 요청 성공 테스트
     *  - 유효한 시간과 사용 가능한 강의실에 대해 예약 성공 결과 반환 확인
     */
    @Test
    public void testProcessReservationRequestSuccess() {
        ReservationResult result = controller.processReservationRequest("2025-05-05", "10:00~10:50", "912", "테스트사용자");
        assertEquals(ReservationResult.SUCCESS, result, "예약 요청은 성공이어야 합니다.");
    }

    /**
     * 사용 불가능 강의실 예약 시도 테스트
     *  - 911호는 강의실 차단 상태이므로 예약 차단 결과 반환 확인
     */
    @Test
    public void testProcessReservationRequestRoomBlocked() {
        ReservationResult result = controller.processReservationRequest("2025-05-05", "09:00~09:50", "911", "테스트사용자");
        assertEquals(ReservationResult.ROOM_BLOCKED, result, "차단된 강의실 예약 요청은 ROOM_BLOCKED 반환해야 합니다.");
    }

    /**
     * 예약 시간 미선택 시 처리 테스트
     *  - 빈 문자열("")을 시간으로 넘길 경우 NOT_SELECTED 결과 반환 확인
     */
    @Test
    public void testProcessReservationRequestNoTimeSelected() {
        ReservationResult result = controller.processReservationRequest("2025-05-05", "", "912", "테스트사용자");
        assertEquals(ReservationResult.NOT_SELECTED, result, "시간 미선택 시 NOT_SELECTED 반환해야 합니다.");
    }
}
