package controller;

import client.ClientMain;
import common.Message;
import common.RequestType;
import common.User;
import view.RegisterView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterController {
    private final RegisterView view;

    public RegisterController(RegisterView view) {
        this.view = view;
        view.setRegisterAction(new RegisterListener());
    }

    private class RegisterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = view.getUsername();
            String pw = view.getPassword();
            String name = view.getName();
            String role = view.getRole();

            if (id.isEmpty() || pw.isEmpty() || name.isEmpty()) {
                view.showMessage("모든 필드를 입력하세요.");
                return;
            }

            String roleCode = switch (role) {
                case "학생" -> "s";
                case "교수" -> "p";
                case "조교" -> "a";
                default -> "";
            };

            User user = new User(id, pw, roleCode, name);

            Message msg = new Message();
            msg.setDomain("user");
            msg.setType(RequestType.REGISTER);
            msg.setPayload(user);

            try {
                ClientMain.out.writeObject(msg);
                ClientMain.out.flush();

                Message res = (Message) ClientMain.in.readObject();

                if (res.getError() != null) {
                    view.showMessage("\u274C 회원가입 실패: " + res.getError());
                } else {
                    view.showMessage("\u2705 회원가입 성공!");
                    view.dispose();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                view.showMessage("서버 오류: " + ex.getMessage());
            }
        }
    }
}
