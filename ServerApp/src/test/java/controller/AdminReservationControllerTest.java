package controller;

import common.Message;
import common.RequestType;
import common.Reservation;
import controller.AdminReservationController;
import model.AdminReservationModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminReservationControllerTest {

    @TempDir Path tempDir;

    private Path dataFile;
    private AdminReservationController controller;
    private StubAdminReservationModel stubModel;

    static class StubAdminReservationModel extends AdminReservationModel {
        List<Reservation> listAllReturn = List.of();
        int updatedIndex = -1;
        String updatedStatus;

        public StubAdminReservationModel(Path file) throws IOException {
            super(file.toString());
        }

        @Override public List<Reservation> listAll()                     { return listAllReturn; }
        @Override public void updateStatus(int idx, String status)       { this.updatedIndex = idx; this.updatedStatus = status; }
    }

    @BeforeEach
    void setUp() throws IOException {
        dataFile = tempDir.resolve("reservations.txt");
        Files.write(dataFile, List.of(
            "R1,2025-06-01,10:00~10:50,901,alice,PENDING",
            "R2,2025-06-02,11:00~11:50,902,bob,PENDING"
        ), StandardOpenOption.CREATE);

        stubModel  = new StubAdminReservationModel(dataFile);
        controller = new AdminReservationController(stubModel);
    }

    @Test
    void testListDelegates() {
        Reservation a = new Reservation("R1","2025-06-01","10:00~10:50","901","alice","PENDING");
        Reservation b = new Reservation("R2","2025-06-02","11:00~11:50","902","bob","PENDING");
        stubModel.listAllReturn = Arrays.asList(a,b);

        Message req = new Message(); req.setType(RequestType.LIST);
        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(2, res.getList().size());
        System.err.println("ADMIN LIST → " + res.getList());
    }

    @Test
    void testUpdateDelegates() {
        Message req = new Message();
        req.setType(RequestType.UPDATE);
        req.setIndex(0);
        req.setPayload("APPROVED");

        Message res = controller.handle(req);
        assertNull(res.getError());
        assertEquals(0, stubModel.updatedIndex);
        assertEquals("APPROVED", stubModel.updatedStatus);
        System.err.println("UPDATED → idx=" + stubModel.updatedIndex + ", st=" + stubModel.updatedStatus);
    }
}
