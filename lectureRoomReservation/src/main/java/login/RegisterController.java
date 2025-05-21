/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;

import java.io.*;
import javax.swing.*;

public class RegisterController {

    private RegisterView view;
    private final String filePath = "C:\\Users\\user\\Documents\\GitHub\\SW-PJ-1Team\\lectureRoomReservation\\src\\main\\resources\\user.txt";

    private boolean isIdChecked = false;
    private String lastCheckedId = "";

    public RegisterController(RegisterView view) {
        this.view = view;

        // 회원가입 버튼
        this.view.done.addActionListener(e -> registerUser());

        // 중복확인 버튼
        this.view.Idcheck.addActionListener(e -> checkDuplicateId());
    }

    // 중복 아이디 체크
    private void checkDuplicateId() {
        String inputId = view.UserId.getText().trim();
    
        if (inputId.isEmpty()) {
            JOptionPane.showMessageDialog(view, "아이디를 입력해주세요!", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isDuplicateId(inputId)) {
            JOptionPane.showMessageDialog(view, "이미 사용 중인 아이디입니다.", "중복 확인", JOptionPane.WARNING_MESSAGE);
            isIdChecked = false; // 사용 불가
        } else {
            JOptionPane.showMessageDialog(view, "사용 가능한 아이디입니다!", "중복 확인", JOptionPane.INFORMATION_MESSAGE);
            isIdChecked = true;   // 사용 가능
            lastCheckedId = inputId; // 이 아이디로 회원가입하는지 나중에 확인
        }
    }


    // 회원가입 로직
    private void registerUser() {
    String userId = view.UserId.getText().trim();
    String password = new String(view.Pass.getPassword()).trim();
    String role = view.rol.getSelectedItem().toString().equals("학생") ? "s" : "p";
    String name = view.UserName.getText().trim();

    // 입력값 체크
    if (userId.isEmpty() || password.isEmpty() || name.isEmpty()) {
        JOptionPane.showMessageDialog(view, "모든 정보를 입력해주세요!", "입력 오류", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 중복 확인을 안 했거나, 확인한 아이디가 변경됐을 경우!
    if (!isIdChecked || !userId.equals(lastCheckedId)) {
        JOptionPane.showMessageDialog(view, "아이디 중복 확인을 먼저 해주세요!", "확인 필요", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String userInfo = userId + "," + password + "," + role + "," + name;

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
        writer.newLine();
        writer.write(userInfo);
        
        JOptionPane.showMessageDialog(view, "회원가입이 완료되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
        
        // 가입 끝났으니 상태 초기화
        view.UserId.setText("");
        view.Pass.setText("");
        view.UserName.setText("");
        view.rol.setSelectedIndex(0);
        isIdChecked = false;
        lastCheckedId = "";
        view.dispose();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(view, "저장 실패: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
    }
}


    // 아이디 중복 여부 확인
    private boolean isDuplicateId(String userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(userId)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            // 파일이 아직 없을 수도 있음 (첫 가입 시)
            return false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "중복 체크 중 오류 발생: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }
}
