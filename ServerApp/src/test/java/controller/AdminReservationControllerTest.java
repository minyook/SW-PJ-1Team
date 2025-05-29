package controller;

import common.Message;
import common.RequestType;
import common.Reservation;
import controller.AdminReservationController;
import model.AdminReservationModel;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AdminReservationControllerTest {

    static class StubAdminReservationModel extends AdminReservationModel {
        List<Reservation> listAllReturn;
        int updatedIndex = -1;
        String updatedStatus = null;

        public StubAdminReservationModel() throws IOException {
            super("src/test/resources/unused_res.txt");
        }

        @Override
        public List<Reservation> listAll() {
            return listAllReturn;
        }

        @Override
        public void updateStatus(int index, String status) {
            this.updatedIndex = index;
            this.updatedStatus = status;
        }
    }

    private StubAdminReservationModel stubModel;
    private AdminReservationController controller;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 더미 파일 생성 (파일 IO를 피해가기 위함)
        Path p = Paths.get("src/test/resources/unused_res.txt");
        Files.createDirectories(p.getParent());
        Files.write(p, List.of(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        stubModel  = new StubAdminReservationModel();
        controller = new AdminReservationController(stubModel);
    }

    @Test
    void listDelegatesToModel() {
        Reservation r1 = new Reservation("R1","2025-06-01","10:00~10:50","901","user1","대기");
        Reservation r2 = new Reservation("R2","2025-06-02","11:00~11:50","902","user2","대기");
        stubModel.listAllReturn = Arrays.asList(r1, r2);

        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = controller.handle(req);

        assertNull(res.getError());
        List<?> out = res.getList();
        assertEquals(2, out.size());
        assertSame(r1, out.get(0));
        assertSame(r2, out.get(1));
    }

    @Test
    void updateDelegatesToModel() {
        Message req = new Message();
        req.setType(RequestType.UPDATE);
        req.setIndex(3);
        req.setPayload("예약");  // 새 상태

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(3, stubModel.updatedIndex);
        assertEquals("예약", stubModel.updatedStatus);
    }

    @Test
    void unsupportedTypeReturnsError() {
        Message req = new Message();
        req.setType(RequestType.LOGIN); // 지원되지 않는 타입

        Message res = controller.handle(req);

        assertEquals("지원하지 않는 관리자 예약 요청입니다.", res.getError());
    }

    @Test
    void exceptionFromModelIsCaptured() throws IOException {
        // listAll() 에서 예외를 던지도록 override
        AdminReservationController exCtrl = new AdminReservationController(
            new AdminReservationModel("src/test/resources/unused_res.txt") {
                @Override public List<Reservation> listAll() { throw new RuntimeException("fail"); }
            }
        );

        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = exCtrl.handle(req);

        assertEquals("fail", res.getError());
    }
}
