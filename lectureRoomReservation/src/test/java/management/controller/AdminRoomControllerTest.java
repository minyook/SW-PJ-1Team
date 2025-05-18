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

import static org.junit.jupiter.api.Assertions.*;

class AdminRoomControllerTest {

    // ─── 테스트 전용 스텁 뷰 ───────────────────────────────────
    static class StubView extends AdminReservationFrame {
        List<Room> lastRooms;
        ActionListener blockListener;
        ActionListener registerListener;
        int selectedRoomIndex;

        @Override
        public void setRoomTable(List<Room> rooms) {
            this.lastRooms = rooms;
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
        // init()에서 setRoomTable이 호출됐는지 확인
        assertNotNull(view.lastRooms);
        assertEquals(1, view.lastRooms.size());
        assertEquals("R1", view.lastRooms.get(0).getRoomId());

        // 차단 리스너 실행
        view.selectedRoomIndex = 0;
        view.blockListener.actionPerformed(null);

        // 모델에 정확히 CLOSED 호출됐는지 검증
        assertTrue(roomModel.updateCalled);
        assertEquals("R1", roomModel.lastRoomId);
        assertEquals(Room.Availability.CLOSED, roomModel.lastAvail);
        assertEquals("", roomModel.lastReason);
    }

    @Test
    void testRegisterScheduleButton() {
        view.selectedRoomIndex = 0;
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
