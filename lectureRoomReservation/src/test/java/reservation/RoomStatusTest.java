/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package reservation;

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
public class RoomStatusTest {
    private RoomStatus instance;
    public RoomStatusTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        instance = new RoomStatus("10:00~10:50", "비어 있음");
    }
    
    @AfterEach
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of getTimeSlot method, of class RoomStatus.
     */
    @Test
    public void testGetTimeSlot() {
        System.out.println("getTimeSlot");
        String result = instance.getTimeSlot();
        assertEquals("10:00~10:50", result);
    }

    /**
     * Test of getStatus method, of class RoomStatus.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        String result = instance.getStatus();
        assertEquals("비어 있음", result);
    }

    /**
     * Test of setStatus method, of class RoomStatus.
     */
    @Test
    public void testSetStatus() {
        System.out.println("setStatus");
        instance.setStatus("예약");
        assertEquals("예약", instance.getStatus());
    }
}
