/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;

import login.UserModel;
import login.LoginView;
import login.User;
import reservation.ReservationMainFrame;


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
        if (role.equals("s") || role.equals("p")) {
            // 권한이 학생 또는 교수이면 예약 화면 띄우기
            SwingUtilities.invokeLater(() -> {
                new ReservationMainFrame().setVisible(true);
            });
        }
    } else {
        view.showMessage("아이디 또는 비밀번호가 틀렸습니다.");
    }
}

     
    // 내부 클래스: 로그인 버튼이 눌렸을 때 실제로 실행되는 코드
    class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 사용자 입력값을 View에서 가져옴
            String username = view.getUsername();
            String password = view.getPassword();

            // Model을 통해 로그인 인증 시도
           User user = model.authenticate(username, password);
            if (user != null) {
                // 로그인 성공 시
                JOptionPane.showMessageDialog(null, "로그인 성공!");
                // 다음 화면으로 전환
                if(user.getRole().equals("s")||user.getRole().equals("p")){
                    new ReservationMainFrame();
                }
            } else {
                // 로그인 실패 시
                JOptionPane.showMessageDialog(null, "아이디 또는 비밀번호가 틀렸습니다.");
                 }
            }
        }
    }
