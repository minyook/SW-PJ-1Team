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
            if (line == null || line.isBlank()) continue;
            String[] f = line.split(",", 3);
            if (f.length < 2) continue;  // 최소 ID, 상태 필요
            String id = f[0].trim();
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
    public List<Room> getAll() {
        return Collections.unmodifiableList(rooms);
    }

    /**
     * 특정 Room 객체의 상태를 변경한 뒤,
     * rooms.txt 파일 전체를 덮어쓰기로 갱신합니다.
     */
    public void updateRoom(Room updated) throws IOException {
        // 메모리 리스트에 반영
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId().equals(updated.getRoomId())) {
                rooms.set(i, updated);
                break;
            }
        }
        // 파일에 다시 쓰기
        List<String> out = rooms.stream()
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
            out,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
