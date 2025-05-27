package server;

import common.Request;
import common.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public static final int PORT = 9999;
    private static final int MAX_CLIENTS = 3;
    private static final AtomicInteger waitingClients = new AtomicInteger(0);
    // 현재 활성화된(로그인 중인) 클라이언트 수
    public static final AtomicInteger activeCount = new AtomicInteger(0);

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        System.out.println("✅ 서버 시작: 포트=" + PORT);
        System.out.println("🔵 내부 IP: " + InetAddress.getLocalHost().getHostAddress() + ":" + PORT);
        System.out.println("🟢 외부 IP: " + getPublicIp() + ":" + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket client = serverSocket.accept();
                threadPool.execute(new ClientHandler(client));
            }
        }
    }

    private static String getPublicIp() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new URL("https://api.ipify.org").openStream()))) {
            return br.readLine();
        } catch (Exception e) {
            return "N/A";
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
