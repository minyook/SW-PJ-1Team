package client;

import view.LoginView;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class ClientMain {
    public static ObjectOutputStream out;
    public static ObjectInputStream in;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("서버 IP 주소 입력 : ");
            String address = scanner.nextLine();

            String[] ipPort = address.split(":");
            String ip = ipPort[0];
            int port = (ipPort.length > 1) ? Integer.parseInt(ipPort[1]) : 9999;

            Socket socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("🚀 서버 연결 성공 후 LoginView 띄우기 시도");
            SwingUtilities.invokeLater(() -> {
                System.out.println("🟢 invokeLater 진입");
                new LoginView().setVisible(true);
            });
            System.out.println("✅ invokeLater 호출 완료");


        } catch (Exception e) {
            System.err.println("\u274C 서버 연결 실패: " + e.getMessage());
        }
    }
}