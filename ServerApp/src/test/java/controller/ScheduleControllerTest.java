package controller;

import common.Message;
import common.RequestType;
import common.ScheduleEntry;
import controller.ScheduleController;
import model.ScheduleModel;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleControllerTest {

    static class StubScheduleModel extends ScheduleModel {
        List<ScheduleEntry> listAllReturn;
        ScheduleEntry created;
        int deletedIndex = -1;

        public StubScheduleModel() throws IOException {
            // 실제 파일 IO는 무시하기 위한 더미 경로
            super("src/test/resources/unused_schedule.txt");
        }

        @Override
        public List<ScheduleEntry> listAll() {
            return listAllReturn;
        }

        @Override
        public void create(ScheduleEntry e) {
            this.created = e;
        }

        @Override
        public void delete(int index) {
            this.deletedIndex = index;
        }
    }

    private StubScheduleModel stubModel;
    private ScheduleController controller;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 더미 파일 준비
        Path p = Paths.get("src/test/resources/unused_schedule.txt");
        Files.createDirectories(p.getParent());
        Files.write(p, List.of(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        stubModel  = new StubScheduleModel();
        controller = new ScheduleController(stubModel);
    }

    @Test
    void listDelegatesToModel() {
        ScheduleEntry e1 = new ScheduleEntry("월", "09:00~10:00", "자료구조", "홍길동교수님");
        ScheduleEntry e2 = new ScheduleEntry("화", "11:00~12:00", "운영체제", "김철수교수님");
        stubModel.listAllReturn = Arrays.asList(e1, e2);

        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = controller.handle(req);

        assertNull(res.getError());
        List<?> out = res.getList();
        assertEquals(2, out.size());
        assertSame(e1, out.get(0));
        assertSame(e2, out.get(1));
    }

    @Test
    void createDelegates() {
        ScheduleEntry e = new ScheduleEntry("수", "14:00~15:00", "데이터베이스", "최교수님");
        Message req = new Message();
        req.setType(RequestType.CREATE);
        req.setPayload(e);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertSame(e, stubModel.created);
    }

    @Test
    void deleteDelegates() {
        Message req = new Message();
        req.setType(RequestType.DELETE);
        req.setIndex(5);

        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(5, stubModel.deletedIndex);
    }

    @Test
    void unsupportedTypeReturnsError() {
        Message req = new Message();
        req.setType(RequestType.LOGIN);  // ScheduleController가 지원하지 않는 타입

        Message res = controller.handle(req);

        assertEquals("지원하지 않는 시간표 요청입니다.", res.getError());
    }

    @Test
    void exceptionFromModelIsCaptured() throws IOException {
        ScheduleController exCtrl = new ScheduleController(new ScheduleModel("src/test/resources/unused_schedule.txt") {
            @Override public java.util.List<ScheduleEntry> listAll() { throw new RuntimeException("fail"); }
        });
        Message req = new Message();
        req.setType(RequestType.LIST);

        Message res = exCtrl.handle(req);

        assertEquals("fail", res.getError());
    }
}
