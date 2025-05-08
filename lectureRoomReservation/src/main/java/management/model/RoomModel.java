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
     */
    private void load() throws IOException {
        rooms.clear();
        Path p = Paths.get(ROOM_FILE);
        if (!Files.exists(p)) {
            // 파일이 없으면 빈 리스트로 그대로 둡니다.
            return;
        }
        for (String line : Files.readAllLines(p)) {
            // "911,사용가능" 또는 "913,사용불가능,사유" 형식
            String[] f = line.split(",", 3);
            Room.Availability avail = "사용가능".equals(f[1])
                ? Room.Availability.OPEN
                : Room.Availability.CLOSED;
            String reason = (avail == Room.Availability.CLOSED && f.length > 2)
                ? f[2]
                : "";
            rooms.add(new Room(f[0], avail, reason));
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
        // 메모리상의 리스트에서 동일 ID의 Room을 찾아 업데이트
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId().equals(updated.getRoomId())) {
                rooms.set(i, updated);
                break;
            }
        }
        // 변경된 리스트를 파일에 다시 씀
        List<String> out = rooms.stream()
            .map(r ->
                r.getRoomId() + "," +
                (r.getAvailability() == Room.Availability.OPEN ? "사용가능" : "사용불가능") +
                (r.getAvailability() == Room.Availability.CLOSED
                    ? "," + r.getCloseReason()
                    : "")
            )
            .collect(Collectors.toList());

        Files.write(
            Paths.get(ROOM_FILE),
            out,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
