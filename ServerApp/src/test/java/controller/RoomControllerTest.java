package controller;

import common.Message;
import common.RequestType;
import common.Room;
import controller.RoomController;
import model.RoomModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomControllerTest {

    @TempDir Path tempDir;

    private Path dataFile;
    private RoomController controller;
    private StubRoomModel stubModel;

    static class StubRoomModel extends RoomModel {
        List<Room> listAllReturn = List.of();
        Room created;
        int deletedIndex = -1;

        public StubRoomModel(Path file) throws IOException {
            super(file.toString());
        }

        @Override public List<Room> listAll() { return listAllReturn; }
        @Override public void create(Room r)    { this.created = r; }
        @Override public void delete(int idx)   { this.deletedIndex = idx; }
    }

    @BeforeEach
    void setUp() throws IOException {
        dataFile = tempDir.resolve("rooms.txt");
        Files.write(dataFile, List.of("101","102","103"), StandardOpenOption.CREATE);

        stubModel  = new StubRoomModel(dataFile);
        controller = new RoomController(stubModel);
    }

    @Test
    void testListDelegates() {
        Room r1 = new Room("101");
        Room r2 = new Room("102");
        stubModel.listAllReturn = Arrays.asList(r1,r2);

        Message req = new Message(); req.setType(RequestType.LIST);
        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(2, res.getList().size());
        System.err.println("ROOM LIST → " + res.getList());
    }

    @Test
    void testCreateDelegates() {
        Room newRoom = new Room("201");
        Message req = new Message();
        req.setType(RequestType.CREATE);
        req.setPayload(newRoom);

        Message res = controller.handle(req);
        assertNull(res.getError());
        assertSame(newRoom, stubModel.created);
        System.err.println("ROOM CREATED → " + stubModel.created.getRoomId());
    }

    @Test
    void testDeleteDelegates() {
        Message req = new Message();
        req.setType(RequestType.DELETE);
        req.setIndex(1);

        Message res = controller.handle(req);
        assertNull(res.getError());
        assertEquals(1, stubModel.deletedIndex);
        System.err.println("ROOM DELETED INDEX → " + stubModel.deletedIndex);
    }
}
