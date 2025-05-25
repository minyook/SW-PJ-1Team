package model;

import common.Room;
import javax.swing.table.AbstractTableModel;
import java.util.List;

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
        return switch (col) {
            case 0 -> r.getRoomId();
            case 1 -> r.getAvailability() == Room.Availability.OPEN ? "사용 가능" : "사용 불가";
            default -> "";
        };
    }

    public Room getRoomAt(int row) {
        return rooms.get(row);
    }
}
