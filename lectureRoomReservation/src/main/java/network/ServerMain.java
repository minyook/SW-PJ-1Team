package network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import login.User;

public class ServerMain {
    private static final int MAX_CLIENTS = 3;
    private static final AtomicInteger waitingClients = new AtomicInteger(0);

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
        boolean connected = false;
        boolean wasWaiting = false;

        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            synchronized (ClientManager.class) {
                if (ClientManager.getCount() >= MAX_CLIENTS) {
                    int waitNum = waitingClients.incrementAndGet();
                    wasWaiting = true;
                    System.out.println("[대기 인원] +1 → 현재 " + waitNum + "명");

                    out.writeObject(new Response(false, "접속 인원이 초과되었습니다. 현재 대기 인원: " + waitNum + "명", null));
                    out.flush();

                    Thread.sleep(200); // 클라이언트가 응답 받을 시간 확보
                    socket.close(); // 강제 종료
                    return;
                }
                ClientManager.clientConnected();
                connected = true;

                if (waitingClients.get() > 0) {
                    int updatedWait = waitingClients.decrementAndGet();
                    System.out.println("[대기 인원] -1 → 현재 " + updatedWait + "명");
                }
            }

            System.out.println("[클라이언트 처리 시작]");

            while (true) {
                Request req = (Request) in.readObject();

                if ("LOGIN".equals(req.getType())) {
                    // 로그인 요청 처리
                    out.writeObject(new Response(true, "로그인 성공\n현재 접속자 수: " + ClientManager.getCount() + "명", null));

                } else if ("DISCONNECT".equals(req.getType())) {
                    // 연결 종료 요청 처리
                    out.writeObject(new Response(true, "연결 종료", null));
                    break;
                    
                } else {
                    out.writeObject(new Response(false, "지원하지 않는 요청입니다.", null));
                }
            }

        } catch (Exception e) {
            System.err.println("[클라이언트 처리 오류] " + e.getMessage());
        } finally {
            if (connected) {
                ClientManager.clientDisconnected();
            }

            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
