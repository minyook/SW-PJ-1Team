/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package reservation;

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
public class ReservationModelTest {
    private static ReservationModel rm1;  // 전체 테스트에서 공용
    private ReservationModel rm2;         // 각 테스트마다 새로 생성
    public ReservationModelTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        System.out.println("setUpClass()");
        rm1= new ReservationModel();
    }
    
    @AfterAll
    public static void tearDownClass() {
        System.out.println("tearDownClass()");
    }
    
    @BeforeEach
    public void setUp() {
        System.out.println("setUp()");
        rm2 = new ReservationModel(); // 각 테스트 전에 새로 생성
    }
    
    @AfterEach
    public void tearDown() {
        System.out.println("tearDown()");
    }

    /**
     * Test of loadTimetable method, of class ReservationModel.
     */
    @Test
    public void testLoadTimetable() {
        System.out.println("loadTimetable");
        String date = "2025-05-05";
        String roomNumber = "911";
        ReservationModel instance = new ReservationModel();
        //List<RoomStatus> expResult = null;
        List<RoomStatus> result = instance.loadTimetable(date, roomNumber);
        // 시간대 개수 확인
        assertNotNull(result);
        assertEquals(8, result.size()); // 예: 시간 슬롯이 8개라고 가정할 때
        
        // 첫 번째 시간대가 비어 있음인지 확인
        assertEquals("09:00~09:50", result.get(0).getTimeSlot());
        assertNotNull(result.get(0).getStatus());

    }

    /**
     * Test of checkAvailability method, of class ReservationModel.
     */
    @Test
    public void testCheckAvailability() {
        System.out.println("checkAvailability");
        String date = "2025-05-05";
        String time = "10:00~10:50";
        String room = "911";
        ReservationModel instance = new ReservationModel();
        //boolean expResult = false;
         // 시간표에 해당 시간이 비어 있다고 가정
        boolean result = instance.checkAvailability(date, time, room);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        // 예상 결과는 true (예약이나 수업 없음)
        assertTrue(result);
    }

    /**
     * Test of saveReservation method, of class ReservationModel.
     */
    @Test
    public void testSaveReservation() {
        System.out.println("saveReservation");
        String date = "2025-05-05";
        String time = "11:00~11:50";
        String room = "911";
        String name = "테스트사용자";
        String status = "예약 대기";
        ReservationModel instance = new ReservationModel();
        //boolean expResult = false;
        boolean result = instance.saveReservation(date, time, room, name, status);
        assertTrue(result); // 저장이 성공했는지 확인
        // 확인 메시지
        System.out.println("예약 저장 테스트 성공 여부: " + result);
    }
    
}
