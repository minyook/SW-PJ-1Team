
package timetable;

import java.util.List;
import java.util.Map;
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
public class TimeTableModelTest {
    
    public TimeTableModelTest() {
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
     * Test of generateWeeklySchedule method, of class TimeTableModel.
     */
    @Test
public void testGenerateWeeklySchedule_WithReservation() {
    int month = 5;
    int week = 3;
    String roomNumber = "911";

    TimeTableModel model = new TimeTableModel();
    Map<String, List<String>> schedule = model.generateWeeklySchedule(month, week, roomNumber);

    assertNotNull(schedule);

    // 금요일 09:00~09:50 = index 0
    String status = schedule.get("금").get(0);
    System.out.println("금요일 09:00~09:50: " + status);

    assertTrue(status.contains("예약"), "해당 시간에 예약이 있어야 합니다.");
}


    
}
