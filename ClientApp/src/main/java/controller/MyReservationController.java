package controller;

import client.ClientMain;
import client.SocketClient;
import common.Message;
import common.RequestType;
import common.Reservation;
import view.MyReservationFrame;

import java.util.List;
import javax.swing.JOptionPane;
import view.ReservationFrame;

public class MyReservationController {

    private final MyReservationFrame view;
    private final String username;

    public MyReservationController(MyReservationFrame view, String username) {
        this.view = view;
        this.username = username;

        // 1) 로드
        loadMyReservations();

        // 2) 삭제 버튼 클릭 시
        view.getDeleteButton().addActionListener(e -> handleDelete());

        // 3) 변경 버튼 클릭 시
        view.getChangeButton().addActionListener(e -> handleChange());
    }

    private void loadMyReservations() {
        try {
            Message req = new Message();
            req.setDomain("reservation");
            req.setType(RequestType.LOAD_MY_RESERVATIONS);
            req.setPayload(username);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            List<Reservation> list = (List<Reservation>) res.getPayload();

            if (list != null) {
                view.updateReservationTable(list);
            } else {
                System.err.println("❌ 예약 리스트가 null입니다.");
                view.updateReservationTable(List.of());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 삭제 처리
    private void handleDelete() {
        int row = view.getReservationTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "취소할 예약을 선택하세요.");
            return;
        }
        // 테이블의 첫 번째 컬럼(예약번호)을 id로 사용
        String idStr = view.getReservationTable().getValueAt(row, 0).toString();
        int id = Integer.parseInt(idStr);

        Message req = new Message();
        req.setDomain("reservation");
        req.setType(RequestType.DELETE);
        req.setIndex(id);

        Message res = SocketClient.send(req);
        if (res != null && res.getError() == null) {
            JOptionPane.showMessageDialog(view, "예약이 취소되었습니다.");
            loadMyReservations();
        } else {
            JOptionPane.showMessageDialog(view, "취소 실패: "
                    + (res == null ? "서버 응답 없음" : res.getError()));
        }
    }

    // 변경 처리 (삭제 후 예약 화면으로)
    private void handleChange() {
        int row = view.getReservationTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "변경할 예약을 선택하세요.");
            return;
        }
        String idStr = view.getReservationTable().getValueAt(row, 0).toString();
        int id = Integer.parseInt(idStr);

        // 삭제 요청 (메시지 다이얼로그는 띄우지 않음)
        Message req = new Message();
        req.setDomain("reservation");
        req.setType(RequestType.DELETE);
        req.setIndex(id);
        SocketClient.send(req);

        // 새 예약 화면으로 이동
        view.dispose();
        new ReservationFrame(view.getUser()).setVisible(true);
    }
}
