package controller;

import client.ClientMain;
import common.*;
import view.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginController {
    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
    }

    public void login(String id, String pw) {
        view.setLoginEnabled(false);

        try {
            ClientMain.socket = new Socket(ClientMain.serverIP, ClientMain.serverPort);
            ClientMain.out = new ObjectOutputStream(ClientMain.socket.getOutputStream());
            ClientMain.out.flush();
            ClientMain.in = new ObjectInputStream(ClientMain.socket.getInputStream());

            Message req = new Message();
            req.setDomain("user");
            req.setType(RequestType.LOGIN);
            req.setPayload(new User(id, pw));

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message response = (Message) ClientMain.in.readObject();

            if (response.getType() == RequestType.INFO) {
                String status = (String) response.getPayload();
                if ("WAIT".equals(status)) {
                    view.showError("현재 접속 인원이 많아 대기 큐에 등록되었습니다.");
                    view.setLoginEnabled(true);
                    return;
                } else if ("CONNECTED".equals(status)) {
                    view.showError("대기 중이던 접속이 허용되었습니다. 다시 로그인해주세요.");
                    view.setLoginEnabled(true);
                    return;
                }
            }

            if (response.getError() != null) {
                view.showError("로그인 실패: " + response.getError());
                view.resetFields();
                view.setLoginEnabled(true);
                return;
            }

            User user = (User) response.getPayload();
            view.showMessage("로그인 성공: " + user.getUsername());

            if ("a".equals(user.getRole())) {
                new AdminReservationFrame(user).setVisible(true);
            } else {
                new ReservationMainFrame(user).setVisible(true);
            }

            view.dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            view.showError("서버 연결 중 오류 발생: " + ex.getMessage());
            view.setLoginEnabled(true);
        }
    }
}

