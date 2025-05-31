package controller;

import common.Message;
import common.RequestType;
import common.Reservation;
import controller.MyReservationController;
import model.ReservationModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyReservationControllerTest {

    @TempDir Path tempDir;

    private Path dataFile;
    private MyReservationController controller;
    private StubReservationModel stubModel;

    static class StubReservationModel extends ReservationModel {
        List<Reservation> getByUserReturn = List.of();

        public StubReservationModel(Path file) throws IOException {
            super(file.toString());
        }

        @Override public List<Reservation> getByUser(String user) { return getByUserReturn; }
    }

    @BeforeEach
    void setUp() throws IOException {
        dataFile = tempDir.resolve("reservations.txt");
        Files.createFile(dataFile);  // 내용은 stub이 override

        stubModel  = new StubReservationModel(dataFile);
        controller = new MyReservationController(stubModel);
    }

    @Test
    void testGetByUserDelegates() {
        Reservation r1 = new Reservation("R1","2025-06-01","10:00~10:50","901","alice","OK");
        stubModel.getByUserReturn = List.of(r1);

        Message req = new Message();
        req.setType(RequestType.LIST);          // Assuming LIST means "my reservations"
        req.setPayload("alice");

        Message res = controller.handle(req);
        assertNull(res.getError());
        assertEquals(1, res.getList().size());
        System.err.println("MY LIST → " + res.getList());
    }
}
