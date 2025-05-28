package controller;

import common.*;
import model.ReservationModel;

import java.io.IOException;

public class ReservationController {
    private final ReservationModel model;

    // 운영용 기존 생성자
    public ReservationController() throws IOException {
        model = new ReservationModel();
    }

    // ★ 테스트용 생성자: 외부에서 ReservationModel 주입
    public ReservationController(ReservationModel model) {
        this.model = model;
    }

    public Message handle(Message req) {
        Message res = new Message();
        try {
            switch (req.getType()) {
                case LIST:
                    res.setList(model.listAll());
                    break;
                case CREATE:
                    model.create((Reservation) req.getPayload());
                    res.setPayload(req.getPayload());
                    break;
                case DELETE:
                    model.delete(req.getIndex());
                    break;
                case UPDATE:
                    model.update(req.getIndex(), (Reservation) req.getPayload());
                    res.setPayload(req.getPayload());
                    break;
                default:
                    res.setError("지원하지 않는 예약 요청입니다.");
            }
        } catch (Exception e) {
            res.setError(e.getMessage());
        }
        return res;
    }
}