package management.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * rooms.txt 파일로부터 강의실 목록을 로드하고,
 * 변경된 강의실 상태를 다시 파일에 저장하는 모델 클래스.
 * 테스트 할 때 실제 파일 변경을 방지하기 위해 skipLoad 옵션을 제공합니다.
 */
public class RoomModel {
    // 기본 리소스 파일 경로
    private static final String ROOM_FILE = "src/main/resources/rooms.txt";

    // skipLoad 플래그
    private final boolean skipLoad;

    // 메모리용 강의실 리스트
    private final List<Room> rooms = new ArrayList<>();

    /**
     * 기본 생성자: 실제 파일 I/O 수행.
     */
    public RoomModel() throws IOException {
        this(false);
        load();
    }

    /**
     * 테스트용 생성자: 파일 읽기/쓰기 건너뛰기.
     */
    protected RoomModel(boolean skipLoad) {
        this.skipLoad = skipLoad;
    }

    /**
     * rooms.txt 파일을 읽어 rooms 리스트를 채웁니다.
     * skipLoad==true이면 파일을 읽지 않습니다.
     */
    private void load() throws IOException {
        if (skipLoad) return;
        rooms.clear();
        Path p = Paths.get(ROOM_FILE);
        if (!Files.exists(p)) return;
        for (String line : Files.readAllLines(p)) {
            if (line.isBlank()) continue;
            String[] f = line.split(",", 3);
            if (f.length < 2) continue;
            String id = f[0].trim();
            String status = f[1].trim();
            Room.Availability avail;
            String reason = "";
            if ("사용가능".equals(status)) {
                avail = Room.Availability.OPEN;
            } else if ("사용불가능".equals(status)) {
                avail = Room.Availability.CLOSED;
                if (f.length > 2) reason = f[2].trim();
            } else {
                avail = Room.Availability.OPEN;
            }
            rooms.add(new Room(id, avail, reason));
        }
    }

    /**
     * 메모리상의 rooms 리스트를 읽기 전용으로 반환합니다.
     */
    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    /**
     * 테스트용 헬퍼: 메모리 리스트에 직접 추가합니다.
     */
    public void addRoom(Room r) {
        rooms.add(r);
    }

    /**
     * 특정 강의실 상태와 사유 변경 후 저장.
     * skipLoad==true이면 파일 쓰지 않고 메모리만 변경합니다.
     */
    public void updateAvailability(String roomId, Room.Availability avail, String reason) throws IOException {
        Room updated = new Room(roomId, avail, reason);
        updateRoom(updated);
    }

    /**
     * rooms 리스트에서 대체 후 전체 덮어쓰기.
     * skipLoad==true 이면 파일 쓰지 않습니다.
     */
    public void updateRoom(Room updated) throws IOException {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId().equals(updated.getRoomId())) {
                rooms.set(i, updated);
                break;
            }
        }
        if (skipLoad) return;
        List<String> outLines = rooms.stream()
            .map(r -> {
                StringBuilder sb = new StringBuilder();
                sb.append(r.getRoomId()).append(",");
                if (r.getAvailability() == Room.Availability.OPEN) {
                    sb.append("사용가능");
                } else {
                    sb.append("사용불가능");
                    if (r.getCloseReason() != null && !r.getCloseReason().isBlank()) {
                        sb.append(",").append(r.getCloseReason());
                    }
                }
                return sb.toString();
            })
            .collect(Collectors.toList());
        Files.write(
            Paths.get(ROOM_FILE),
            outLines,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
