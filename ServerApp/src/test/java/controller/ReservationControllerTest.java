package controller;

import common.Message;
import common.RequestType;
import common.Reservation;
import controller.ReservationController;
import model.ReservationModel;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationControllerTest {

    static class StubReservationModel extends ReservationModel {
        List<Reservation> listAllReturn;
        int deletedIndex = -1;
        Reservation created;
        int updatedIndex = -1;
        Reservation updatedPayload;

        public StubReservationModel() throws IOException {
            super("does_not_matter.txt");
        }

        @Override
        public List<Reservation> listAll() {
            return listAllReturn;
        }

        @Override
        public void create(Reservation r) {
            this.created = r;
        }

        @Override
        public void delete(int index) {
            this.deletedIndex = index;
        }

        @Override
        public void update(int index, Reservation updated) {
            this.updatedIndex = index;
            this.updatedPayload = updated;
        }
    }

    private StubReservationModel stub;
    private ReservationController controller;

    @BeforeEach
    void setUp() throws IOException {
        stub = new StubReservationModel();
        controller = new ReservationController(stub);
    }

    @Test
    @DisplayName("전체 조회(List) 요청 시 모델의 리스트를 반환한다")
    void listAllDelegatesToModel() {
        Reservation r1 = new Reservation("R1","2025-06-01","10:00~10:50","901","user1","예약");
        Reservation r2 = new Reservation("R2","2025-06-02","11:00~11:50","902","user2","예약");
        stub.listAllReturn = Arrays.asList(r1, r2);

        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(2, res.getList().size());
        assertSame(r1, res.getList().get(0));
        assertSame(r2, res.getList().get(1));
    }

    @Test
    @DisplayName("예약 생성(Create) 요청 시 payload를 그대로 반환하고 모델에 전달된다")
    void createDelegatesAndReturnsPayload() {
        Reservation r = new Reservation("R3","2025-06-03","12:00~12:50","903","user3","예약");
        Message req = new Message();
        req.setType(RequestType.CREATE);
        req.setPayload(r);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertSame(r, res.getPayload());
        assertSame(r, stub.created);
    }

    @Test
    void deleteDelegates() {
        Message req = new Message();
        req.setType(RequestType.DELETE);
        req.setIndex(5);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(5, stub.deletedIndex);
    }

    @Test
    void updateDelegatesAndReturnsPayload() {
        Reservation r = new Reservation("R4","2025-06-04","13:00~13:50","904","user4","예약");
        Message req = new Message();
        req.setType(RequestType.UPDATE);
        req.setIndex(3);
        req.setPayload(r);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertSame(r, res.getPayload());
        assertEquals(3, stub.updatedIndex);
        assertSame(r, stub.updatedPayload);
    }

    @Test
    @DisplayName("지원되지 않는 타입 요청 시 에러 메시지를 반환한다")
    void unsupportedTypeGivesError() throws IOException {
        Message req = new Message();
        req.setType(RequestType.LOGIN);  // null 대신 LOGIN 사용

        Message res = controller.handle(req);

        assertEquals("지원하지 않는 예약 요청입니다.", res.getError());
    }

    @Test
    void modelThrowsExceptionIsCaptured() throws IOException {
        // override stub to throw
        ReservationController exController = new ReservationController(new ReservationModel() {
            @Override public List<Reservation> listAll() { throw new RuntimeException("fail"); }
        });
        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = exController.handle(req);

        assertEquals("fail", res.getError());
    }
}
