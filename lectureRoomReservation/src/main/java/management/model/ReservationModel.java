package management.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * reservation_data.txt 파일을
 * 날짜,시간,강의실,이름,상태
 * 형식으로 읽고 쓰는 모델 클래스
 */
public class ReservationModel {
    private static final String DATA_FILE = 
        "src/main/resources/reservation_data.txt";

    private final List<Reservation> list = new ArrayList<>();

    public ReservationModel() throws IOException {
        load();
    }

    private void load() throws IOException {
        list.clear();
        List<String> lines = Files.readAllLines(Paths.get(DATA_FILE));
        int idCounter = 1;

        for (String ln : lines) {
            // split into exactly 5 parts
            String[] f = ln.split(",", 5);
            // f[0]=YYYY-MM-DD, f[1]=HH:mm~HH:mm, f[2]=roomId, f[3]=user, f[4]=status
            LocalDate date = LocalDate.parse(f[0]);
            String[] times = f[1].split("~");
            LocalTime st = LocalTime.parse(times[0]);
            LocalTime et = LocalTime.parse(times[1]);
            String roomId   = f[2];
            String userName = f[3];
            String stStr    = f[4];

            // 한글 상태 → enum 매핑
            Reservation.Status status;
            if      (stStr.contains("거절")) status = Reservation.Status.REJECTED;
            else if (stStr.contains("대기")) status = Reservation.Status.PENDING;
            else                             status = Reservation.Status.APPROVED;

            list.add(new Reservation(
                idCounter++, date, st, et, roomId, userName, status
            ));
        }
    }

    /** 읽기 전용 리스트 반환 */
    public List<Reservation> getAll() {
        return Collections.unmodifiableList(list);
    }

    /** 상태 변경 후 파일에 저장 */
    public void updateStatus(Reservation r, Reservation.Status newStatus) 
            throws IOException {
        int idx = list.indexOf(r);
        if (idx >= 0) {
            r.setStatus(newStatus);
            save();
        }
    }

    /** 메모리의 list 를 reservation_data.txt 에 덮어쓰기 */
    private void save() throws IOException {
        List<String> out = new ArrayList<>();
        for (Reservation r : list) {
            // enum → 한글 상태 문자열
            String statusStr = switch(r.getStatus()) {
                case APPROVED  -> "예약";
                case REJECTED  -> "거절";
                case PENDING   -> "예약 대기";
            };
            out.add(String.join(",",
                r.getDate().toString(),
                r.getStartTime() + "~" + r.getEndTime(),
                r.getRoomId(),
                r.getUserName(),
                statusStr
            ));
        }
        Files.write(
            Paths.get(DATA_FILE),
            out,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
