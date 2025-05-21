
package timetable;
import java.util.Map;
import java.util.List;
/**
 *
 * @author rbcks
 */
public class TimeTableController {
    private TimeTableModel model;

    public TimeTableController() {
        model = new TimeTableModel(); // 모델과 연결
    }

    public Map<String, List<String>> getWeeklySchedule(int month, int week, String roomNumber) {
        return model.generateWeeklySchedule(month, week, roomNumber);
    }
}
