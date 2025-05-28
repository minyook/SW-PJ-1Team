package controller;

import common.Message;
import common.RequestType;
import common.Reservation;
import model.ReservationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MyReservationControllerTest {

    private MyReservationController controller;

    @BeforeEach
    void setUp() throws IOException {
        controller = new MyReservationController(new FakeReservationModel());
    }

    @Test
    void testHandleListRequest_Success() {
        Message request = new Message();
        request.setType(RequestType.LIST);
        request.setPayload("홍길동");

        Message response = controller.handle(request);
        List<?> list = response.getList();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("홍길동", ((Reservation) list.get(0)).getUserName());
    }

    @Test
    void testHandleUnsupportedRequest_Failure() {
        Message request = new Message();
        request.setType(RequestType.UPDATE); // 지원되지 않는 타입

        Message response = controller.handle(request);

        assertNotNull(response.getError());
        assertTrue(response.getError().contains("지원하지 않는 요청"));
    }

    // 🔹 테스트용 모델 클래스
    static class FakeReservationModel extends ReservationModel {
        private final List<Reservation> dummyData;

        public FakeReservationModel() throws IOException {
            super(); // 부모 생성자 호출, 필요 없으면 제거해도 됨
            dummyData = new ArrayList<>();
            dummyData.add(new Reservation(
                "R001", "2025-06-01", "10:00~11:00", "911", "홍길동", "예약 대기"
            ));
        }

        @Override
        public List<Reservation> getByUser(String username) {
            List<Reservation> result = new ArrayList<>();
            for (Reservation r : dummyData) {
                if (r.getUserName().equals(username)) {
                    result.add(r);
                }
            }
            return result;
        }
    }
}
