package client;

import view.LoginView;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ClientMain {

    public static Socket socket;
    public static ObjectOutputStream out;
    public static ObjectInputStream in;

    public static String serverIP;
    public static int serverPort;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("서버 IP 주소 입력 (예: 192.168.0.11:9999) : ");
            String address = scanner.nextLine();

            String[] ipPort = address.split(":");
            serverIP = ipPort[0];
            serverPort = (ipPort.length > 1) ? Integer.parseInt(ipPort[1]) : 9999;

            socket = new Socket(serverIP, serverPort); // ✅ 전역 변수에 저장
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("🚀 서버 연결 성공 후 LoginView 띄우기 시도");
            SwingUtilities.invokeLater(() -> {
                System.out.println("🟢 invokeLater 진입");
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                }
                new LoginView().setVisible(true);
            });
            System.out.println("✅ invokeLater 호출 완료");

        } catch (Exception e) {
            System.err.println("❌ 서버 연결 실패: " + e.getMessage());
        }
    }

}
