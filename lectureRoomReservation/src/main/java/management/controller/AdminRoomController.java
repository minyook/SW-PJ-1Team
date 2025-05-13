package management.controller;

import management.model.RoomModel;
import management.model.ScheduleModel;
import management.model.Room;
import management.model.ScheduleEntry;
import management.view.AdminReservationFrame;
import management.view.RoomTableModel;

import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.io.IOException;
import java.util.List;

public class AdminRoomController {
    private final AdminReservationFrame view;
    private final RoomModel roomModel;
    private final ScheduleModel schedModel;
    private String currentRoomId = null;

    public AdminRoomController(AdminReservationFrame view) throws IOException {
        this.view       = view;
        this.roomModel  = new RoomModel();
        this.schedModel = new ScheduleModel();
        init();
    }

    private void init() {
        refreshRoomList();

        view.addRoomSelectionListener(e -> {
            int idx = view.getSelectedRoomIndex();
            if (idx < 0) return;

            Room room = roomModel.getAll().get(idx);
            currentRoomId = room.getRoomId();
            view.setRoomDetails(
                currentRoomId,
                room.getAvailability(),
                room.getCloseReason()
            );
            loadSchedule();
        });

        view.addRegisterScheduleListener(e -> {
            if (currentRoomId == null) {
                JOptionPane.showMessageDialog(
                    view, "먼저 강의실을 선택하세요.",
                    "경고", JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            boolean avail = view.isAvailableChecked();
            DayOfWeek day = view.getSelectedDay();
            String[] ts = view.getSelectedTime().split("~");
            String reason = avail ? "" : view.getReasonText();

            ScheduleEntry newEntry = new ScheduleEntry(
                day,
                LocalTime.parse(ts[0]),
                LocalTime.parse(ts[1]),
                avail,
                reason
            );

            try {
                // 1) 스케줄 파일에 append
                schedModel.saveAppend(currentRoomId, newEntry);

                // 2) 저장 후 전체 스케줄 재로드
                List<ScheduleEntry> all = schedModel.load(currentRoomId);
                view.setScheduleTable(all);

                // 3) 항상 RoomModel 업데이트 (사용 가능/사용 불가능 반영)
                Room r = roomModel.getAll().stream()
                    .filter(x -> x.getRoomId().equals(currentRoomId))
                    .findFirst()
                    .orElseThrow();

                if (avail) {
                    r.setAvailability(Room.Availability.OPEN);
                    r.setCloseReason("");
                } else {
                    r.setAvailability(Room.Availability.CLOSED);
                    r.setCloseReason(reason);
                }

                roomModel.updateRoom(r);
                refreshRoomList();

            } catch (IOException ex) {
                showError(ex);
            }
        });
    }

    private void refreshRoomList() {
        view.setRoomTable(roomModel.getAll());
    }

    private void loadSchedule() {
        try {
            List<ScheduleEntry> all = schedModel.load(currentRoomId);
            view.setScheduleTable(all);
        } catch (IOException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(
            view, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE
        );
    }
}
