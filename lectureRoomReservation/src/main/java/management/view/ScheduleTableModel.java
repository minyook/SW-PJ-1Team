/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.view;

import management.model.ScheduleEntry;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * 한 강의실의 스케줄(차단 또는 기존 수업 일정)을 JTable 에 뿌려주기 위한 TableModel
 */
public class ScheduleTableModel extends AbstractTableModel {
    private final List<ScheduleEntry> list;
    private final String[] columns = { "요일", "시작", "종료", "가능 여부", "사유" };
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public ScheduleTableModel(List<ScheduleEntry> list) {
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
        ScheduleEntry e = list.get(row);
        switch (col) {
            case 0: 
                // DayOfWeek → 한글
                switch (e.getDay()) {
                    case MONDAY:    return "월";
                    case TUESDAY:   return "화";
                    case WEDNESDAY: return "수";
                    case THURSDAY:  return "목";
                    case FRIDAY:    return "금";
                    default:        return e.getDay().toString();
                }
            case 1: 
                return e.getStartTime().format(timeFmt);
            case 2:
                return e.getEndTime().format(timeFmt);
            case 3:
                return e.isAvailable() ? "사용가능" : "사용불가능";
            case 4:
                return e.isAvailable() ? "" : e.getReason();
            default:
                return "";
        }
    }

    /** 필요하면 선택된 스케줄 엔트리 꺼내기 */
    public ScheduleEntry getEntryAt(int row) {
        return list.get(row);
    }
}

