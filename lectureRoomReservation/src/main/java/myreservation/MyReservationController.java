package myreservation;

import reservation.Reservation;
import reservation.ReservationModel;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class MyReservationController {
    private MyReservationFrame view;
    private ReservationModel model;
    private String userId;

    public MyReservationController(MyReservationFrame view, String userId) {
        this.view = view;
        this.userId = userId;
        this.model = new ReservationModel();

        loadTable();
        setListeners();
    }

    private void loadTable() {
        DefaultTableModel modelTable = (DefaultTableModel) view.getReservationTable().getModel();
        modelTable.setRowCount(0);

        List<Reservation> reservations = model.getReservationsByUser(userId);
        for (Reservation r : reservations) {
            modelTable.addRow(new Object[]{
                r.getReservationId(),
                r.getDate(),
                r.getTime(),
                r.getRoomNumber(),
                r.getStatus()
            });
        }
    }

    private void setListeners() {
        view.getDeleteButton().addActionListener(e -> {
            int row = view.getReservationTable().getSelectedRow();
            if (row != -1) {
                String reservationId = view.getReservationTable().getValueAt(row, 0).toString();
                String status = view.getReservationTable().getValueAt(row, 4).toString(); // 상태 열 읽기

            if (status.equals("취소됨")) {
                javax.swing.JOptionPane.showMessageDialog(null, "이미 취소된 예약입니다.");
                return;
            }
                
                
            model.cancelReservation(reservationId);
            javax.swing.JOptionPane.showMessageDialog(null, "예약이 취소되었습니다.");
            loadTable();
            }
        });

        view.getChangeButton().addActionListener(e -> {
            int row = view.getReservationTable().getSelectedRow();
            if (row != -1) {
                String reservationId = view.getReservationTable().getValueAt(row, 0).toString();
                javax.swing.JOptionPane.showMessageDialog(null, "예약 변경 기능은 아직 구현 ㄴㄴ. 예약 ID: " + reservationId);
            }
        });
    }
}
