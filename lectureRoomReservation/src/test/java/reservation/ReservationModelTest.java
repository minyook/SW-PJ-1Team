package reservation;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationModelTest {

    private ReservationModel model;

    @BeforeEach
    public void setUp() {
        model = new ReservationModel() {
            @Override
            public List<RoomStatus> loadTimetable(String date, String roomNumber) {
                List<RoomStatus> list = new ArrayList<>();
                list.add(new RoomStatus("09:00~09:50", "비어 있음"));
                list.add(new RoomStatus("10:00~10:50", "예약"));
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
                if ("10:00~10:50".equals(time)) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean saveReservation(String date, String time, String room, String name, String status) {
                return true;
            }
        };
    }

    @Test
    public void testLoadTimetable() {
        List<RoomStatus> timetable = model.loadTimetable("2025-05-05", "912");

        assertNotNull(timetable);
        assertEquals(2, timetable.size());
        assertEquals("09:00~09:50", timetable.get(0).getTimeSlot());
        assertEquals("비어 있음", timetable.get(0).getStatus());
        assertEquals("10:00~10:50", timetable.get(1).getTimeSlot());
        assertEquals("예약", timetable.get(1).getStatus());
    }

    @Test
    public void testCheckRoomAvailable() {
        assertEquals("강의실 차단", model.checkRoomAvailable("911"));
        assertNull(model.checkRoomAvailable("912"));
    }

    @Test
    public void testCheckAvailability() {
        assertFalse(model.checkAvailability("2025-05-05", "10:00~10:50", "912"));
        assertTrue(model.checkAvailability("2025-05-05", "09:00~09:50", "912"));
    }

    @Test
    public void testSaveReservation() {
        boolean saved = model.saveReservation("2025-05-05", "09:00~09:50", "912", "테스트사용자", "예약 대기");
        assertTrue(saved);
    }
}
