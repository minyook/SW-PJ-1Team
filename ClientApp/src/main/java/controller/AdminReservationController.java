package controller;

import client.ClientMain;
import client.SocketClient;
import common.Message;
import common.RequestType;
import common.Reservation;
import java.util.HashMap;
import view.AdminReservationFrame;

import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

public class AdminReservationController {
     private final AdminReservationFrame view;

    public AdminReservationController(AdminReservationFrame view) {
        this.view = view;

        view.addApproveListener(e -> handleStatusUpdate("예약"));
        view.addRejectListener(e -> handleStatusUpdate("거절"));
        view.addRefreshListener(e -> loadAllReservations());

        loadAllReservations();  // 초기 로딩
    }
    // 1. 전체 예약 목록 조회
    public List<Reservation> getAllReservations() {
        try {
            Message req = new Message();
            req.setDomain("admin");
            req.setType(RequestType.LIST);

            Message res = SocketClient.send(req);
            if (res.getError() == null) {
                @SuppressWarnings("unchecked")
                List<Reservation> list = (List<Reservation>) res.getList();
                return list;
            } else {
                System.out.println("조회 실패: " + res.getError());
            }
        } catch (Exception e) {
            System.out.println("서버 요청 오류: " + e.getMessage());
        }
        return List.of();  // 빈 목록 반환
    }

    // 2. 예약 상태 변경 (예: 승인/거절)
    public boolean updateStatus(Reservation r) {
        try {
            Message req = new Message();
            req.setDomain("admin");
            req.setType(RequestType.UPDATE);
            req.setPayload(r);

            Message res = SocketClient.send(req);
            if (res.getError() == null) return true;
            else System.out.println("상태 변경 실패: " + res.getError());

        } catch (Exception e) {
            System.out.println("서버 요청 오류: " + e.getMessage());
        }
        return false;
    }
    public void loadAllReservations() {
        try {
            Message req = new Message();
            req.setDomain("admin");
            req.setType(RequestType.LOAD_ALL_RESERVATIONS);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            List<Reservation> list = (List<Reservation>) res.getPayload();

            view.setReservationTable(list);  // 테이블에 설정
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateReservationStatus(String id, String newStatus) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("id", id);
            payload.put("status", newStatus);

            Message req = new Message();
            req.setDomain("admin");
            req.setType(RequestType.UPDATE);
            req.setPayload(payload);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            if ("OK".equals(res.getPayload())) {
                JOptionPane.showMessageDialog(null, "상태가 변경되었습니다.");
                loadAllReservations();  // 새로고침
            } else {
                JOptionPane.showMessageDialog(null, "상태 변경 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleStatusUpdate(String newStatus) {
        int idx = view.getSelectedReservationIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(null, "예약을 선택하세요.");
            return;
        }

        String reservationId = view.getReservationAt(idx).getReservationId();
        updateReservationStatus(reservationId, newStatus);
    }

}
