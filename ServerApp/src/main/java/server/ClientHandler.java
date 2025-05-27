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
import controller.UserController;

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
        boolean slotAcquired = false;
        try {
            // 스트림 초기화
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(socket.getInputStream());
            System.out.println("🔵 클라이언트 스트림 연결됨: " + socket.getInetAddress());
            ensureRoomDataInitialized();

            // 3) 요청 처리 루프
            while (true) {
                try {
                    Message msg = (Message) in.readObject();
                    System.out.println("✅ 수신된 메시지: " + msg.getType());

                    Message response = new Message();
                    response.setDomain(msg.getDomain());
                    response.setType(msg.getType());

                    // 메시지 타입별 분기 처리
                    if (msg.getDomain().equals("user")) {
                        UserController uc = new UserController();
                        Message userResponse = uc.handle(msg);
                        response = userResponse;  // 그대로 응답 사용
                    } // run() 안 메시지 분기 처리 중에
                    else if (msg.getType() == RequestType.DISCONNECT) {
                        // 클라이언트가 직접 연결 종료 의사를 밝힘
                        response.setPayload("DISCONNECTED");
                        out.writeObject(response);
                        out.flush();
                        // break; -> finally 로 넘어가서 slot 반환
                        break;
                    } else if (msg.getDomain().equals("user")) {
                         UserController uc = new UserController();
                         Message userResponse = uc.handle(msg);
                         response = userResponse;  // 그대로 사용
                        } 
                        else if (msg.getType() == RequestType.RESERVE) {
                        Reservation r = (Reservation) msg.getPayload();
                        if (isTimeSlotTaken(r)) {
                            response.setPayload("중복");
                        } else {
                            saveReservation(r);
                            response.setPayload("성공");
                            System.out.println("✅ 예약 저장됨: " + r.getUserName() + " - " + r.getDate() + " " + r.getTime());
                        }
                    } else if (msg.getType() == RequestType.LOAD_TIMETABLE) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String,String> info = (java.util.Map<String,String>) msg.getPayload();
                        String date = info.get("date");
                        String room = info.get("room");
                        java.util.List<RoomStatus> statusList = loadTimeTable(date, room);
                        response.setPayload(statusList);
                    } else if (msg.getType() == RequestType.LOAD_SCHEDULE_FILE) {
                        String roomNumber = (String) msg.getPayload();
                        java.util.List<String> scheduleLines = loadRoomSchedule(roomNumber);
                        response.setPayload(scheduleLines);
                    } else if (msg.getType() == RequestType.LOAD_MY_RESERVATIONS) {
                        String username = (String) msg.getPayload();
                        java.util.List<Reservation> list = loadReservationsByUserId(username);
                        response.setPayload(list);
                    } else if (msg.getType() == RequestType.LOAD_ALL_RESERVATIONS) {
                        java.util.List<Reservation> all = loadAllReservations();
                        response.setPayload(all);
                    } else if (msg.getType() == RequestType.UPDATE) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String,String> info = (java.util.Map<String,String>) msg.getPayload();
                        boolean success = updateReservationStatus(info.get("id"), info.get("status"));
                        response.setPayload(success?"OK":"FAIL");
                    } else if (msg.getType() == RequestType.LOAD_ROOMS) {
                        java.util.List<Room> rooms = loadRooms();
                        response.setPayload(rooms);
                    } else if (msg.getType() == RequestType.UPDATE_ROOM_STATUS) {
                        Room updatedRoom = (Room) msg.getPayload();
                        boolean ok = updateRoomStatus(updatedRoom);
                        response.setPayload(ok?"OK":"FAIL");
                    } else if (msg.getType() == RequestType.LOAD_SCHEDULE_ENTRIES) {
                        String roomId = (String) msg.getPayload();
                        java.util.List<ScheduleEntry> entries = loadScheduleEntries(roomId);
                        response.setPayload(entries);
                    } else if (msg.getType() == RequestType.SAVE_SCHEDULE_ENTRY) {
                        try {
                            Object[] arr = (Object[]) msg.getPayload();
                            saveScheduleEntry((String)arr[0], (ScheduleEntry)arr[1]);
                            response.setPayload("OK");
                        } catch (Exception ex) {
                            response.setMessage("일정 저장 중 오류: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    } else {
                        response.setMessage("지원하지 않는 요청입니다.");
                    }

                    out.writeObject(response);
                    out.flush();
                } catch (EOFException | SocketException e) {
                    System.out.println("⚠️ 클라이언트 연결 종료됨: " + socket.getInetAddress());
                    break;
                } catch (Exception e) {
                    System.err.println("❌ 클라이언트 처리 중 예외 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("❌ 소켓 설정 중 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (slotAcquired) {
                int left = Server.activeCount.decrementAndGet();
                System.out.println("🔄 슬롯 반환: 현재 활성 클라이언트 수 = " + left);
            }
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
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
