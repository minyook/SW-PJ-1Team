package common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private String domain;         // ex: "reservation", "user", "admin"
    private RequestType type;      // ex: LIST, CREATE, DELETE, etc.
    private Object payload;        // 단일 데이터 (보낼 DTO 객체)
    private List<?> list;          // 리스트 응답 (ex: 예약 목록)
    private int index;             // 수정/삭제 인덱스
    private String error;          // 오류 메시지

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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
} 