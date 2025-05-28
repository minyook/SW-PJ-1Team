package model;

import common.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationModelTest {

    private ReservationModel model;

    @BeforeEach
    void setUp() throws IOException {
        model = new ReservationModel("reservation_data.txt");
    }

    @Test
    void testListAllReturnsAllReservations() {
        List<Reservation> list = model.listAll();
        assertNotNull(list);
        assertEquals(3, list.size());
    }

    @Test
    void testGetByUserReturnsCorrectReservations() {
        List<Reservation> list = model.getByUser("홍길동");
        assertNotNull(list);
        assertEquals(2, list.size());
        for (Reservation r : list) {
            assertEquals("홍길동", r.getUserName());
        }
    }

    @Test
    void testUnsupportedCreateThrowsException() {
        Reservation dummy = new Reservation("R999", "2025-12-12", "09:00~10:00", "999", "테스트유저", "예약");
        assertThrows(UnsupportedOperationException.class, () -> model.create(dummy));
    }

    @Test
    void testUnsupportedUpdateThrowsException() {
        Reservation dummy = new Reservation("R999", "2025-12-12", "09:00~10:00", "999", "테스트유저", "예약");
        assertThrows(UnsupportedOperationException.class, () -> model.update(0, dummy));
    }

    @Test
    void testUnsupportedDeleteThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> model.delete(0));
    }
}
