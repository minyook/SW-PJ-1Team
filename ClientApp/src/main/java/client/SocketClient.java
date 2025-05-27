package client;

import common.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {
    private static Socket socket;
    public static ObjectOutputStream out;
    public static ObjectInputStream in;
    private static final String HOST = "서버 IP";
    private static final int PORT = 9999;

    // 로그인/요청 전 매번 호출
    public static void ensureConnected() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(HOST, PORT);
            out    = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in     = new ObjectInputStream(socket.getInputStream());
        }
    }

    public static Message send(Message req) throws Exception {
        ensureConnected();
        out.writeObject(req);
        out.flush();
        return (Message)in.readObject();
    }

    public static void disconnect() {
        try {
            if (out != null) out.close();
            if (in  != null) in .close();
            if (socket != null) socket.close();
        } catch (IOException ignore) {}
        socket = null;
    }
}