package view;

import client.ClientMain;
import common.Message;
import common.RequestType;
import common.Reservation;
import common.User;
import controller.ReservationController;
import controller.LoginController;
import common.ReservationResult;
import common.RoomStatus;
import view.ReservationMainFrame;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ReservationFrame extends javax.swing.JFrame {
    private final User user; // 🔸 로그인 사용자 정보
    private final ReservationController controller;
    private String lastSelectedRoom;

    public ReservationFrame(User user) {
        this.user = user;
        this.controller = new ReservationController();
        initComponents();
        setLocationRelativeTo(null);
    }
    private void applyTableColoring() {
    timeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = table.getValueAt(row, 1).toString(); // 상태 열은 1번째 열

            // 상태값에 따른 배경색 지정
            if ("비어 있음".equals(status)) {
                c.setBackground(new Color(198, 239, 206)); // 연두색
            } else {
                c.setBackground(new Color(255, 199, 206)); // 연분홍색
            }

            if (isSelected) {
                c.setBackground(new Color(100, 149, 237)); // 선택 시 파란색
                c.setForeground(Color.WHITE);
            } else {
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    });
}
    private List<Reservation> fetchReservationsFromServer() {
        try {
            Message request = new Message();
            request.setDomain("reservation");
            request.setType(RequestType.LOAD_RESERVATIONS);

            ClientMain.out.writeObject(request);
            ClientMain.out.flush();

            Message response = (Message) ClientMain.in.readObject();

            if (response.getError() != null) {
                JOptionPane.showMessageDialog(null, "❌ 예약 목록 불러오기 실패: " + response.getError());
                return new ArrayList<>();
            }

            return (List<Reservation>) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "서버 통신 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    private List<RoomStatus> fetchScheduleFromServer(String dayOfWeek, String roomNumber) {
    try {
        Map<String, String> payload = new HashMap<>();
        payload.put("day", dayOfWeek);
        payload.put("room", roomNumber);

        Message req = new Message();
        req.setDomain("timetable");
        req.setType(RequestType.LOAD_TIMETABLE);
        req.setPayload(payload);

        ClientMain.out.writeObject(req);
        ClientMain.out.flush();

        Message res = (Message) ClientMain.in.readObject();

        if (res.getError() != null) {
            JOptionPane.showMessageDialog(this, "스케줄 불러오기 실패: " + res.getError());
            return Collections.emptyList();
        }

        return (List<RoomStatus>) res.getPayload();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "통신 오류: " + e.getMessage());
        e.printStackTrace();
        return Collections.emptyList();
    }
}

    private void updateTimeTable(List<RoomStatus> statusList) {
        DefaultTableModel model = (DefaultTableModel) timeTable.getModel(); // 🔸 timeTable = JTable
        model.setRowCount(0); // 기존 행 모두 삭제

        for (RoomStatus rs : statusList) {
            model.addRow(new Object[]{rs.getTimeSlot(), rs.getStatus()});
        }

        applyTableColoring();
    }
    
    private void handleRoomButtonClick(String roomNumber) {
        lastSelectedRoom = roomNumber;

        String year = (String) yearComboBox.getSelectedItem();
        String month = (String) monthComboBox.getSelectedItem();
        String day = (String) dayComboBox.getSelectedItem();

        LocalDate date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        DayOfWeek dow = date.getDayOfWeek(); // MONDAY ~ SUNDAY
        String dayKor = switch (dow) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };

        // 🟡 1. 서버에서 해당 요일의 시간표 상태 받아오기 (예약 여부 등)
        List<RoomStatus> statusList = fetchScheduleFromServer(dayKor, roomNumber);

        // 🟡 2. 서버에서 해당 강의실의 수업 시간표(schedule_XXX.txt) 받아오기
        List<String> scheduleLines = controller.loadScheduleFile(roomNumber);

        // 🟡 3. 수업 시간 매핑: 시간 → 과목명 (교수명)
        Map<String, String> classMap = new HashMap<>();
        for (String line : scheduleLines) {
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                classMap.put(parts[1], parts[2] + " (" + parts[3] + ")");
            }
        }

        // 🟡 4. 전체 시간 슬롯 정의
        String[] timeSlots = {
            "09:00~09:50", "10:00~10:50", "11:00~11:50",
            "12:00~12:50", "13:00~13:50", "14:00~14:50",
            "15:00~15:50", "16:00~16:50"
        };

        // 🟡 5. 테이블 갱신
        DefaultTableModel model = (DefaultTableModel) timeTable.getModel();
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[] { "시간", "상태" });

        for (String time : timeSlots) {
            String status = classMap.getOrDefault(time, "비어 있음");
            model.addRow(new Object[] { time, status });
        }

        applyTableColoring(); // ✅ 배경색 적용 함수 (이미 정의되어 있음)
    }




    // 🔸 예약 요청 처리

    private void handleReservationRequest() {
        int selectedRow = timeTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "시간대를 선택하세요.");
            return;
        }

        String time = timeTable.getValueAt(selectedRow, 0).toString();
        String year = (String) yearComboBox.getSelectedItem();
        String month = (String) monthComboBox.getSelectedItem();
        String day = (String) dayComboBox.getSelectedItem();
        String date = year + "-" + month + "-" + day;
        String room = lastSelectedRoom;
        String name = user.getName();  // 🔸 변경됨

        try {
            ReservationResult result = controller.processReservationRequest(date, time, room, name);

            switch (result) {
                case SUCCESS -> {
                    JOptionPane.showMessageDialog(this, "예약이 완료되었습니다.");
                    this.dispose(); // 현재 프레임 닫기
                    new ReservationFrame(user).setVisible(true); // 새로고침 효과
                }
                case TIME_OCCUPIED -> JOptionPane.showMessageDialog(this, "이미 예약된 시간입니다. 다른 시간을 선택해주세요.");
                case NOT_SELECTED -> JOptionPane.showMessageDialog(this, "시간대를 선택하세요.");
                case ERROR -> JOptionPane.showMessageDialog(this, "예약 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 통신 중 오류 발생: " + e.getMessage());
        }
    }



    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        yearComboBox = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        monthComboBox = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        dayComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        Btn911 = new javax.swing.JButton();
        Btn915 = new javax.swing.JButton();
        Btn916 = new javax.swing.JButton();
        Btn918 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        Btn908 = new javax.swing.JButton();
        Btn912 = new javax.swing.JButton();
        Btn913 = new javax.swing.JButton();
        Btn914 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        timeTable = new javax.swing.JTable();
        reservationBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("맑은 고딕", 1, 18)); // NOI18N
        jLabel1.setText("강의실 예약");

        jLabel2.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        jLabel2.setText("날짜 선택");

        yearComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2025", "2026", "2027", "2028", "2029", "2030" }));

        jLabel3.setText("년 :");

        jLabel4.setText("월 : ");

        monthComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" }));

        jLabel5.setText("일 : ");

        dayComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));

        jLabel6.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        jLabel6.setText("강의실 선택");

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));

        jLabel7.setText("실습실");

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        Btn911.setText("911");
        Btn911.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn911ActionPerformed(evt);
            }
        });
        jPanel2.add(Btn911);

        Btn915.setText("915");
        Btn915.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn915ActionPerformed(evt);
            }
        });
        jPanel2.add(Btn915);

        Btn916.setText("916");
        Btn916.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn916ActionPerformed(evt);
            }
        });
        jPanel2.add(Btn916);

        Btn918.setText("918");
        Btn918.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn918ActionPerformed(evt);
            }
        });
        jPanel2.add(Btn918);

        jLabel8.setText("이론실");

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        Btn908.setText("908");
        Btn908.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn908ActionPerformed(evt);
            }
        });
        jPanel4.add(Btn908);

        Btn912.setText("912");
        Btn912.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn912ActionPerformed(evt);
            }
        });
        jPanel4.add(Btn912);

        Btn913.setText("913");
        Btn913.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn913ActionPerformed(evt);
            }
        });
        jPanel4.add(Btn913);

        Btn914.setText("914");
        Btn914.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Btn914ActionPerformed(evt);
            }
        });
        jPanel4.add(Btn914);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel9.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        jLabel9.setText("시간표");

        timeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "시간", "상태"
            }
        ));
        jScrollPane1.setViewportView(timeTable);

        reservationBtn.setText("예약하기");
        reservationBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reservationBtnActionPerformed(evt);
            }
        });

        backBtn.setText("뒤로");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(backBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(252, 252, 252))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(21, 21, 21)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(monthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(268, 268, 268)
                        .addComponent(jLabel9))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(235, 235, 235)
                        .addComponent(reservationBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(99, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(yearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(monthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(dayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(reservationBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Btn911ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn911ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("911");
        lastSelectedRoom = "911";
    }//GEN-LAST:event_Btn911ActionPerformed

    private void Btn915ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn915ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("915");
        lastSelectedRoom = "915";
    }//GEN-LAST:event_Btn915ActionPerformed

    private void Btn916ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn916ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("916");
        lastSelectedRoom = "916";
    }//GEN-LAST:event_Btn916ActionPerformed

    private void Btn918ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn918ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("918");
        lastSelectedRoom = "918";
    }//GEN-LAST:event_Btn918ActionPerformed

    private void Btn908ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn908ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("908");
        lastSelectedRoom = "908";
    }//GEN-LAST:event_Btn908ActionPerformed

    private void Btn912ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn912ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("912");
        lastSelectedRoom = "912";
    }//GEN-LAST:event_Btn912ActionPerformed

    private void Btn913ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn913ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("913");
        lastSelectedRoom = "913";
    }//GEN-LAST:event_Btn913ActionPerformed

    private void Btn914ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Btn914ActionPerformed
        // TODO add your handling code here:
        handleRoomButtonClick("914");
        lastSelectedRoom = "914";
    }//GEN-LAST:event_Btn914ActionPerformed

    private void reservationBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reservationBtnActionPerformed
        // TODO add your handling code here:
        handleReservationRequest();
    }//GEN-LAST:event_reservationBtnActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        // TODO add your handling code here:
        this.dispose();
        ReservationMainFrame RMF = new ReservationMainFrame(user); // ✔ 수정
        RMF.setVisible(true);
    }//GEN-LAST:event_backBtnActionPerformed

 // 메인 임시삭제

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Btn908;
    private javax.swing.JButton Btn911;
    private javax.swing.JButton Btn912;
    private javax.swing.JButton Btn913;
    private javax.swing.JButton Btn914;
    private javax.swing.JButton Btn915;
    private javax.swing.JButton Btn916;
    private javax.swing.JButton Btn918;
    private javax.swing.JButton backBtn;
    private javax.swing.JComboBox<String> dayComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> monthComboBox;
    private javax.swing.JButton reservationBtn;
    private javax.swing.JTable timeTable;
    private javax.swing.JComboBox<String> yearComboBox;
    // End of variables declaration//GEN-END:variables

}
