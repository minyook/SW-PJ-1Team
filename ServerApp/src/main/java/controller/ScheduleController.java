// src/main/java/controller/ScheduleController.java
package controller;

import common.Message;
import common.ScheduleEntry;
import common.RequestType;
import model.ScheduleModel;

import java.io.IOException;

public class ScheduleController {
    private final ScheduleModel model;

    // 운영용
    public ScheduleController() throws IOException {
        model = new ScheduleModel();
    }

    // ★ 테스트용 생성자
    public ScheduleController(ScheduleModel model) {
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
