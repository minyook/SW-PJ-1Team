package common;

public enum RequestType {
    LIST,       // 전체 목록 요청
    CREATE,     // 새 항목 추가
    DELETE,     // 항목 삭제
    UPDATE,     // 항목 수정
    LOGIN,      // 로그인 요청
    REGISTER,    // 회원가입 요청   
    LOGOUT,     //로그아웃 요청
    LOAD_RESERVATIONS, 
    LOAD_TIMETABLE,
    RESERVE,
    LOAD_SCHEDULE_FILE,
    LOAD_MY_RESERVATIONS,
    LOAD_ALL_RESERVATIONS,
    UPDATE_ROOM_STATUS,
    LOAD_ROOMS,
    LOAD_SCHEDULE_ENTRIES,
    SAVE_SCHEDULE_ENTRY,
    DISCONNECT
}
