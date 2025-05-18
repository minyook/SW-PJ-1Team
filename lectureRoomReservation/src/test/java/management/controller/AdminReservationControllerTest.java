package management.controller;

import management.model.Reservation;
import management.model.ReservationModel;
import management.view.AdminReservationFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5만 사용해 AdminReservationController를 단위 테스트하기 위한 클래스
 */
class AdminReservationControllerTest {

    /**
     * 테스트용 StubView: 뷰 인터랙션을 캡처
     */
    static class StubView extends AdminReservationFrame {
        List<Reservation> lastList;
        ActionListener approveL;
        ActionListener rejectL;
        ActionListener refreshL;
        int selectedIdx;
        Reservation selRes;

        @Override
        public void setReservationTable(List<Reservation> data) {
            lastList = data;
        }

        @Override
        public void addApproveListener(ActionListener l) {
            approveL = l;
        }

        @Override
        public void addRejectListener(ActionListener l) {
            rejectL = l;
        }

        @Override
        public void addRefreshListener(ActionListener l) {
            refreshL = l;
        }

        @Override
        public int getSelectedReservationIndex() {
            return selectedIdx;
        }

        @Override
        public Reservation getReservationAt(int idx) {
            return selRes;
        }
    }

    /**
     * 테스트용 StubModel: 상태 변경 호출만 기록
     */
    static class StubModel extends ReservationModel {
        boolean called;
        int updatedId;
        Reservation.Status updatedStatus;

        public StubModel() throws IOException {
            super(true); // skip load
        }

        @Override
        public void updateStatus(Reservation r, Reservation.Status newStatus) {
            called = true;
            updatedId = r.getId();
            updatedStatus = newStatus;
        }
    }

    private StubView view;
    private StubModel model;
    private AdminReservationController ctrl;

    @BeforeEach
    void setUp() throws IOException {
        view = new StubView();
        model = new StubModel();
        // 주입용 생성자 필요: AdminReservationController(AdminReservationFrame, ReservationModel)
        ctrl = new AdminReservationController(view, model);
    }

    @Test
    void testInitialLoadCallsSetReservationTable() {
        // init() 안에서 refreshTable -> setReservationTable 호출됨
        assertNotNull(view.lastList, "초기 로드 시 setReservationTable 호출 필요");
    }

    @Test
    void testApproveListener() {
        // 뷰에 선택 및 반환값 설정
        Reservation r = new Reservation(1, null, null, null, "R101", "User", Reservation.Status.PENDING);
        view.selectedIdx = 0;
        view.selRes = r;

        // 리스너 등록 확인
        assertNotNull(view.approveL, "addApproveListener가 호출되어야 합니다");

        // 이벤트 실행
        view.approveL.actionPerformed(null);

        // 모델 메서드 호출 검증
        assertTrue(model.called, "모델의 updateStatus가 호출되어야 합니다");
        assertEquals(1, model.updatedId, "업데이트된 예약 ID가 일치해야 합니다");
        assertEquals(Reservation.Status.APPROVED, model.updatedStatus, "상태가 APPROVED여야 합니다");
    }

    @Test
    void testRejectListener() {
        Reservation r = new Reservation(2, null, null, null, "R102", "User2", Reservation.Status.PENDING);
        view.selectedIdx = 0;
        view.selRes = r;
        assertNotNull(view.rejectL);

        view.rejectL.actionPerformed(null);

        assertTrue(model.called);
        assertEquals(2, model.updatedId);
        assertEquals(Reservation.Status.REJECTED, model.updatedStatus);
    }

    @Test
    void testRefreshListener() {
        assertNotNull(view.refreshL, "addRefreshListener가 호출되어야 합니다");
        view.refreshL.actionPerformed(null);
        assertNotNull(view.lastList, "refresh 시에도 setReservationTable 호출 필요");
    }
}
