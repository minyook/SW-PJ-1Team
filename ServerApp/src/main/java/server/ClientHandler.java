package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import common.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class ClientHandler extends Thread {

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        initReservationData(); // 🔹 초기화 호출
    }

    private void initReservationData() {
        File storageFile = new File("storage/reservation_data.txt");
        if (storageFile.exists()) {
            return;
        }

        try (InputStream is = getClass().getResourceAsStream("/reservation_data.txt")) {
            if (is == null) {
                System.err.println("❌ resources에서 reservation_data.txt 파일을 찾을 수 없습니다.");
                return;
            }
            storageFile.getParentFile().mkdirs();
            Files.copy(is, storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ 예약 데이터 초기화 완료 (storage/reservation_data.txt)");
        } catch (IOException e) {
            System.err.println("❌ 예약 데이터 초기화 중 오류 발생:");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("🔵 클라이언트 스트림 연결됨: " + socket.getInetAddress());
            ensureRoomDataInitialized();
            while (true) {
                try {
                    Message msg = (Message) in.readObject();
                    System.out.println("✅ 수신된 메시지: " + msg.getType());

                    Message response = new Message();
                    response.setDomain(msg.getDomain());
                    response.setType(msg.getType());

                    if (msg.getType() == RequestType.LOGIN) {
                        User requestUser = (User) msg.getPayload();
                        User found = findUser(requestUser.getUsername(), requestUser.getPassword());

                        if (found != null) {
                            response.setPayload(found);
                            System.out.println("🔐 로그인 성공: " + found.getUsername());
                        } else {
                            response.setError("아이디 또는 비밀번호가 일치하지 않습니다.");
                            System.out.println("❌ 로그인 실패");
                        }

                    } else if (msg.getType() == RequestType.REGISTER) {
                        User newUser = (User) msg.getPayload();

                        if (checkUserExists(newUser.getUsername())) {
                            response.setError("이미 존재하는 ID입니다.");
                        } else {
                            if (saveUser(newUser)) {
                                response.setPayload("회원가입 완료");
                                System.out.println("✅ 신규 회원 등록됨: " + newUser.getUsername());
                            } else {
                                response.setError("회원가입 저장 중 오류 발생");
                            }
                        }

                    } else if (msg.getType() == RequestType.RESERVE) {
                        Reservation r = (Reservation) msg.getPayload();

                        if (isTimeSlotTaken(r)) {
                            response.setPayload("중복");
                        } else {
                            saveReservation(r);
                            response.setPayload("성공");
                            System.out.println("✅ 예약 저장됨: " + r.getUserName() + " - " + r.getDate() + " " + r.getTime());
                        }

                    } else if (msg.getType() == RequestType.LOAD_TIMETABLE) {
                        Map<String, String> info = (Map<String, String>) msg.getPayload();
                        String date = info.get("date");
                        String room = info.get("room");

                        List<RoomStatus> statusList = loadTimeTable(date, room);

                        response.setDomain("timetable");
                        response.setType(RequestType.LOAD_TIMETABLE);
                        response.setPayload(statusList);

                    } else if (msg.getType() == RequestType.LOAD_SCHEDULE_FILE) {
                        String roomNumber = (String) msg.getPayload();
                        List<String> scheduleLines = loadRoomSchedule(roomNumber);

                        response.setDomain("schedule");
                        response.setType(RequestType.LOAD_SCHEDULE_FILE);
                        response.setPayload(scheduleLines);

                    } else if (msg.getType() == RequestType.LOAD_MY_RESERVATIONS) {
                        String username = (String) msg.getPayload(); // ✅ null 여부 로그 찍기
                        System.out.println("📥 예약 목록 요청 (ID): " + username);

                        List<Reservation> list = loadReservationsByUserId(username);
                        System.out.println("📤 예약 수: " + list.size());

                        response.setDomain("reservation");
                        response.setType(RequestType.LOAD_MY_RESERVATIONS);
                        response.setPayload(list);
                    } else if (msg.getType() == RequestType.LOAD_ALL_RESERVATIONS) {
                        List<Reservation> list = loadAllReservations();

                        response.setDomain("admin");
                        response.setType(RequestType.LOAD_ALL_RESERVATIONS);
                        response.setPayload(list);
                    } else if (msg.getType() == RequestType.UPDATE) {
                        Map<String, String> info = (Map<String, String>) msg.getPayload();
                        String id = info.get("id");
                        String newStatus = info.get("status");

                        boolean success = updateReservationStatus(id, newStatus);
                        response.setPayload(success ? "OK" : "FAIL");
                    } else if (msg.getType() == RequestType.LOAD_ROOMS) {
                        List<Room> rooms = loadRooms();
                        response.setDomain("room");
                        response.setType(RequestType.LOAD_ROOMS);
                        response.setPayload(rooms);
                    } else if (msg.getType() == RequestType.UPDATE_ROOM_STATUS) {
                        Room updatedRoom = (Room) msg.getPayload();
                        boolean success = updateRoomStatus(updatedRoom);

                        response.setDomain("room");
                        response.setType(RequestType.UPDATE_ROOM_STATUS);
                        response.setPayload(success ? "OK" : "FAIL");
                    } else if (msg.getType() == RequestType.LOAD_SCHEDULE_ENTRIES) {
                        String roomId = (String) msg.getPayload();
                        List<ScheduleEntry> entries = loadScheduleEntries(roomId);

                        response.setDomain("schedule");
                        response.setType(RequestType.LOAD_SCHEDULE_ENTRIES);
                        response.setPayload(entries);
                    } else if (msg.getType() == RequestType.SAVE_SCHEDULE_ENTRY) {
                        try {
                            Object[] arr = (Object[]) msg.getPayload();
                            String roomId = (String) arr[0];
                            ScheduleEntry entry = (ScheduleEntry) arr[1];

                            saveScheduleEntry(roomId, entry);
                            response.setPayload("OK");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            response.setError("서버에서 일정 저장 중 오류 발생: " + ex.getMessage());
                        }
                    } else if (msg.getType() == RequestType.LOAD_SCHEDULE_ENTRIES) {
                        String roomId = (String) msg.getPayload();
                        List<ScheduleEntry> list = loadScheduleEntries(roomId);
                        response.setPayload(list);
                    } else {
                        response.setError("지원하지 않는 요청입니다.");
                    }

                    out.writeObject(response);
                    out.flush();
                } catch (EOFException | SocketException e) {
                    System.out.println("⚠️ 클라이언트 연결 종료됨: " + socket.getInetAddress());
                    break;
                } catch (Exception e) {
                    System.err.println("❌ 클라이언트 처리 중 예외 발생: " + e.getMessage());
                    e.printStackTrace();
                    // 클라이언트 연결은 유지
                }
            }
        } catch (IOException e) {
            System.err.println("❌ 소켓 설정 중 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println("🔒 소켓 닫힘: " + socket.getInetAddress());
                }
            } catch (IOException ignored) {
            }
        }
    }

    private User findUser(String id, String pw) {
        File file = new File("storage/user.txt");
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String fileId = parts[0].trim();
                    String filePw = parts[1].trim();
                    String roleCode = parts[2].trim();
                    String name = parts[3].trim();

                    if (fileId.equals(id) && filePw.equals(pw)) {
                        String role = switch (roleCode) {
                            case "s" ->
                                "학생";
                            case "p" ->
                                "교수";
                            case "a" ->
                                "조교";
                            default ->
                                "알 수 없음";
                        };
                        return new User(fileId, filePw, role, name);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean checkUserExists(String username) {
        File file = new File("storage/user.txt");
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].trim().equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean saveUser(User user) {
        File file = new File("storage/user.txt");
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.newLine();
                String line = String.format("%s,%s,%s,%s",
                        user.getUsername(),
                        user.getPassword(),
                        user.getRole(),
                        user.getName());
                writer.write(line);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTimeSlotTaken(Reservation r) {
        File file = new File("storage/reservation_data.txt");
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String date = parts[0];
                    String time = parts[1];
                    String room = parts[2];
                    String status = parts[4];

                    // “거절”은 예약 불가 검사에서 제외
                    if (date.equals(r.getDate())
                            && time.equals(r.getTime())
                            && room.equals(r.getRoomNumber())
                            && !status.equalsIgnoreCase("거절")) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean updateReservationStatus(String id, String newStatus) {
        File file = new File("storage/reservation_data.txt");
        List<String> updated = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentId = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    if (String.valueOf(currentId).equals(id)) {
                        parts[4] = newStatus;
                        found = true;
                    }
                    updated.add(String.join(",", parts));
                    currentId++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String ln : updated) {
                    writer.write(ln + "\n");
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private boolean updateRoomStatus(Room updatedRoom) {
        File file = new File("storage/rooms.txt");
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(updatedRoom.getRoomId())) {
                    lines.add(updatedRoom.getRoomId() + "," + updatedRoom.getAvailability() + "," + updatedRoom.getCloseReason());
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (updated) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String ln : lines) {
                    writer.write(ln + "\n");
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void saveReservation(Reservation r) {
        File file = new File("storage/reservation_data.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.newLine();
            // 변경: r.getUserId() 로 아이디를 꺼내고, 실제 이름을 user.txt 에서 찾아 넣습니다.
            String userId = r.getUserId();
            String realName = getUserNameById(userId);
            String line = String.format("%s,%s,%s,%s,%s",
                    r.getDate(),
                    r.getTime(),
                    r.getRoomNumber(),
                    // user.txt 마지막 필드(이름)를 꺼내서
                    realName != null ? realName : userId,
                    r.getStatus()
            );
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean saveScheduleEntry(String roomId, ScheduleEntry entry) {
        File file = new File("storage/schedule_" + roomId + ".txt");
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(entry.toTextLine());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<RoomStatus> loadTimeTable(String date, String room) {
        List<RoomStatus> result = new ArrayList<>();

        String[] timeSlots = {
            "09:00~09:50", "10:00~10:50", "11:00~11:50",
            "12:00~12:50", "13:00~13:50", "14:00~14:50",
            "15:00~15:50", "16:00~16:50"
        };

        // 시간 → 상태 맵
        Map<String, String> slotStatusMap = new HashMap<>();
        File file = new File("storage/reservation_data.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    // parts[0]=날짜, parts[1]=시간, parts[2]=강의실, parts[4]=상태
                    if (parts.length >= 5
                            && parts[0].equals(date)
                            && parts[2].equals(room)) {
                        String status = parts[4];
                        // '거절'은 비어 있음으로 처리하고, 그 외(예약 대기/예약)만 기록
                        if (!"거절".equals(status)) {
                            slotStatusMap.put(parts[1], status);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 결과 생성: 저장된 상태 있으면 그 상태, 없으면 비어 있음
        for (String slot : timeSlots) {
            String status = slotStatusMap.getOrDefault(slot, "비어 있음");
            result.add(new RoomStatus(slot, status));
        }

        return result;
    }

    private List<String> loadRoomSchedule(String roomNumber) {
        List<String> scheduleList = new ArrayList<>();
        String fileName = "/schedule_" + roomNumber + ".txt";

        try (InputStream is = getClass().getResourceAsStream(fileName); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                scheduleList.add(line);
            }

        } catch (IOException | NullPointerException e) {
            System.err.println("📛 시간표 파일 읽기 실패: " + fileName);
            e.printStackTrace();
        }

        return scheduleList;
    }

    private List<Reservation> loadReservationsByUserId(String userId) {
        List<Reservation> list = new ArrayList<>();
        String userName = getUserNameById(userId);  // 🔸 ID로 이름 조회
        if (userName == null) {
            System.err.println("❌ ID에 해당하는 이름을 찾을 수 없습니다: " + userId);
            return list;
        }

        File file = new File("storage/reservation_data.txt");

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int id = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[3].trim().equals(userName.trim())) {
                    list.add(new Reservation(String.valueOf(id++), parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private List<Reservation> loadAllReservations() {
        List<Reservation> list = new ArrayList<>();
        File file = new File("storage/reservation_data.txt");

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int id = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    list.add(new Reservation(String.valueOf(id++), parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private List<Room> loadRooms() {
        List<Room> list = new ArrayList<>();
        File file = new File("storage/rooms.txt");

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    Room.Availability avail = switch (parts[1].trim()) {
                        case "OPEN", "사용가능" ->
                            Room.Availability.OPEN;
                        case "CLOSED", "사용불가능" ->
                            Room.Availability.CLOSED;
                        default ->
                            throw new IllegalArgumentException("⚠️ 잘못된 상태: " + parts[1]);
                    };
                    Room room = new Room(parts[0], avail);
                    if (parts.length >= 3) {
                        room.setCloseReason(parts[2]);
                    }
                    list.add(room);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return list;
    }

    private List<ScheduleEntry> loadScheduleEntries(String roomId) {
        List<ScheduleEntry> list = new ArrayList<>();
        File file = new File("storage/schedule_" + roomId + ".txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    DayOfWeek day = ScheduleEntry.convertKorDayToEnum(parts[0].trim());
                    String[] times = parts[1].split("~");
                    LocalTime start = LocalTime.parse(times[0].trim());
                    LocalTime end = LocalTime.parse(times[1].trim());
                    String course = parts[2].trim();
                    String prof = parts[3].trim();
                    list.add(new ScheduleEntry(day, start, end, true, "", course, prof));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private String getUserNameById(String userId) {
        File file = new File("storage/user.txt");
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].trim().equals(userId.trim())) {
                    return parts[3].trim();  // ✅ 이름 반환
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void ensureRoomDataInitialized() {
        File target = new File("storage/rooms.txt");
        if (!target.exists()) {
            try (InputStream is = getClass().getResourceAsStream("/rooms.txt")) {
                if (is == null) {
                    return;
                }
                target.getParentFile().mkdirs();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is)); BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertEnumToKorDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY ->
                "월";
            case TUESDAY ->
                "화";
            case WEDNESDAY ->
                "수";
            case THURSDAY ->
                "목";
            case FRIDAY ->
                "금";
            case SATURDAY ->
                "토";
            case SUNDAY ->
                "일";
        };
    }

}
