package controller;

import common.*;
import model.RoomModel;

import java.io.IOException;

public class RoomController {
    private final RoomModel model;

    public RoomController() throws IOException {
        model = new RoomModel();
    }

    public Message handle(Message req) {
        Message res = new Message();
        try {
            switch (req.getType()) {
                case LIST:
                    res.setList(model.listAll());
                    break;
                case CREATE:
                    model.create((Room) req.getPayload());
                    break;
                case DELETE:
                    model.delete(req.getIndex());
                    break;
                default:
                    res.setError("지원하지 않는 강의실 요청입니다.");
            }
        } catch (Exception e) {
            res.setError(e.getMessage());
        }
        return res;
    }
}