package controller;

import common.*;
import model.AdminReservationModel;

import java.io.IOException;

public class AdminReservationController {
    private final AdminReservationModel model;

    public AdminReservationController() throws IOException {
        model = new AdminReservationModel();
    }

    public Message handle(Message req) {
        Message res = new Message();
        try {
            switch (req.getType()) {
                case LIST:
                    res.setList(model.listAll());
                    break;
                case UPDATE:
                    String status = (String) req.getPayload();
                    model.updateStatus(req.getIndex(), status);
                    break;
                default:
                    res.setError("지원하지 않는 관리자 예약 요청입니다.");
            }
        } catch (Exception e) {
            res.setError(e.getMessage());
        }
        return res;
    }
}