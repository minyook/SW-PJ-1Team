package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public static final int PORT = 9999;
    public static final int MAX_ACTIVE = 3;
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
}
