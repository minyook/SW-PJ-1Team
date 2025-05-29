package controller;

import common.Message;
import common.RequestType;
import common.Reservation;
import controller.ReservationController;
import model.ReservationModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationControllerTest {

    // 매 테스트마다 새 임시 디렉토리
    @TempDir
    Path tempDir;

    private Path dummyFile;
    private ReservationController controller;
    private StubReservationModel stubModel;

    /**
     * ReservationModel을 상속해 파일 경로만 주입받고 실제 로드/저장은 override,
     * LIST/CREATE/DELETE/UPDATE만 기록하도록 만듭니다.
     */
    static class StubReservationModel extends ReservationModel {

        List<Reservation> listAllReturn = List.of();
        Reservation created;
        int deletedIndex = -1;
        int updatedIndex = -1;
        Reservation updatedPayload;

        // Path.toString() 으로 super 호출
        public StubReservationModel(Path file) throws IOException {
            super(file.toString());
        }

        @Override
        public List<Reservation> listAll() {
            return listAllReturn;
        }

        @Override
        public void create(Reservation r) {
            this.created = r;
        }

        @Override
        public void delete(int index) {
            this.deletedIndex = index;
        }

        @Override
        public void update(int index, Reservation updated) {
            this.updatedIndex = index;
            this.updatedPayload = updated;
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        // 1) 임시 폴더에 더미 파일 생성 (내용은 중요하지 않음)
        dummyFile = tempDir.resolve("reservation_data.txt");
        Files.createFile(dummyFile);

        // 2) Stub 모델에 파일 경로 주입, 컨트롤러 초기화
        stubModel = new StubReservationModel(dummyFile);
        controller = new ReservationController(stubModel);
    }

    @Test
    void testListDelegatesToModel() {
        // 주어진 리스트를 stub이 반환하도록 설정
        Reservation r1 = new Reservation("R1", "2025-06-01", "10:00~10:50", "901", "user1", "예약");
        Reservation r2 = new Reservation("R2", "2025-06-02", "11:00~11:50", "902", "user2", "예약");
        stubModel.listAllReturn = Arrays.asList(r1, r2);

        Message req = new Message();
        req.setType(RequestType.LIST);
        Message res = controller.handle(req);

        assertNull(res.getError());
        assertEquals(2, res.getList().size());
        assertSame(r1, res.getList().get(0));
        assertSame(r2, res.getList().get(1));

        // 눈으로도 확인
        System.err.println("=== LIST 결과 ===");
        res.getList().forEach(System.err::println);
        System.err.println("================");
    }

    @Test
    void testCreateDelegatesToModel() {
        // GIVEN
        Reservation newRes = new Reservation(null, "2025-06-03", "12:00~12:50", "903", "user3", "예약");
        Message req = new Message();
        req.setType(RequestType.CREATE);
        req.setPayload(newRes);

        // WHEN
        controller.handle(req);

        // THEN
        assertSame(newRes, stubModel.created);
        assertNull(req.getError());
    }

    @Test
    void testDeleteDelegatesToModel() {
        Message req = new Message();
        req.setType(RequestType.DELETE);
        req.setIndex(1);
        Message res = controller.handle(req);

        // 에러 없어야 하고, stubModel.deletedIndex가 1로 찍혀야 합니다
        assertNull(res.getError());
        assertEquals(1, stubModel.deletedIndex);

        System.err.println("deletedIndex = " + stubModel.deletedIndex);
    }
}
