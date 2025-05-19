package login;

import login.UserModel;
import login.LoginView;
import login.User;
import reservation.ReservationMainFrame;
import management.view.AdminReservationFrame;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    String username = view.getUsername();
    String password = view.getPassword();


    User user = model.authenticate(username, password);

    if (user != null) {
        String role = user.getRole();
        System.out.println("role: [" + role + "]");
        
        SwingUtilities.invokeLater(() -> {
        switch(role){
            case "s":
            case "p":
                // 권한이 학생 또는 교수이면 예약 화면 띄우기
                new ReservationMainFrame(user.getName()).setVisible(true); // userId 전달
                break;
            case "a":
                // 권한이 조교이면 관리자 화면 띄우기
                new AdminReservationFrame().setVisible(true);
                break;
            default:
                view.showMessage("알 수 없는 권한 입니다:" + role);
        }
      });
    } else {
        view.showMessage("아이디 또는 비밀번호가 틀렸습니다.");
    }
  }
}