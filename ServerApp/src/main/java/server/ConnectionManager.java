package server;

import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private static final int MAX_CONNECTIONS = 3;
    private final Set<Socket> activeConnections = ConcurrentHashMap.newKeySet();

    public synchronized boolean canAccept() {
        return activeConnections.size() < MAX_CONNECTIONS;
    }

    public synchronized void add(Socket socket) {
        activeConnections.add(socket);
        System.out.println("🟢 접속 허용됨. 현재 접속 수 = " + activeConnections.size());
    }

    public synchronized void remove(Socket socket) {
        activeConnections.remove(socket);
        System.out.println("🔴 접속 종료됨. 현재 접속 수 = " + activeConnections.size());
    }

    public int getCurrentSize() {
        return activeConnections.size();
    }
}
