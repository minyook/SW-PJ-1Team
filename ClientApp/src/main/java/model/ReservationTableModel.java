package model;

import common.Reservation;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ReservationTableModel extends AbstractTableModel {
    private final List<Reservation> list;
    private final String[] columns = {
        "예약 날짜", "시간", "강의실", "사용자", "상태"
    };

    public ReservationTableModel(List<Reservation> list) {
        this.list = list;
    }

    @Override
    public int getRowCount() { return list.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Reservation r = list.get(row);
        return switch (col) {
            case 0 -> r.getDate();
            case 1 -> r.getTime();
            case 2 -> r.getRoomNumber();
            case 3 -> r.getUserId();
            case 4 -> r.getStatus();
            default -> "";
        };
    }

    public Reservation getReservationAt(int row) {
        return list.get(row);
    }
}
