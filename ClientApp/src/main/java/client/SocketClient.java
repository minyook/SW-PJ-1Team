package client;

import common.Message;

public class SocketClient {
    public static Message send(Message request) {
        try {
            ClientMain.out.writeObject(request);
            ClientMain.out.flush();
            return (Message) ClientMain.in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}