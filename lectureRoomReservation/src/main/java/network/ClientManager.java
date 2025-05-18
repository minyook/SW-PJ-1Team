package network;

/**
 *
 * @author rbcks
 */
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientManager {
    private static final AtomicInteger count = new AtomicInteger(0);
    private static final Set<String> loggedInCredentials = ConcurrentHashMap.newKeySet(); // "username,password" 형태
    public static void clientConnected() {
        int current = count.incrementAndGet();
        System.out.println("[접속자 수] +1 → 현재 " + current + "명");
    }

    public static void clientDisconnected() {
        int current = count.decrementAndGet();
        System.out.println("[접속자 수] -1 → 현재 " + current + "명");
    }
    
     public static boolean isLoggedIn(String key) {
        return loggedInCredentials.contains(key);
    }

    public static boolean addLogin(String key) {
        return loggedInCredentials.add(key);
    }

    public static void removeLogin(String key) {
        loggedInCredentials.remove(key);
    }
    
    public static int getCount() {
        return count.get();
    }
}