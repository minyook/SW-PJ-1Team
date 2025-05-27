package common;

import java.io.Serializable;
import java.util.List;

/**
 * 서버↔클라이언트 간 요청(request)과 응답(response)을
 * 하나의 클래스로 통합한 DTO입니다.
 */
public class Message implements Serializable {
    private boolean success;      // 응답 성공 여부
    private String message; // 설명 또는 에러 메시지 
    private Object data;

    private String domain;        // ex: "user", "reservation", "room" 등
    private RequestType type;     // ex: LOGIN, REGISTER, RESERVE, LOAD_MY_RESERVATIONS 등

    private Object payload;       // 단일 객체 (User, Reservation 등)
    private List<?> list;         // 목록 응답용

    private int index;            // 수정/삭제 인덱스 등 필요 시 사용

    public Message() {}

    // 편의 생성자: 최소한의 정보로 성공/실패 메시지 전송
    public Message(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 전체 필드를 한 번에 세팅하고 싶다면 아래 생성자 사용
    public Message(boolean success, String message,
                   String domain, RequestType type,
                   Object payload, List<?> list, int index) {
        this.success = success;
        this.message = message;
        this.domain = domain;
        this.type = type;
        this.payload = payload;
        this.list = list;
        this.index = index;
    }

    // --- getters / setters ---

    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public RequestType getType() {
        return type;
    }
    public void setType(RequestType type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }
    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public List<?> getList() {
        return list;
    }
    public void setList(List<?> list) {
        this.list = list;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public Object getData() {
        return data;
    }
}
