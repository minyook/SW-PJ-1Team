package model;

import common.ScheduleEntry;

import javax.swing.table.AbstractTableModel;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 요일별 캘린더 뷰 방식의 스케줄 TableModel
 * 첫 컬럼은 시간, 이후 월~금 컬럼으로 배열되며,
 * 각 셀에는 차단 사유가 표시됩니다.
 */
public class ScheduleTableModel extends AbstractTableModel {
    private final String[] columns = { "시간", "월", "화", "수", "목", "금", "담당교수" };
    private final List<String> timeSlots;
    private final Map<DayOfWeek, Map<String, ScheduleEntry>> scheduleMap;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public ScheduleTableModel(List<ScheduleEntry> entries) {
        // 1) 유니크한 시간 슬롯을 추출해 정렬
        this.timeSlots = entries.stream()
            .map(e -> e.getStartTime().format(timeFmt) + "~" + e.getEndTime().format(timeFmt))
            .distinct()
            .sorted(Comparator.comparing(slot ->
                LocalTime.parse(slot.split("~")[0], timeFmt)))
            .collect(Collectors.toList());

        // 2) 요일×시간 맵 초기화
        scheduleMap = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) {
            scheduleMap.put(d, new HashMap<>());
        }
        // 3) 각 엔트리를 해당 요일·시간 슬롯에 매핑
        for (ScheduleEntry e : entries) {
            String slot = e.getStartTime().format(timeFmt) + "~" + e.getEndTime().format(timeFmt);
            scheduleMap.get(e.getDay()).put(slot, e);
        }
    }

    @Override
    public int getRowCount() {
        return timeSlots.size();
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
        String slot = timeSlots.get(row);
        // 0열: 시간
        if (col == 0) {
            return slot;
        }
        // 1~5열: 월~금 과목명
        if (col >= 1 && col <= 5) {
            DayOfWeek day = DayOfWeek.of(col);
            ScheduleEntry e = scheduleMap.get(day).get(slot);
            return (e == null) ? "" : e.getCourseName();    // 과목명
        }
        // 6열: 담당교수 (해당 시간에 배정된 모든 교수님을 , 로 연결)
        if (col == 6) {
            StringBuilder profs = new StringBuilder();
            for (int d = 1; d <= 5; d++) {
                ScheduleEntry e = scheduleMap
                    .get(DayOfWeek.of(d))
                    .get(slot);
                if (e != null) {
                    if (profs.length() > 0) profs.append(", ");
                    profs.append(e.getProfessorName());
                }
            }
            return profs.toString();
        }
        return "";
    }

}
