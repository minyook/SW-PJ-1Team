
package reservation;

/**
 *
 * @author rbcks
 */
import java.util.List;
import reservation.ReservationResult;

public class ReservationController {

    private ReservationModel model;

    public ReservationController() {
        this.model = new ReservationModel();
    }

    // View에서 호출되는 메서드
    public List<RoomStatus> loadTimetable(String year, String month, String day, String roomNumber) {
        // yyyy-MM-dd 형식으로 조합
        String date = year + "-" + month + "-" + day;
        return model.loadTimetable(date, roomNumber);
    }
    
    public ReservationResult processReservationRequest(String date, String time, String room, String name) {
        if (time == null || time.isBlank()) {
            return ReservationResult.NOT_SELECTED;
        }

        // 중복 예약 또는 수업 여부 확인
        if (!model.checkAvailability(date, time, room)) {
            return ReservationResult.TIME_OCCUPIED;
        }

        // 예약 저장 시도
        boolean saved = model.saveReservation(date, time, room, name, "예약 대기");
        return saved ? ReservationResult.SUCCESS : ReservationResult.ERROR;
    }
}
