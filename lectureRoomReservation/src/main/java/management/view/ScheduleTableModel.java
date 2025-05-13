package management.view;

import management.model.ScheduleEntry;

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
    private final String[] columns = { "시간", "월", "화", "수", "목", "금" };
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
        if (col == 0) {
            // 첫 번째 컬럼: 시간 슬롯
            return slot;
        }
        // col 1=MONDAY, 2=TUESDAY, … 5=FRIDAY
        DayOfWeek day = DayOfWeek.of(col);
        ScheduleEntry e = scheduleMap.get(day).get(slot);
        if (e == null) {
            return "";
        }
        // '사용 불가능'인 경우만 사유 표시
        return e.isAvailable() ? "" : "불가: " + e.getReason();
    }
}
