package controller;

import client.ClientMain;
import client.SocketClient;
import common.Message;
import common.RequestType;
import common.Reservation;
import view.MyReservationFrame;

import java.util.List;

public class MyReservationController {
    private final MyReservationFrame view;
    private final String username;

    public MyReservationController(MyReservationFrame view, String username) {
        this.view = view;
        this.username = username;
        loadMyReservations();
    }

    private void loadMyReservations() {
        try {
            Message req = new Message();
            req.setDomain("reservation");
            req.setType(RequestType.LOAD_MY_RESERVATIONS);
            req.setPayload(username);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            List<Reservation> list = (List<Reservation>) res.getPayload();

            if (list != null) {
                view.updateReservationTable(list);
            } else {
                System.err.println("❌ 예약 리스트가 null입니다.");
                view.updateReservationTable(List.of());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

