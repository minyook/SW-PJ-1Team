package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 9999;
    public static final AtomicInteger activeCount = new AtomicInteger(0);
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ 서버가 포트 " + PORT + "에서 시작되었습니다.");

            // 내부 IP
            String localIp = InetAddress.getLocalHost().getHostAddress();
            System.out.println("🔵 내부 IP 주소: " + localIp + ":" + PORT);

            // 외부 IP
            String externalIp = getPublicIp();
            System.out.println("🟢 외부 IP 주소: " + externalIp + ":" + PORT);

            System.out.println("💡 클라이언트는 위 IP 중 하나로 접속하면 됩니다.");

            // 클라이언트 연결 수락
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 클라이언트 접속: " + clientSocket.getInetAddress());

                // 클라이언트 핸들러 실행
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("❌ 서버 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 외부 IP 가져오는 메서드
    private static String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            return br.readLine();
        } catch (Exception e) {
            return "외부 IP 불러오기 실패";
        }
    }
}