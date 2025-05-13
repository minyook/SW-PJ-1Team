/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package management.model;

import management.model.ReservationModel;
import java.io.IOException;

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
public class ReservationModelTest {
    
    public ReservationModelTest() {
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
     * Test of getAll method, of class ReservationModel.
     */
    @Test
    public void testGetAll() throws IOException {
        System.out.println("getAll");
        ReservationModel instance = new ReservationModel();
        List<Reservation> expResult = null;
        List<Reservation> result = instance.getAll();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateStatus method, of class ReservationModel.
     */
    @Test
    public void testUpdateStatus() throws Exception {
        System.out.println("updateStatus");
        Reservation r = null;
        Reservation.Status newStatus = null;
        ReservationModel instance = new ReservationModel();
        instance.updateStatus(r, newStatus);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
