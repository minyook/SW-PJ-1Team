/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.view;

import management.model.Reservation;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ReservationTableModel extends AbstractTableModel {
    private final List<Reservation> list;
    private final String[] columns = {
        "예약 번호", "날짜", "시간", "강의실", "이름", "상태"
    };

    public ReservationTableModel(List<Reservation> list) {
        this.list = list;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Reservation r = list.get(row);
        switch (col) {
            case 0: return r.getId();
            case 1: return r.getDate().toString();
            case 2: return r.getStartTime() + "~" + r.getEndTime();
            case 3: return r.getRoomId();
            case 4: return r.getUserName();
            case 5: return r.getStatus().name();
            default: return "";
        }
    }

    public Reservation getReservationAt(int row) {
        return list.get(row);
    }
}

