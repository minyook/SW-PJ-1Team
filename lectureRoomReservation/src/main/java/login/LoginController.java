package login;

import login.UserModel;
import login.LoginView;
import login.User;
import reservation.ReservationMainFrame;
import management.view.AdminReservationFrame;
import network.Client;
import network.Request;
import network.Response;
import reservation.LoggedInUser;
import reservation.session;

import javax.swing.*;

public class LoginController {

    private UserModel model;
    private LoginView view;

    // 생성자에서 Model과 View를 받아서 저장
    public LoginController(UserModel model, LoginView view) {
        this.model = model;
        this.view = view;

        // View에서 "로그인" 버튼이 눌렸을 때 수행할 리스너를 등록
        this.view.setLoginAction(e -> handleLogin());
    }

    private void handleLogin() {
        // 1. 서버 연결 시도
        if (!Client.connect()) {
            view.showMessage("서버에 연결할 수 없습니다. 관리자에게 문의하세요.");
            return;
        }

        // 2. 사용자 입력 받기
        String username = view.getUsername();
        String password = view.getPassword();

        // 3. 서버에 LOGIN 요청 전송 (User 객체 생성해서 전송)
        User loginAttempt = new User(username, password, "", "");
        Request request = new Request("LOGIN", loginAttempt);
        Response response = Client.send(request);

        // 4. 서버 응답 확인
        if (!response.isSuccess()) {
            view.showMessage(response.getMessage());
            Client.disconnect();
            return;
        }

        // 5. 로컬 파일에서 사용자 정보 확인 (기존 인증)
        User user = model.authenticate(username, password);

        if (user != null) {
            String role = user.getRole();

            // 로그인 성공 시 현재 사용자 정보 세션에 저장
            session.currentUser = new LoggedInUser(
                user.getUsername(),  // 아이디
                user.getName(),      // 이름
                user.getRole()       // 권한
            );

            // 로그인 성공 후 현재 로그인 창 닫기
            view.dispose();

            System.out.println("role: [" + role + "]");

            SwingUtilities.invokeLater(() -> {
                switch (role) {
                    case "s":
                    case "p":
                        // 학생 또는 교수 권한 시 이름 전달하며 예약 화면 띄우기
                        new ReservationMainFrame(user.getName()).setVisible(true);
                        break;
                    case "a":
                        // 조교 권한 시 관리자 화면 띄우기
                        new AdminReservationFrame().setVisible(true);
                        break;
                    default:
                        view.showMessage("알 수 없는 권한 입니다: " + role);
                }
            });
        } else {
            view.showMessage("아이디 또는 비밀번호가 틀렸습니다.");
            Client.disconnect();
        }
    }
}
