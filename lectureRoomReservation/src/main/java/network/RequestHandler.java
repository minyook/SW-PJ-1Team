package network;

/**
 *
 * @author rbcks
 */
public class RequestHandler {
    public static Response handle(Request req) {
        if ("LOGIN".equals(req.getType())) {
            if (ClientManager.getCount() >= 3) {
                return new Response(false, "접속 인원이 초과되었습니다. 잠시 후 다시 시도해주세요.", null);
            }
            return new Response(true, "로그인 성공", null);
        } else {
            return new Response(false, "지원하지 않는 요청입니다.", null);
        }
    }
}

