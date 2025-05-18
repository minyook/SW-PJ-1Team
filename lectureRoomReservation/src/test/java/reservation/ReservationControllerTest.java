/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package reservation;
import reservation.ReservationResult; 
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rbcks
 */
public class ReservationControllerTest {
    
    public ReservationControllerTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of loadTimetable method, of class ReservationController.
     */
    @Test
    public void testLoadTimetable() {
        System.out.println("loadTimetable");
        String year = "2025";
        String month = "05";
        String day = "05";
        String roomNumber = "911";
        ReservationController instance = new ReservationController();
        List<RoomStatus> result = instance.loadTimetable(year, month, day, roomNumber);
        
        assertNotNull(result);
        assertEquals(9, result.size()); // 시간대 8개라고 가정
        assertEquals("09:00~09:50", result.get(0).getTimeSlot());
    }

    /**
     * Test of processReservationRequest method, of class ReservationController.
     */
    @Test
    public void testProcessReservationRequest() {
        System.out.println("processReservationRequest");
        String date = "2025-05-05";
        String time = "16:00~16:50";
        String room = "912";
        String name = "테스트사용자";
        ReservationController instance = new ReservationController();
        ReservationResult result = instance.processReservationRequest(date, time, room, name);
        
        assertNotNull(result);
        assertEquals(ReservationResult.SUCCESS, result); // ← 예약 가능하다고 가정한 상태
    }

    @Test
public void testRoomBlockedReservation() {
    System.out.println("testRoomBlockedReservation");

    // rooms.txt에 911,사용불가능 이라고 등록되어 있어야 합니다
    String date = "2025-05-10";
    String time = "09:00~09:50";
    String room = "911";  // 현재 사용불가능 상태라고 가정
    String name = "테스트사용자";

    ReservationController controller = new ReservationController();
    ReservationResult result = controller.processReservationRequest(date, time, room, name);

    assertEquals(ReservationResult.ROOM_BLOCKED, result);  // 강의실 차단이어야함.
}


    
}
