/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.view;

import management.model.Room;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * 강의실 목록을 JTable 에 뿌려주기 위한 TableModel
 */
public class RoomTableModel extends AbstractTableModel {
    private final List<Room> rooms;
    private final String[] columns = { "강의실", "상태" };

    public RoomTableModel(List<Room> rooms) {
        this.rooms = rooms;
    }

    @Override
    public int getRowCount() {
        return rooms.size();
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
        Room r = rooms.get(row);
        switch (col) {
            case 0: return r.getRoomId();
            case 1:
                // Availability 을 한글로 표시
                return r.getAvailability() == Room.Availability.OPEN
                        ? "사용가능" : "사용불가능";
            default: return "";
        }
    }

    /** 선택된 행의 Room 객체를 꺼낼 때 필요하면 추가하세요 */
    public Room getRoomAt(int row) {
        return rooms.get(row);
    }
}

