package management.controller;

import management.model.RoomModel;
import management.model.ScheduleModel;
import management.model.Room;
import management.model.ScheduleEntry;
import management.view.AdminReservationFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * 관리자용 강의실/스케줄 컨트롤러
 */
public class AdminRoomController {
    private final AdminReservationFrame view;
    private final RoomModel       roomModel;
    private final ScheduleModel   schedModel;
    private String currentRoomId;

    public AdminRoomController(AdminReservationFrame view) throws IOException {
        this.view       = view;
        this.roomModel  = new RoomModel();
        this.schedModel = new ScheduleModel();
        init();
    }

    private void init() {
        // 1) 강의실 목록 초기 로드
        refreshRoomList();

        // 2) 강의실 선택 시 상세 정보 & 스케줄 로드
        view.addRoomSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int idx = view.getSelectedRoomIndex();
                if (idx < 0) return;

                Room room = roomModel.getRooms().get(idx);
                currentRoomId = room.getRoomId();
                view.setRoomDetails(currentRoomId, room.getAvailability(), /*reason=*/"");
                loadSchedule();
            }
        });

        // 3) 차단하기 버튼
        view.addBlockListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoomId == null) return;
                try {
                    roomModel.updateAvailability(
                        currentRoomId,
                        Room.Availability.CLOSED,
                        ""    // 사유 없음
                    );
                    reloadRoomSelection();
                } catch (IOException ex) {
                    showError(ex);
                }
            }
        });

        // 4) 차단해제 버튼
        view.addUnblockListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoomId == null) return;
                try {
                    roomModel.updateAvailability(
                        currentRoomId,
                        Room.Availability.OPEN,
                        ""    // 사유 없음
                    );
                    reloadRoomSelection();
                } catch (IOException ex) {
                    showError(ex);
                }
            }
        });

        // 5) 스케줄 등록 버튼
        view.addRegisterScheduleListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoomId == null) {
                    JOptionPane.showMessageDialog(
                        view,
                        "먼저 강의실을 선택하세요.",
                        "경고",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // 요일, 시간, 과목명, 교수명 가져오기
                DayOfWeek day = view.getSelectedDay();
                String[] ts  = view.getSelectedTime().split("~");
                LocalTime st = LocalTime.parse(ts[0]);
                LocalTime et = LocalTime.parse(ts[1]);
                String course = view.getInputCourse();
                String prof   = view.getInputProfessor();

                // 항상 사용 가능(true) & 사유 없음("")
                ScheduleEntry newEntry = new ScheduleEntry(
                    day, st, et,
                    /*available=*/true,
                    /*reason=*/"",
                    course,
                    prof
                );

                try {
                    // 스케줄 저장 및 갱신
                    schedModel.saveAppend(currentRoomId, newEntry);
                    List<ScheduleEntry> all = schedModel.load(currentRoomId);
                    view.setScheduleTable(all);
                } catch (IOException ex) {
                    showError(ex);
                }
            }
        });
    }

    /** 강의실 목록을 읽어 View에 세팅 */
    private void refreshRoomList() {
        List<Room> rooms = roomModel.getRooms();
        view.setRoomTable(rooms);
    }

    /** 선택된 강의실의 스케줄을 View에 세팅 */
    private void loadSchedule() {
        try {
            List<ScheduleEntry> all = schedModel.load(currentRoomId);
            view.setScheduleTable(all);
        } catch (IOException ex) {
            showError(ex);
        }
    }

    /** 목록 재로딩 후, 마지막 선택 상태 유지 및 상세∙스케줄 갱신 */
    private void reloadRoomSelection() {
        refreshRoomList();
        List<Room> rooms = roomModel.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId().equals(currentRoomId)) {
                view.getRoomTable().setRowSelectionInterval(i, i);
                view.setRoomDetails(currentRoomId, rooms.get(i).getAvailability(), "");
                loadSchedule();
                break;
            }
        }
    }

    /** 오류 다이얼로그 표시 */
    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(
            view,
            ex.getMessage(),
            "오류",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
