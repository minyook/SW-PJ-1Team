package network;

/**
 *
 * @author rbcks
 */
public class RequestHandler {
    public static Response handle(Request req) {
        if ("LOGIN".equals(req.getType())) {
            return new Response(true, "로그인 성공", null);
        } else {
            return new Response(false, "지원하지 않는 요청입니다.", null);
        }
    }
}

