package controller;

import common.Message;
import common.RequestType;
import common.ScheduleEntry;
import controller.ScheduleController;
import model.ScheduleModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleControllerTest {

    @TempDir Path tempDir;

    private Path dataFile;
    private ScheduleController controller;
    private StubScheduleModel stubModel;

    static class StubScheduleModel extends ScheduleModel {
        List<ScheduleEntry> listAllReturn = List.of();
        ScheduleEntry created;
        int deletedIndex = -1;

        public StubScheduleModel(Path file) throws IOException {
            super(file.toString());
        }

        @Override public List<ScheduleEntry> listAll()              { return listAllReturn; }
        @Override public void create(ScheduleEntry e)               { this.created = e; }
        @Override public void delete(int idx)                       { this.deletedIndex = idx; }
    }

    @BeforeEach
    void setUp() throws IOException {
        dataFile = tempDir.resolve("schedule.txt");
        Files.createFile(dataFile);

        stubModel  = new StubScheduleModel(dataFile);
        controller = new ScheduleController(stubModel);
    }

    @Test
    void testListDelegates() {
        ScheduleEntry e1 = new ScheduleEntry("월","09:00~10:00","A","ProfA");
        ScheduleEntry e2 = new ScheduleEntry("화","11:00~12:00","B","ProfB");
        stubModel.listAllReturn = Arrays.asList(e1,e2);

        Message req = new Message(); req.setType(RequestType.LIST);
        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(2, res.getList().size());
        System.err.println("SCHEDULE LIST → " + res.getList());
    }

    @Test
    void testCreateDelegates() {
        ScheduleEntry e = new ScheduleEntry("수","14:00~15:00","C","ProfC");
        Message req = new Message();
        req.setType(RequestType.CREATE);
        req.setPayload(e);

        Message res = controller.handle(req);
        assertNull(res.getError());
        assertSame(e, stubModel.created);
        System.err.println("CREATED → " + stubModel.created);
    }
}
