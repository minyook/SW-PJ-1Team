/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.controller;

/**
 *
 * @author limmi
 */
import management.model.Room;
import management.model.Reservation;
import management.model.AdminModel;
import management.view.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class AdminControllerCode {
    private AdminModel model;
    private AdminViewCode view;
    private List<Room> rooms;
    private List<Reservation> ress;

    public AdminControllerCode(AdminModel m, AdminViewCode v) {
        this.model = m; this.view = v;
        loadAll();
        bindActions();
    }

    private void loadAll() {
        try {
            rooms = model.loadRooms();
            ress  = model.loadReservations();
            updateRoomList();
            updateResTable();
        } catch (IOException e) { error(e); }
    }

    private void updateRoomList() {
        DefaultListModel<String> lm = new DefaultListModel<>();
        rooms.forEach(r -> lm.addElement(r.toString()));
        view.roomList.setModel(lm);
    }

    private void updateResTable() {
        String[] cols = {"ID","강의실","사용자","상태","사유"};
        DefaultTableModel tm = new DefaultTableModel(cols,0);
        ress.forEach(r -> tm.addRow(new Object[]{
            r.getId(), r.getRoomId(), r.getUser(), r.getStatus(), r.getReason()
        }));
        view.resTable.setModel(tm);
    }

    private void bindActions() {
        view.btnAdd.addActionListener(e -> {
            String id = JOptionPane.showInputDialog("강의실 ID:");
            if (id!=null && !id.isBlank()) {
                rooms.add(new Room(id,false));
                persistAll();
            }
        });
        view.btnDel.addActionListener(e -> {
            int i = view.roomList.getSelectedIndex();
            if (i>=0) { rooms.remove(i); persistAll(); }
        });
        view.btnToggle.addActionListener(e -> {
            int i = view.roomList.getSelectedIndex();
            if (i>=0) {
                Room r = rooms.get(i);
                r.setBlocked(!r.isBlocked());
                persistAll();
            }
        });
        view.btnReload.addActionListener(e -> loadAll());
        view.btnApp.addActionListener(e -> changeRes("APPROVED"));
        view.btnRej.addActionListener(e -> changeRes("REJECTED"));
    }

    private void changeRes(String st) {
    int row = view.resTable.getSelectedRow();
    if (row < 0) return;
    String rid = view.resTable.getValueAt(row, 0).toString();

    // ① reason 선언 시 초기화를 제거 → 딱 한 번만 할당하도록
    final String reason;
    if ("REJECTED".equals(st)) {
        String input = JOptionPane.showInputDialog("거절 사유:");
        if (input == null) return;
        reason = input;
    } else {
        reason = "";
    }

    // ② 람다식 안에서 참조해도 OK
    ress.stream()
        .filter(r -> r.getId().equals(rid))
        .findFirst()
        .ifPresent(r -> {
            r.setStatus(st);
            r.setReason(reason);
        });

    persistAll();
}


    private void persistAll() {
        try {
            model.saveRooms(rooms);
            model.saveReservations(ress);
            updateRoomList();
            updateResTable();
        } catch (IOException e) { error(e); }
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(view, e.getMessage(),"오류",JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            AdminModel m = new AdminModel();
            AdminViewCode v = new AdminViewCode();
            new AdminControllerCode(m,v).view.setVisible(true);
        });
    }
}
