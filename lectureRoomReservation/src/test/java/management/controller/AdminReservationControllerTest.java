package management.controller;

import management.model.Reservation;
import management.model.ReservationModel;
import management.view.AdminReservationFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5만 사용해 AdminReservationController를 단위 테스트하기 위한 클래스
 */
class AdminReservationControllerTest {

    // ─── Stub View ─────────────────────────────────────────────────
    static class StubView extends AdminReservationFrame {
        List<Reservation> lastTableData;
        ActionListener approveListener;
        ActionListener rejectListener;
        ActionListener refreshListener;

        // 선택된 인덱스 (테스트에서 세팅)
        int selectedIndex = -1;

        @Override
        public void setReservationTable(List<Reservation> data) {
            // 컨트롤러가 호출해 준 데이터를 저장해 둡니다.
            lastTableData = new ArrayList<>(data);
        }

        @Override
        public void addApproveListener(ActionListener l) {
            this.approveListener = l;
        }

        @Override
        public void addRejectListener(ActionListener l) {
            this.rejectListener = l;
        }

        @Override
        public void addRefreshListener(ActionListener l) {
            this.refreshListener = l;
        }

        @Override
        public int getSelectedReservationIndex() {
            return selectedIndex;
        }

        @Override
        public Reservation getReservationAt(int idx) {
            return lastTableData.get(idx);
        }
    }

    // ─── Stub Model ────────────────────────────────────────────────
    static class StubModel extends ReservationModel {
        List<Reservation> internal = new ArrayList<>();
        boolean updateCalled = false;
        Reservation lastUpdated;
        Reservation.Status lastStatus;

        public StubModel() throws IOException {
            super(true);  // load(skip)
        }

        @Override
        public List<Reservation> getAll() {
            // 컨트롤러가 refreshTable() 할 때 쓰이는 데이터
            return internal.stream().collect(Collectors.toList());
        }

        @Override
        public void updateStatus(Reservation r, Reservation.Status newStatus) throws IOException {
            updateCalled = true;
            lastUpdated = r;
            lastStatus  = newStatus;
            // 모델 내부 상태 변경까지 흉내
            r.setStatus(newStatus);
        }
    }

    StubView view;
    StubModel model;
    AdminReservationController ctrl;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws IOException {
        view  = new StubView();
        model = new StubModel();

        // 사전에 몇 개의 Reservation 객체를 넣어 둡니다.
        // 기본 생성자(id, date, st, et, roomId, user, status)
        Reservation a = new Reservation(1, null, null, null, "R1", "Alice", Reservation.Status.PENDING);
        Reservation b = new Reservation(2, null, null, null, "R2", "Bob",   Reservation.Status.PENDING);
        model.internal.add(a);
        model.internal.add(b);

        // DI 생성자 사용
        ctrl  = new AdminReservationController(view, model);
    }

    @org.junit.jupiter.api.Test
    void testInitialRefreshLoadsTable() {
        // 생성자에서 init() → refreshTable() 이 불리면서 setReservationTable 호출됨
        assertNotNull(view.lastTableData, "초기화 시 테이블이 로드되어야 한다");
        assertEquals(2, view.lastTableData.size());
        assertEquals("Alice", view.lastTableData.get(0).getUserName());
    }

    @org.junit.jupiter.api.Test
    void testApproveCallsModelAndRefreshes() throws Exception {
        // 사용자 A(인덱스 0)를 선택한 것처럼 설정
        view.selectedIndex = 0;

        // 컨트롤러가 붙여둔 approve 리스너 실행
        view.approveListener.actionPerformed(null);

        // 모델 updateStatus 가 호출되었는지
        assertTrue(model.updateCalled, "updateStatus가 호출되어야 한다");
        assertEquals(model.lastUpdated, view.getReservationAt(0));
        assertEquals(Reservation.Status.APPROVED, model.lastStatus);

        // 그리고 refreshTable()이 다시 불려서 뷰의 lastTableData 가 갱신됨
        assertEquals(Reservation.Status.APPROVED, view.getReservationAt(0).getStatus());
    }

    @org.junit.jupiter.api.Test
    void testRejectCallsModelAndRefreshes() throws Exception {
        // 사용자 B(인덱스 1)를 선택
        view.selectedIndex = 1;

        view.rejectListener.actionPerformed(null);

        assertTrue(model.updateCalled, "거절(updateStatus) 호출 확인");
        assertEquals(model.lastUpdated, view.getReservationAt(1));
        assertEquals(Reservation.Status.REJECTED, model.lastStatus);
    }

    @org.junit.jupiter.api.Test
    void testRefreshButtonReloadsTable() {
        // clear 상태
        view.lastTableData = null;

        // 컨트롤러가 붙여둔 refresh 리스너 실행
        view.refreshListener.actionPerformed(null);

        assertNotNull(view.lastTableData, "새로고침 시 테이블 재로딩");
        assertEquals(2, view.lastTableData.size());
    }
}