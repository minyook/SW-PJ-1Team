
package reservation;

/**
 *
 * @author rbcks
 */
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import reservation.ReservationResult;

public class ReservationController {

    private ReservationModel model;
    private boolean showDialog = true;
     
    public ReservationController() {
        this.model = new ReservationModel();
    }
    public ReservationController(ReservationModel model) {
        this.model = model;
    }

        public void setShowDialog(boolean show) {
        this.showDialog = show;
    }

    // View에서 호출되는 메서드
    public List<RoomStatus> loadTimetable(String year, String month, String day, String roomNumber) {
        
        // yyyy-MM-dd 형식으로 조합
        String date = year + "-" + month + "-" + day;
        // 사용불가 여부 먼저 확인
    String reason = model.checkRoomAvailable(roomNumber);
    if (reason != null) {
        // 모든 시간대에 사유를 표시
        List<RoomStatus> blockedList = new ArrayList<>();
        String[] times = {
            "09:00~09:50", "10:00~10:50", "11:00~11:50",
            "12:00~12:50", "13:00~13:50", "14:00~14:50",
            "15:00~15:50", "16:00~16:50", "17:00~17:50"
        };
        for (String t : times) {
            blockedList.add(new RoomStatus(t, reason));
        }
        return blockedList;
    }
        return model.loadTimetable(date, roomNumber);
    }
    
    public ReservationResult processReservationRequest(String date, String time, String room, String name) {
        if (time == null || time.isBlank()) {
            return ReservationResult.NOT_SELECTED;
        }
        
        //  사용 불가능 강의실 검사
    String reason = model.checkRoomAvailable(room);
    if (reason != null) {
        if (showDialog) {
            JOptionPane.showMessageDialog(null, "해당 강의실은 예약할 수 없습니다.\n사유: " + reason);
        }

        return ReservationResult.ROOM_BLOCKED;
    }

    if (!model.checkAvailability(date, time, room)) {
        return ReservationResult.TIME_OCCUPIED;
    }

    boolean saved = model.saveReservation(date, time, room, name, "예약 대기");
    return saved ? ReservationResult.SUCCESS : ReservationResult.ERROR;

    }
}
