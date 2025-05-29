package controller;

import common.Message;
import common.RequestType;
import common.Room;
import controller.RoomController;
import model.RoomModel;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomControllerTest {

    static class StubRoomModel extends RoomModel {
        List<Room> listAllReturn;
        Room created;
        int deletedIndex = -1;

        public StubRoomModel() throws IOException {
            super("src/test/resources/unused_rooms.txt");
        }

        @Override
        public List<Room> listAll() {
            return listAllReturn;
        }

        @Override
        public void create(Room r) {
            this.created = r;
        }

        @Override
        public void delete(int index) {
            this.deletedIndex = index;
        }
    }

    private StubRoomModel stubModel;
    private RoomController controller;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 파일 준비 (빈 파일)
        Path p = Paths.get("src/test/resources/unused_rooms.txt");
        Files.createDirectories(p.getParent());
        Files.write(p, List.of(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        stubModel  = new StubRoomModel();
        controller = new RoomController(stubModel);
    }

    @Test
    void listDelegatesToModel() throws Exception {
        Room r1 = new Room("101");
        Room r2 = new Room("102");
        stubModel.listAllReturn = Arrays.asList(r1, r2);

        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = controller.handle(req);

        assertNull(res.getError());
        List<?> returned = res.getList();
        assertEquals(2, returned.size());
        assertSame(r1, returned.get(0));
        assertSame(r2, returned.get(1));
    }

    @Test
    void createDelegates() throws Exception {
        Room newRoom = new Room("103");
        Message req = new Message();
        req.setType(RequestType.CREATE);
        req.setPayload(newRoom);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertSame(newRoom, stubModel.created);
    }

    @Test
    void deleteDelegates() throws Exception {
        Message req = new Message();
        req.setType(RequestType.DELETE);
        req.setIndex(7);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(7, stubModel.deletedIndex);
    }

    @Test
    void unsupportedTypeReturnsError() throws Exception {
        Message req = new Message();
        req.setType(RequestType.LOGIN);

        Message res = controller.handle(req);

        assertEquals("지원하지 않는 강의실 요청입니다.", res.getError());
    }

    @Test
    void exceptionFromModelIsCaptured() throws Exception {
        // listAll 에서 예외 발생
        RoomController exController = new RoomController(new RoomModel("src/test/resources/unused_rooms.txt") {
            @Override public List<Room> listAll() { throw new RuntimeException("fail"); }
        });

        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = exController.handle(req);

        assertEquals("fail", res.getError());
    }
}
