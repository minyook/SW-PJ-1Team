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
 */
public class RoomModel {
    // resources 폴더 아래 rooms.txt 경로
    private static final String ROOM_FILE = "src/main/resources/rooms.txt";

    // 메모리 상에 올려둘 강의실 리스트
    private final List<Room> rooms = new ArrayList<>();

    /**
     * 생성 시 즉시 파일을 읽어서 rooms 리스트를 초기화.
     */
    public RoomModel() throws IOException {
        load();
    }

    /**
     * rooms.txt 파일을 읽어 rooms 리스트를 채웁니다.
     * 라인이 비어 있거나 필드가 부족하면 건너뜁니다.
     */
    private void load() throws IOException {
        rooms.clear();
        Path p = Paths.get(ROOM_FILE);
        if (!Files.exists(p)) {
            return;
        }
        for (String line : Files.readAllLines(p)) {
            if (line.isBlank()) continue;
            String[] f = line.split(",", 3);
            if (f.length < 2) continue;

            String id     = f[0].trim();
            String status = f[1].trim();
            Room.Availability avail;
            String reason = "";

            if ("사용가능".equals(status)) {
                avail = Room.Availability.OPEN;
            } else if ("사용불가능".equals(status)) {
                avail = Room.Availability.CLOSED;
                if (f.length > 2) {
                    reason = f[2].trim();
                }
            } else {
                // 알 수 없는 상태면 기본 OPEN
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
     * 특정 강의실 ID에 대해 상태와 사유만 바꾸고 바로 파일에 저장합니다.
     * @param roomId 변경할 강의실 ID
     * @param avail  변경할 상태 (OPEN / CLOSED)
     * @param reason CLOSED 상태일 때 사용할 사유 (OPEN 은 빈 문자열)
     */
    public void updateAvailability(String roomId, Room.Availability avail, String reason) throws IOException {
        // 새 Room 객체를 만들어 기존 updateRoom 메서드 재사용
        Room updated = new Room(roomId, avail, reason);
        updateRoom(updated);
    }

    /**
     * rooms 리스트의 해당 Room 객체를 통째로 교체한 뒤,
     * rooms.txt 파일을 덮어쓰기 방식으로 갱신합니다.
     */
    public void updateRoom(Room updated) throws IOException {
        // 1) 메모리상의 리스트 갱신
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId().equals(updated.getRoomId())) {
                rooms.set(i, updated);
                break;
            }
        }
        // 2) 파일에 다시 쓰기
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
    