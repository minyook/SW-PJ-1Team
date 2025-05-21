package management.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomModelTest {

    private RoomModel model;

    @BeforeEach
    void setUp() throws IOException {
        // skipLoad=true 로 파일 읽기 건너뛰기
        model = new RoomModel(true);
    }

    @Test
    void testGetRooms_emptyOnSkipLoad() {
        List<Room> rooms = model.getRooms();
        assertNotNull(rooms, "getRooms()는 null을 반환하면 안 됩니다");
        assertTrue(rooms.isEmpty(), "초기에는 빈 리스트여야 합니다");
    }

    @Test
    void testUpdateAvailability() throws IOException {
        // 1) 테스트용 헬퍼로 방 하나 추가
        Room r1 = new Room("912", Room.Availability.OPEN, "");
        model.addRoom(r1);

        // 2) updateAvailability 호출 (CLOSED, reason)
        model.updateAvailability("912", Room.Availability.CLOSED, "점검");

        // 3) 결과 검증
        List<Room> rooms = model.getRooms();
        assertEquals(1, rooms.size(), "리스트 크기는 1이어야 합니다");
        Room r2 = rooms.get(0);
        assertEquals("912",                    r2.getRoomId());
        assertEquals(Room.Availability.CLOSED,  r2.getAvailability());
        assertEquals("점검",                     r2.getCloseReason());
    }

    @Test
    void testUpdateRoom() throws IOException {
        // 1) 헬퍼로 방 추가
        Room original = new Room("911", Room.Availability.OPEN, "");
        model.addRoom(original);

        // 2) 새 Room 객체를 만들어 updateRoom 호출
        Room updated = new Room("911", Room.Availability.CLOSED, "교체");
        model.updateRoom(updated);

        // 3) 메모리 리스트 확인
        List<Room> rooms = model.getRooms();
        assertEquals(1, rooms.size());
        Room r3 = rooms.get(0);
        assertEquals("911",                     r3.getRoomId());
        assertEquals(Room.Availability.CLOSED,   r3.getAvailability());
        assertEquals("교체",                     r3.getCloseReason());
    }
}
