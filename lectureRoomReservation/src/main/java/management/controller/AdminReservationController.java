/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.controller;

import management.model.*;
import management.view.AdminReservationFrame;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class AdminReservationController {
    private final AdminReservationFrame view;
    private final ReservationModel model;

    public AdminReservationController(
        AdminReservationFrame view,
        ReservationModel model
    ) throws IOException {
        this.view  = view;
        this.model = model;
        init();
    }
    public AdminReservationController(AdminReservationFrame view) throws IOException {
        this(view, new ReservationModel());
        init();
    }

    private void init() {
        refreshTable();

        view.addApproveListener(e -> {
            int sel = view.getSelectedReservationIndex();
            if (sel < 0) return;
            Reservation r = view.getReservationAt(sel);
            try {
                model.updateStatus(r, Reservation.Status.APPROVED);
                refreshTable();
            } catch (IOException ex) {
                showError(ex);
            }
        });

        view.addRejectListener(e -> {
            int sel = view.getSelectedReservationIndex();
            if (sel < 0) return;
            Reservation r = view.getReservationAt(sel);
            try {
                model.updateStatus(r, Reservation.Status.REJECTED);
                refreshTable();
            } catch (IOException ex) {
                showError(ex);
            }
        });

        view.addRefreshListener(e -> refreshTable());
    }

    private void refreshTable() {
    List<Reservation> all = model.getAll();
    System.out.println("[CTRL] refreshTable() → 모델에 " + all.size() + "개 예약");
    view.setReservationTable(all);
}

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
    }
}

