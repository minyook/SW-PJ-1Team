package model;

import common.Room;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomModelTest {

    @TempDir
    Path tempDir;                // 매 테스트마다 새 임시 폴더

    private Path testFile;
    private RoomModel model;

    @BeforeEach
    void setUp() throws IOException {
        // 1) 임시 폴더에 rooms.txt 생성
        testFile = tempDir.resolve("rooms.txt");
        Files.write(testFile, List.of(
            "101",    // 방 번호만 한 줄에 하나
            "102",
            "103"
        ), StandardOpenOption.CREATE);

        // 2) 파일 경로를 넘겨서 모델 초기화
        model = new RoomModel(testFile.toString());
    }

    @Test
    void testListAllLoadsRooms() throws IOException {
        List<Room> list = model.listAll();
        assertEquals(3, list.size(), "초기 방 목록은 3개여야 합니다");

        // ▶ System.err로 출력: 확인용
        System.err.println("=== 로드된 방 목록 ===");
        list.forEach(r -> System.err.println("Room ID: " + r.getRoomId()));
        System.err.println("=====================");

        assertTrue(list.stream().anyMatch(r -> r.getRoomId().equals("101")));
    }

    @Test
    void testCreateAddsRoom() throws IOException {
        Room newRoom = new Room("201");
        model.create(newRoom);

        List<Room> list = model.listAll();
        assertEquals(4, list.size(), "새 방 추가 후 총 4개여야 합니다");

        System.err.println("=== 방 추가 후 목록 ===");
        list.forEach(r -> System.err.println(r.getRoomId()));
        System.err.println("=====================");

        assertTrue(list.stream().anyMatch(r -> r.getRoomId().equals("201")));
    }

    @Test
    void testDeleteRemovesRoom() throws IOException {
        model.delete(1); // 인덱스 1(“102”) 삭제

        List<Room> list = model.listAll();
        assertEquals(2, list.size(), "삭제 후 2개가 남아야 합니다");

        System.err.println("=== 삭제 후 방 목록 ===");
        list.forEach(r -> System.err.println(r.getRoomId()));
        System.err.println("=====================");

        assertFalse(list.stream().anyMatch(r -> r.getRoomId().equals("102")));
    }
}
