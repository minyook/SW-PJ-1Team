package controller;

import client.SocketClient;
import common.Message;
import common.RequestType;
import common.User;
import view.LoginView;
import view.ReservationMainFrame;
import view.AdminReservationFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {
    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
        view.setLoginAction(new LoginAction());
    }

    class LoginAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                view.showMessage("아이디와 비밀번호를 입력하세요.");
                return;
            }

            try {
                Message req = new Message();
                req.setDomain("user");
                req.setType(RequestType.LOGIN);
                req.setPayload(new User(username, password));

                Message res = SocketClient.send(req);
                if (res.getError() == null) {
                    User user = (User) res.getPayload();
                    String role = user.getRole() == null ? "" : user.getRole().trim();

                    view.showMessage(user.getName() + "님 환영합니다! (역할: " + role + ")");

                    switch (role) {
                        case "s", "p" -> new ReservationMainFrame(user).setVisible(true);
                        case "a" -> new AdminReservationFrame(user).setVisible(true);
                        default -> view.showMessage("알 수 없는 역할입니다: [" + role + "]");
                    }

                    view.dispose();
                } else {
                    view.showMessage("로그인 실패: " + res.getError());
                    view.resetFields();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                view.showMessage("서버 통신 오류: " + ex.getMessage());
            }
        }
    }

    public User login(String id, String pw) {
        try {
            User loginUser = new User(id, pw, "", "");

            Message req = new Message();
            req.setDomain("user");
            req.setType(RequestType.LOGIN);
            req.setPayload(loginUser);

            Message res = SocketClient.send(req);

            if (res.getError() == null) {
                return (User) res.getPayload();
            } else {
                System.out.println("로그인 실패: " + res.getError());
            }
        } catch (Exception e) {
            System.out.println("서버 오류: " + e.getMessage());
        }
        return null;
    }
}
