
package network;

/**
 *
 * @author rbcks
 */
public class ClientMain {
    public static void main(String[] args) {
        if (Client.connect()) {
            Request loginRequest = new Request("LOGIN", "testUser");
            Response res = Client.send(loginRequest);

            System.out.println("[서버 응답] " + res.getMessage());

            Client.disconnect();
        }
    }
}

