package network;

/**
 *
 * @author rbcks
 */
import java.util.concurrent.atomic.AtomicInteger;

public class ClientManager {
    private static final AtomicInteger count = new AtomicInteger(0);

    public static void clientConnected() {
        int current = count.incrementAndGet();
        System.out.println("[접속자 수] +1 → 현재 " + current + "명");
    }

    public static void clientDisconnected() {
        int current = count.decrementAndGet();
        System.out.println("[접속자 수] -1 → 현재 " + current + "명");
    }
    
    public static int getCount() {
        return count.get();
    }
}