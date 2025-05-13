/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package management.model;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author limmi
 */
public class ScheduleModelTest {
    
    public ScheduleModelTest() {
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
     * Test of load method, of class ScheduleModel.
     */
    @Test
    public void testLoad() throws Exception {
        System.out.println("load");
        String roomId = "";
        ScheduleModel instance = new ScheduleModel();
        List<ScheduleEntry> expResult = null;
        List<ScheduleEntry> result = instance.load(roomId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveAppend method, of class ScheduleModel.
     */
    @Test
    public void testSaveAppend() throws Exception {
        System.out.println("saveAppend");
        String roomId = "";
        ScheduleEntry e = null;
        ScheduleModel instance = new ScheduleModel();
        instance.saveAppend(roomId, e);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
