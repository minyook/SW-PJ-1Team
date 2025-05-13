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
            Request req = (Request) in.readObject();
            Response res = RequestHandler.handle(req);
            out.writeObject(res);

            if (res.isSuccess() && "LOGIN".equals(req.getType())) {
                ClientManager.clientConnected();
            }
        } catch (Exception e) {
            System.err.println("[클라이언트 처리 오류] " + e.getMessage());
        } finally {
            ClientManager.clientDisconnected();
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}

