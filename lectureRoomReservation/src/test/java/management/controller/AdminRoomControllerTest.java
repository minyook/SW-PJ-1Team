package management.controller;

import management.model.Room;
import management.model.RoomModel;
import management.model.ScheduleEntry;
import management.model.ScheduleModel;
import management.view.AdminReservationFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionListener;

import static org.junit.jupiter.api.Assertions.*;

class AdminRoomControllerTest {

    // ─── 테스트 전용 스텁 뷰 ───────────────────────────────────
    static class StubView extends AdminReservationFrame {
    // 기존 필드
    List<Room> lastRooms;
    ActionListener blockListener;
    ActionListener registerListener;
    int selectedRoomIndex;

    // 새로 추가: 컨트롤러가 등록하는 리스너를 저장할 필드
    ListSelectionListener roomSelectionListener;

    @Override
    public void setRoomTable(List<Room> rooms) {
        this.lastRooms = rooms;
    }

    @Override
    public void addRoomSelectionListener(ListSelectionListener l) {
        // 컨트롤러가 여기에 선택 리스너를 붙여줄 때 저장해 둡니다.
        this.roomSelectionListener = l;
    }

    @Override
    public void addBlockListener(ActionListener l) {
        this.blockListener = l;
    }

    @Override
    public void addRegisterScheduleListener(ActionListener l) {
        this.registerListener = l;
    }

    @Override
    public int getSelectedRoomIndex() {
        return selectedRoomIndex;
    }

        @Override
        public DayOfWeek getSelectedDay() {
            return DayOfWeek.MONDAY;
        }

        @Override
        public String getSelectedTime() {
            return "09:00~09:50";
        }

        @Override
        public String getInputCourse() {
            return "자료구조";
        }

        @Override
        public String getInputProfessor() {
            return "김철수";
        }
    }

    // ─── 테스트 전용 스텁 모델 ─────────────────────────────────
    static class StubRoomModel extends RoomModel {
        List<Room> rooms = new ArrayList<>();
        boolean updateCalled;
        String lastRoomId;
        Room.Availability lastAvail;
        String lastReason;

        public StubRoomModel() throws IOException {
            super(true);  // 파일 로드 건너뛰기
        }

        @Override
        public List<Room> getRooms() {
            return rooms;
        }

        @Override
        public void updateAvailability(String roomId, Room.Availability avail, String reason) {
            updateCalled = true;
            lastRoomId   = roomId;
            lastAvail    = avail;
            lastReason   = reason;
        }
    }

    static class StubScheduleModel extends ScheduleModel {
        ScheduleEntry lastEntry;

        public StubScheduleModel() throws IOException {
            super(true);
        }

        @Override
        public void saveAppend(String roomId, ScheduleEntry e) {
            lastEntry = e;
        }

        @Override
        public List<ScheduleEntry> load(String roomId) {
            return lastEntry == null
                ? List.of()
                : List.of(lastEntry);
        }
    }

    private StubView view;
    private StubRoomModel roomModel;
    private StubScheduleModel schedModel;

    @BeforeEach
    void setUp() throws IOException {
        view       = new StubView();
        roomModel  = new StubRoomModel();
        schedModel = new StubScheduleModel();
        roomModel.rooms.add(new Room("R1", Room.Availability.OPEN, ""));

        // 의존성 주입용 생성자 호출
        new AdminRoomController(view, roomModel, schedModel);
    }

    @Test
    void testBlockButton() {
        // 1) 컨트롤러 생성 후 setRoomTable 호출 확인
        assertNotNull(view.lastRooms);
        assertEquals(1, view.lastRooms.size());

        // 2) 선택 인덱스를 0으로 세팅
        view.selectedRoomIndex = 0;
        // 3) 컨트롤러가 붙여놓은 ListSelectionListener 직접 실행
        view.roomSelectionListener.valueChanged(null);

        // 4) 이제 blockListener를 실행하면 currentRoomId가 설정된 상태
        view.blockListener.actionPerformed(null);

        // 5) 모델이 호출됐는지 검증
        assertTrue(roomModel.updateCalled);
        assertEquals("R1", roomModel.lastRoomId);
        assertEquals(Room.Availability.CLOSED, roomModel.lastAvail);
        assertEquals("", roomModel.lastReason);
    }

    @Test
    void testRegisterScheduleButton() {
        view.selectedRoomIndex = 0;
        view.roomSelectionListener.valueChanged(null);

        // 등록 버튼 클릭 시뮬레이션
        view.registerListener.actionPerformed(null);

        ScheduleEntry e = schedModel.lastEntry;
        assertNotNull(e);
        assertEquals(DayOfWeek.MONDAY, e.getDay());
        assertEquals(LocalTime.of(9, 0),  e.getStartTime());
        assertEquals(LocalTime.of(9,50),  e.getEndTime());
        assertEquals("자료구조",            e.getCourseName());
        assertEquals("김철수",             e.getProfessorName());
        assertTrue(e.isAvailable());
    }
}
