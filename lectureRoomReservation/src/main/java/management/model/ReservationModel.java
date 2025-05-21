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

    /**
     * 실제 로딩 생성자: 파일에서 기존 예약 읽어오기
     */
    public ReservationModel() throws IOException {
        load();
    }

    /**
     * 테스트 전용 생성자: 파일 읽기를 건너뜀
     */
    protected ReservationModel(boolean skipLoad) {
        // skip load
    }

    /**
     * 파일에서 예약 데이터를 읽어 list 에 채우기
     */
    private void load() throws IOException {
        list.clear();
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) return;
        List<String> lines = Files.readAllLines(path);
        int idCounter = 1;
        for (String ln : lines) {
            if (ln.isBlank()) continue;
            String[] f = ln.split(",", 5);
            LocalDate date = LocalDate.parse(f[0]);
            String[] times = f[1].split("~");
            LocalTime st = LocalTime.parse(times[0]);
            LocalTime et = LocalTime.parse(times[1]);
            String roomId = f[2];
            String userName = f[3];
            String stStr = f[4];
            Reservation.Status status;
            if (stStr.contains("거절"))      status = Reservation.Status.REJECTED;
            else if (stStr.contains("대기")) status = Reservation.Status.PENDING;
            else                               status = Reservation.Status.APPROVED;
            list.add(new Reservation(
                idCounter++, date, st, et, roomId, userName, status
            ));
        }
    }

    /**
     * 읽기 전용으로 예약 리스트 반환
     */
    public List<Reservation> getAll() {
        return Collections.unmodifiableList(list);
    }

    /**
     * 기존: Reservation 객체 기반 상태 변경
     */
    public void updateStatus(Reservation r, Reservation.Status newStatus) throws IOException {
        int idx = list.indexOf(r);
        if (idx >= 0) {
            r.setStatus(newStatus);
            save();
        }
    }

    /**
     * 오버로드: ID와 한글 상태 문자열로 상태 변경
     */
    public void updateStatus(String reservationId, String newStatusStr) throws IOException {
        Reservation.Status ns;
        switch (newStatusStr) {
            case "예약", "승인" -> ns = Reservation.Status.APPROVED;
            case "거절"        -> ns = Reservation.Status.REJECTED;
            case "대기", "예약 대기" -> ns = Reservation.Status.PENDING;
            default             -> throw new IllegalArgumentException("Unknown status: " + newStatusStr);
        }
        for (Reservation r : list) {
            if (String.valueOf(r.getReservationId()).equals(reservationId)) {
                r.setStatus(ns);
                break;
            }
        }
        save();
    }

    /**
     * 새 Reservation 객체를 list 에 추가 후 저장 (원래 메서드)
     */
    public void addReservation(Reservation r) throws IOException {
        list.add(r);
        save();
    }

    /**
     * 오버로드: 파라미터로 문자열 받아 새 예약 생성, list에 추가 후 저장 및 객체 반환
     */
    public Reservation addReservation(
        String roomId,
        String dateStr,
        String timeRange,
        String userName
    ) throws IOException {
        String[] times = timeRange.split("~");
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime st = LocalTime.parse(times[0]);
        LocalTime et = LocalTime.parse(times[1]);
        int nextId = list.size() + 1;
        Reservation r = new Reservation(
            nextId, date, st, et, roomId, userName, Reservation.Status.PENDING
        );
        list.add(r);
        save();
        return r;
    }

    /**
     * 메모리의 list 를 파일에 덮어쓰기
     */
    private void save() throws IOException {
        List<String> out = new ArrayList<>();
        for (Reservation r : list) {
            String statusStr = switch (r.getStatus()) {
                case APPROVED     -> "예약";
                case REJECTED     -> "거절";
                case PENDING      -> "예약 대기";
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
