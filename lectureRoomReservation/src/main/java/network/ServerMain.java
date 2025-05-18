package network;

/**
 *
 * @author limmi
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("[서버 시작됨] 포트: 5000");

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handle(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("[서버 오류] " + e.getMessage());
        }
    }

    private static void handle(Socket socket) {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            boolean connected = false;

            while (true) {
                Request req = (Request) in.readObject();

                if ("LOGIN".equals(req.getType())) {
                    if (ClientManager.getCount() >= 3) {
                        out.writeObject(new Response(false, "접속 인원이 초과되었습니다.", null));
                    } else {
                        ClientManager.clientConnected();
                        connected = true;
                        out.writeObject(new Response(true, "로그인 성공\n현재 접속자 수: " + ClientManager.getCount() + "명", null));
                    }
                } else if ("DISCONNECT".equals(req.getType())) {
                    out.writeObject(new Response(true, "연결 종료", null));
                    break;
                } else {
                    out.writeObject(new Response(false, "지원하지 않는 요청입니다.", null));
                }
            }
        } catch (Exception e) {
            System.err.println("[클라이언트 처리 오류] " + e.getMessage());
        } finally {
            ClientManager.clientDisconnected();
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
