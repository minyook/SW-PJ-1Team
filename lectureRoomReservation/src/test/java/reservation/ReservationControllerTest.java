package reservation;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationControllerTest {

    private ReservationController controller;

    @BeforeEach
    public void setUp() {
        ReservationModel testModel = new ReservationModel() {
            @Override
            public List<RoomStatus> loadTimetable(String date, String roomNumber) {
                List<RoomStatus> list = new ArrayList<>();
                list.add(new RoomStatus("09:00~09:50", "비어 있음"));
                list.add(new RoomStatus("10:00~10:50", "비어 있음"));
                return list;
            }

            @Override
            public String checkRoomAvailable(String roomNumber) {
                if ("911".equals(roomNumber)) {
                    return "강의실 차단";
                }
                return null;
            }

            @Override
            public boolean checkAvailability(String date, String time, String room) {
                return true;
            }

            @Override
            public boolean saveReservation(String date, String time, String room, String name, String status) {
                return true;
            }
        };

        controller = new ReservationController(testModel);
        controller.setShowDialog(false);
    }

    @Test
    public void testLoadTimetable() {
        List<RoomStatus> list = controller.loadTimetable("2025", "05", "05", "912");
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("09:00~09:50", list.get(0).getTimeSlot());
        assertEquals("비어 있음", list.get(0).getStatus());
    }

    @Test
    public void testProcessReservationRequestSuccess() {
        ReservationResult result = controller.processReservationRequest("2025-05-05", "10:00~10:50", "912", "테스트사용자");
        assertEquals(ReservationResult.SUCCESS, result);
    }

    @Test
    public void testProcessReservationRequestRoomBlocked() {
        ReservationResult result = controller.processReservationRequest("2025-05-05", "09:00~09:50", "911", "테스트사용자");
        assertEquals(ReservationResult.ROOM_BLOCKED, result);
    }

    @Test
    public void testProcessReservationRequestNoTimeSelected() {
        ReservationResult result = controller.processReservationRequest("2025-05-05", "", "912", "테스트사용자");
        assertEquals(ReservationResult.NOT_SELECTED, result);
    }
}
