package model;
import model.RoomModel;
import common.Room;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoomModelTest {
    private static final String TEST_FILE = "resources/room_data.txt";

    @BeforeEach
    void setup() throws IOException {
        Path path = Paths.get(TEST_FILE);
        Files.createDirectories(path.getParent());
        Files.write(path, List.of("911"));
    }

    @Test
    void testListAllReadsRooms() throws IOException {
        RoomModel model = new RoomModel();
        List<Room> rooms = model.listAll();
        assertEquals(1, rooms.size());
        assertEquals("911", rooms.get(0).getRoomId());
    }

    @Test
    void testCreateAddsNewRoom() throws IOException {
        RoomModel model = new RoomModel();
        model.create(new Room("912"));

        List<Room> rooms = model.listAll();
        assertEquals(2, rooms.size());
        assertTrue(rooms.stream().anyMatch(r -> r.getRoomId().equals("912")));
    }

    @Test
    void testDeleteRemovesRoom() throws IOException {
        RoomModel model = new RoomModel();
        model.delete(0);

        List<Room> rooms = model.listAll();
        assertTrue(rooms.isEmpty());
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }
}
