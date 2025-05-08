
/**
 *
 * @author rbcks
 */

package network;

import java.io.*;
import java.net.Socket;

public class Client {
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    public static boolean connect() {
        try {
            socket = new Socket("localhost", 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            System.err.println("[Client] 서버 연결 실패: " + e.getMessage());
            return false;
        }
    }

    public static Response send(Request request) {
        try {
            out.writeObject(request);
            return (Response) in.readObject();
        } catch (Exception e) {
            return new Response(false, "요청 전송 실패: " + e.getMessage(), null);
        }
    }

    public static void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[Client] 연결 종료됨");
            }
        } catch (IOException e) {
            System.err.println("[Client] 종료 중 오류: " + e.getMessage());
        }
    }
}
