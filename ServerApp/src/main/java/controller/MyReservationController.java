package controller;

import common.Message;
import common.RequestType;
import common.Reservation;
import model.ReservationModel;

import java.util.List;

public class MyReservationController {
    private final ReservationModel model;

    public MyReservationController(ReservationModel model) {
        this.model = model;
    }

    public Message handle(Message req) {
        Message res = new Message();
        res.setDomain("my");

        try {
            if (req.getType() == RequestType.LIST) {
                String username = (String) req.getPayload();
                List<Reservation> userReservations = model.getByUser(username);
                res.setList(userReservations);
            } else {
                res.setError("지원하지 않는 요청입니다.");
            }

        } catch (Exception e) {
            res.setError("내 예약 조회 중 오류 발생: " + e.getMessage());
        }

        return res;
    }
}
