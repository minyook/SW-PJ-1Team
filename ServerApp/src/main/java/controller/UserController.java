package controller;

import common.*;
import model.UserModel;

import java.io.IOException;

public class UserController {
    private final UserModel model;

    public UserController() throws IOException {
        model = new UserModel();
    }

    public Message handle(Message req) {
        Message res = new Message();
        try {
            switch (req.getType()) {
                case LOGIN:
                    User user = (User) req.getPayload();
                    boolean success = model.login(user.getUsername(), user.getPassword());
                    if (success) res.setPayload(user);
                    else res.setError("로그인 실패: 아이디 또는 비밀번호 오류");
                    break;
                case REGISTER:
                    model.register((User) req.getPayload());
                    break;
                default:
                    res.setError("지원하지 않는 사용자 요청입니다.");
            }
        } catch (Exception e) {
            res.setError(e.getMessage());
        }
        return res;
    }
}