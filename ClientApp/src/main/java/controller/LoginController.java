package controller;

import client.ClientMain;
import client.SocketClient;
import common.Message;
import common.RequestType;
import common.User;
import view.LoginView;
import view.ReservationMainFrame;
import view.AdminReservationFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class LoginController {
    private final LoginView view;
    public LoginController(LoginView view) {
        this.view = view;
        view.setLoginAction(new LoginAction());
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.setLoginEnabled(false);
            view.showMessage("로그인 중…");

            new SwingWorker<Message, String>() {
                @Override
                protected Message doInBackground() throws Exception {
                    // --- 로그인 요청 보내기 ---
                    Message req = new Message();
                    req.setDomain("user");
                    req.setType(RequestType.LOGIN);
                    req.setPayload(new User(view.getUsername(), view.getPassword()));
                    ClientMain.out.writeObject(req);
                    ClientMain.out.flush();

                    // --- 서버 응답 반복 읽기 ---
                    while (!isCancelled()) {
                        Message resp = SocketClient.send(req);
                        if (resp.getMessage() != null && resp.getMessage().contains("대기열")) {
                            System.out.println("📤 로그인 대기중");
                            SwingUtilities.invokeLater(() ->
                                view.showMessage(resp.getMessage())
                            );
                            Thread.sleep(200);
                            continue;
                        }
                        return resp;
                    }
                    // 취소된 경우
                    Message cancelled = new Message();
                    cancelled.setMessage("취소됨");
                    return cancelled;
                }

                @Override
                protected void done() {
                    view.setLoginEnabled(true);
                    if (isCancelled()) return;
                    try {
                        Message res = get();
                        if (res.getMessage() != null) {
                            view.showMessage("❌ 로그인 실패: " + res.getMessage());
                            view.resetFields();
                        } else {
                            User user = (User) res.getPayload();
                            view.showMessage("✅ 로그인 성공: " + user.getUsername());
                            new ReservationMainFrame(user).setVisible(true);
                            view.dispose();
                        }
                    } catch (Exception ex) {
                        view.showMessage("서버 통신 오류: " + ex.getMessage());
                    }
                }
            }.execute();
        }
    }
}