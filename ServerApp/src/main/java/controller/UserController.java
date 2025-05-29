package controller;

import common.*;
import model.UserModel;

import java.io.IOException;

public class UserController {
    private final UserModel model;

    public UserController() throws IOException {
        this(new UserModel());
    }
     public UserController(UserModel model) {
        this.model = model;
    }

    public Message handle(Message req) {
        Message res = new Message();
        res.setDomain("user");
        res.setType(req.getType());

        try {
            switch (req.getType()) {
                case LOGIN -> {
                    User user = (User) req.getPayload();
                    User found = model.findUser(user.getUsername(), user.getPassword());

                    if (found != null) {
                        res.setPayload(found);
                    } else {
                        res.setError("로그인 실패: 아이디 또는 비밀번호 오류");
                    }
                }

                case REGISTER -> {
                    User newUser = (User) req.getPayload();

                    boolean exists = model.checkUserExists(newUser.getUsername());
                    if (exists) {
                        res.setError("이미 존재하는 ID입니다.");
                    } else {
                        model.register(newUser);
                        model.reload();
                        res.setPayload("회원가입 완료");
                    }
                }

                default -> res.setError("지원하지 않는 사용자 요청입니다.");
            }
        } catch (Exception e) {
            res.setError("오류 발생: " + e.getMessage());
        }

        return res;
    }
}