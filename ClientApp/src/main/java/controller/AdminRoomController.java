package controller;

import client.ClientMain;
import common.Message;
import common.RequestType;
import common.Room;
import common.ScheduleEntry;
import view.AdminReservationFrame;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

public class AdminRoomController {
    private final AdminReservationFrame view;

    public AdminRoomController(AdminReservationFrame view) {
        this.view = view;

        addRoomSelectionHandler();
        loadRoomList(); // 처음 목록 불러오기

        // 리스너 연결
        view.addBlockListener(e -> blockSelectedRoom());
        view.addUnblockListener(e -> unblockSelectedRoom());
        view.addRegisterScheduleListener(e -> registerSchedule());
    }

    // 강의실 목록 조회
    public void loadRoomList() {
        try {
            Message req = new Message();
            req.setDomain("room");
            req.setType(RequestType.LOAD_ROOMS);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            List<Room> list = (List<Room>) res.getPayload();

            view.setRoomTable(list);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "강의실 목록 불러오기 실패: " + e.getMessage());
        }
    }

    // 차단
    private void blockSelectedRoom() {
        int idx = view.getSelectedRoomIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(view, "강의실을 선택하세요.");
            return;
        }

        Room selected = view.getRoomAt(idx);
        String reason = view.getReasonText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(view, "차단 사유를 입력하세요.");
            return;
        }

        selected.setAvailability(Room.Availability.CLOSED);
        selected.setCloseReason(reason);

        if (updateRoomStatus(selected)) {
            JOptionPane.showMessageDialog(view, "강의실이 차단되었습니다.");
            loadRoomList();
        } else {
            JOptionPane.showMessageDialog(view, "차단 실패");
        }
    }

    // 해제
    private void unblockSelectedRoom() {
        int idx = view.getSelectedRoomIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(view, "강의실을 선택하세요.");
            return;
        }

        Room selected = view.getRoomAt(idx);
        selected.setAvailability(Room.Availability.OPEN);
        selected.setCloseReason("");

        if (updateRoomStatus(selected)) {
            JOptionPane.showMessageDialog(view, "강의실이 사용 가능 상태로 변경되었습니다.");
            loadRoomList();
        } else {
            JOptionPane.showMessageDialog(view, "차단 해제 실패");
        }
    }

    // 강의실 선택 시 스케줄 불러오기
    public void addRoomSelectionHandler() {
        view.addRoomSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = view.getSelectedRoomIndex();
                if (index != -1) {
                    String roomId = view.getRoomAt(index).getRoomId();
                    Room.Availability avail = view.getRoomAt(index).getAvailability();
                    view.setRoomDetails(roomId, avail, "");
                    loadScheduleFromServer(roomId);
                }
            }
        });
    }

    // 서버로 상태 전송
    private boolean updateRoomStatus(Room room) {
        try {
            Message req = new Message();
            req.setDomain("room");
            req.setType(RequestType.UPDATE_ROOM_STATUS);
            req.setPayload(room);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            return "OK".equals(res.getPayload());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 스케줄 로딩
    public void loadScheduleFromServer(String roomId) {
        try {
            Message req = new Message();
            req.setType(RequestType.LOAD_SCHEDULE_ENTRIES);
            req.setDomain("schedule");
            req.setPayload(roomId);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            if (res.getError() != null) {
                System.err.println("❌ 스케줄 로드 실패: " + res.getError());
                return;
            }

            List<ScheduleEntry> list = (List<ScheduleEntry>) res.getPayload();
            view.setScheduleTable(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 스케줄 등록
    private void registerSchedule() {
        String roomId = view.getSelectedRoomId();
        DayOfWeek day = view.getSelectedDay();
        String timeRange = view.getSelectedTime();
        String course = view.getInputCourse();
        String prof = view.getInputProfessor();

        if (course.isEmpty() || prof.isEmpty()) {
            JOptionPane.showMessageDialog(view, "교수명과 과목명을 모두 입력해주세요.");
            return;
        }

        String[] times = timeRange.split("~");
        LocalTime start = LocalTime.parse(times[0]);
        LocalTime end = LocalTime.parse(times[1]);

        ScheduleEntry entry = new ScheduleEntry(day, start, end, true, "", course, prof);

        try {
            Message req = new Message();
            req.setDomain("schedule");
            req.setType(RequestType.SAVE_SCHEDULE_ENTRY);
            req.setPayload(new Object[]{roomId, entry});

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            if (res.getError() != null) {
                JOptionPane.showMessageDialog(view, "등록 실패: " + res.getError());
            } else {
                JOptionPane.showMessageDialog(view, "등록 성공!");
                loadScheduleFromServer(roomId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "서버와의 통신 중 오류가 발생했습니다.");
        }
    }
}
