/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.controller;

import management.model.*;
import management.view.AdminReservationFrame;  // 동일 Frame 내 탭으로 가정

import javax.swing.*;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import management.model.Room;
import management.model.ScheduleEntry;
import management.view.RoomTableModel;
import management.view.ScheduleTableModel;


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
        refreshRoomList();

        // AdminRoomController.java init() 안에서
        view.addRoomSelectionListener(e -> {
            // 이벤트가 발생할 때마다 선택된 행을 꺼냅니다.
            int idx = view.getSelectedRoomIndex();
            if (idx < 0) return;               // 아무것도 선택 안 된 경우 무시
            Room room = roomModel.getAll().get(idx);
            view.setRoomDetails(
                room.getRoomId(),
                room.getAvailability(),
                room.getCloseReason()
            );
            loadSchedule(room.getRoomId());
        });


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

            // 새 스케줄 추가
            list.add(new ScheduleEntry(
                day,
                LocalTime.parse(ts[0]),
                LocalTime.parse(ts[1]),
                avail,
                reason
            ));

            try {
                schedModel.save(roomId, list);
                view.setScheduleTable(list);
                // 차단된 방 상태 업데이트…
                if (!avail) {
                    Room r = roomModel.getAll().stream()
                        .filter(x -> x.getRoomId().equals(roomId))
                        .findFirst().get();
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

    private void refreshRoomList() {
        List<Room> rooms = roomModel.getAll();
        view.setRoomTable(rooms);
    }

    private void loadSchedule(String roomId) {
        try {
            List<ScheduleEntry> sch = schedModel.load(roomId);
            view.setScheduleTable(sch);
        } catch (IOException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
    }
}

