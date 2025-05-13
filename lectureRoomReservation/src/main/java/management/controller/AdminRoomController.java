package management.controller;

import management.model.RoomModel;
import management.model.ScheduleModel;
import management.model.Room;
import management.model.ScheduleEntry;
import management.view.AdminReservationFrame;
import management.view.RoomTableModel;
import management.view.ScheduleTableModel;

import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminRoomController {
    private final AdminReservationFrame view;
    private final RoomModel roomModel;
    private final ScheduleModel schedModel;

    public AdminRoomController(AdminReservationFrame view) throws IOException {
        this.view       = view;
        this.roomModel  = new RoomModel();
        this.schedModel = new ScheduleModel();
        init();
    }

    private void init() {
        // 1) 초기 강의실 목록 로드
        refreshRoomList();

        // 2) 강의실 선택 시 상세정보 + 스케줄 로드
        view.addRoomSelectionListener(e -> {
            int idx = view.getSelectedRoomIndex();
            if (idx < 0) return;

            Room room = roomModel.getAll().get(idx);
            view.setRoomDetails(
                room.getRoomId(),
                room.getAvailability(),
                room.getCloseReason()
            );
            // 선택된 방의 스케줄을 보이기
            loadSchedule(room.getRoomId());
        });

        // 3) 요일 필터 콤보박스(==view.getSelectedDay() 사용) 변경 시 테이블 갱신
        view.addDayFilterListener(e -> {
            // 같은 방 ID로 재로드
            String currentRoomId = view.getSelectedRoomId();
            if (currentRoomId != null) {
                loadSchedule(currentRoomId);
            }
        });

        // 4) 등록하기 버튼: 스케줄 추가 → 파일 저장 → 테이블 갱신
        view.addRegisterScheduleListener(e -> {
            String roomId = view.getSelectedRoomId();
            boolean avail = view.isAvailableChecked();
            DayOfWeek day = view.getSelectedDay();
            String[] ts = view.getSelectedTime().split("~");
            String reason = avail ? "" : view.getReasonText();

            List<ScheduleEntry> list;
            try {
                list = schedModel.load(roomId);
            } catch (IOException ex) {
                showError(ex);
                return;
            }

            list.add(new ScheduleEntry(
                day,
                LocalTime.parse(ts[0]),
                LocalTime.parse(ts[1]),
                avail,
                reason
            ));

            try {
                schedModel.save(roomId, list);
                // 저장 후 동일 방+요일로 테이블 재갱신
                loadSchedule(roomId);

                // 만약 '불가' 설정이면 rooms.txt에도 반영
                if (!avail) {
                    Room r = roomModel.getAll().stream()
                        .filter(x -> x.getRoomId().equals(roomId))
                        .findFirst()
                        .orElseThrow();
                    r.setAvailability(Room.Availability.CLOSED);
                    r.setCloseReason(reason);
                    roomModel.updateRoom(r);
                    refreshRoomList();
                }
            } catch (IOException ex) {
                showError(ex);
            }
        });
    }

    /** rooms.txt → roomTable */
    private void refreshRoomList() {
        List<Room> rooms = roomModel.getAll();
        view.setRoomTable(rooms);
    }

    /**
     * schedule_<roomId>.txt → scheduleTable
     * 1) 전체 로드 → 2) view.getSelectedDay() 로 필터 → 3) table 에 모델 설정
     */
    private void loadSchedule(String roomId) {
        try {
            List<ScheduleEntry> all = schedModel.load(roomId);
            DayOfWeek filterDay = view.getSelectedDay();
            List<ScheduleEntry> filtered = all.stream()
                .filter(e -> e.getDay() == filterDay)
                .collect(Collectors.toList());
            view.setScheduleTable(filtered);
        } catch (IOException ex) {
            showError(ex);
        }
    }

    /** 에러 다이얼로그 */
    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(
            view,
            ex.getMessage(),
            "오류",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
