package controller;

import common.*;
import model.ScheduleModel;

import java.io.IOException;

public class ScheduleController {
    private final ScheduleModel model;

    public ScheduleController() throws IOException {
        model = new ScheduleModel();
    }

    public Message handle(Message req) {
        Message res = new Message();
        try {
            switch (req.getType()) {
                case LIST:
                    res.setList(model.listAll());
                    break;
                case CREATE:
                    model.create((ScheduleEntry) req.getPayload());
                    break;
                case DELETE:
                    model.delete(req.getIndex());
                    break;
                default:
                    res.setError("지원하지 않는 시간표 요청입니다.");
            }
        } catch (Exception e) {
            res.setError(e.getMessage());
        }
        return res;
    }
}