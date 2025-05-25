/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import client.ClientMain;
import common.Message;
import common.RequestType;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.event.ListSelectionListener;
import java.time.DayOfWeek;
import java.util.List;
import javax.swing.JOptionPane;

import common.Reservation;
import common.Room;
import common.ScheduleEntry;
import common.User;

import model.ReservationTableModel;
import model.RoomTableModel;
import model.ScheduleTableModel;
import controller.AdminReservationController;
import controller.AdminRoomController;


/**
 *
 * @author limmi
 */
public class AdminReservationFrame extends javax.swing.JFrame {

    /**
     * Creates new form AdminReservationFrame
     */
        private User user;
        private AdminReservationController reservationController;
        private AdminRoomController roomController;

        public AdminReservationFrame(User user) {
            this.user = user;
            initComponents();
            this.reservationController = new AdminReservationController(this);
            this.roomController = new AdminRoomController(this);
            setLocationRelativeTo(null);
        }

        public AdminReservationFrame() {
            initComponents();  // (1) UI 컴포넌트 초기화

            // (2) 두 컨트롤러를 this 프레임에 DI
            try {
                reservationController = new AdminReservationController(this);
                roomController        = new AdminRoomController(this);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "화면 초기화 중 오류가 발생했습니다:\n" + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    
    // ─── 예약 목록 버튼 리스너 등록 ──────────────────────────────────────────
    // 승인
    public void addApproveListener(ActionListener l) {
        approveButton.addActionListener(l);
    }
    // 거절
    public void addRejectListener(ActionListener l) {
        rejectButton.addActionListener(l);
    }
    // 새로고침
    public void addRefreshListener(ActionListener l) {
        refreshButton.addActionListener(l);
    }
    
    // ─── 강의실 차단/해제 버튼 리스너 등록 ──────────────────────────────────────────
    public void addBlockListener(ActionListener l) {
        blockButton.addActionListener(l);   // "차단하기" 버튼
    }
    public void addUnblockListener(ActionListener l) {
        unblockButton.addActionListener(l);   // "차단해제" 버튼
    }
    

    // ─── 예약 목록 조회/변경 ────────────────────────────────────────────────
    // 선택된 테이블 행 인덱스
    public int getSelectedReservationIndex() {
        return reservationTable.getSelectedRow();
    }
    // 해당 인덱스의 Reservation 객체 꺼내기
    public Reservation getReservationAt(int idx) {
        return ((ReservationTableModel)reservationTable.getModel())
                .getReservationAt(idx);
    }
    // 컨트롤러가 넘긴 데이터로 JTable 갱신
    public void setReservationTable(List<Reservation> data) {
        reservationTable.setModel(new ReservationTableModel(data));
    }

    // ─── 강의실 목록 선택 리스너 ───────────────────────────────────────────
    public void addRoomSelectionListener(ListSelectionListener l) {
        roomTable.getSelectionModel().addListSelectionListener(l);
    }
    
    public int getSelectedRoomIndex() {
        return roomTable.getSelectedRow();
    }

    // ─── 요일 필터 콤보박스 리스너 ─────────────────────────────────────────
    public void addDayFilterListener(ActionListener l) {
        dayCombo.addActionListener(l);
    }

    // ─── 스케줄 등록 버튼 리스너 ─────────────────────────────────────────
    public void addRegisterScheduleListener(ActionListener l) {
        registerButton.addActionListener(l);
    }

    // ─── 강의실 정보 조회 ────────────────────────────────────────────────
    // 선택된 강의실 ID
    public String getSelectedRoomId() {
        int r = roomTable.getSelectedRow();
        return (String)roomTable.getValueAt(r, 0);
    }
    // 선택된 요일 → DayOfWeek
    public DayOfWeek getSelectedDay() {
        switch ((String)dayCombo.getSelectedItem()) {
            case "월": return DayOfWeek.MONDAY;
            case "화": return DayOfWeek.TUESDAY;
            case "수": return DayOfWeek.WEDNESDAY;
            case "목": return DayOfWeek.THURSDAY;
            case "금": return DayOfWeek.FRIDAY;
            default:   return DayOfWeek.MONDAY;
        }
    }
    // 선택된 시간 문자열
    public String getSelectedTime() {
        return (String)timeCombo.getSelectedItem();
    }
    public String getInputProfessor() {
        return professorField1.getText().trim();
    }

    public String getInputCourse() {
        return classField2.getText().trim();
    }
    // 불가능 사유 텍스트
    public String getReasonText() {
        return reasonField.getText();
    }
    
    // ─── 선택된 강의실 꺼내기 ───────────────────────────────────────────
    public Room getRoomAt(int idx) {
        return ((RoomTableModel)roomTable.getModel()).getRoomAt(idx);
    }


    // ─── 테이블 데이터 갱신 ───────────────────────────────────────────────
    public void setRoomTable(List<Room> rooms) {
        roomTable.setModel(new RoomTableModel(rooms));
    }
    public void setScheduleTable(List<ScheduleEntry> sch) {
        scheduleTable.setModel(new ScheduleTableModel(sch));
    }
    
    public void setRoomDetails(String roomId, Room.Availability avail, String reason) {
        // 텍스트 필드에 강의실 ID 출력
        textField1.setText(roomId);
    }
    
    public javax.swing.JTable getRoomTable() {
        return roomTable;
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        approveButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        reservationTable = new javax.swing.JTable();
        logoutButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        roomTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        textField1 = new java.awt.TextField();
        dayCombo = new javax.swing.JComboBox<>();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        label3 = new java.awt.Label();
        timeCombo = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        scheduleTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        registerButton = new javax.swing.JButton();
        label5 = new java.awt.Label();
        label6 = new java.awt.Label();
        professorField1 = new java.awt.TextField();
        classField2 = new java.awt.TextField();
        unblockButton = new javax.swing.JButton();
        blockButton = new javax.swing.JButton();
        reasonField = new java.awt.TextField();
        label4 = new java.awt.Label();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("맑은 고딕", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("예약 목록");

        approveButton.setText("승인");

        rejectButton.setText("거절");

        refreshButton.setText("새로고침");

        reservationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "예약 번호", "날짜", "시간", "강의실", "이름", "상태"
            }
        ));
        jScrollPane1.setViewportView(reservationTable);

        logoutButton.setText("로그아웃");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 909, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(247, 247, 247)
                        .addComponent(approveButton)
                        .addGap(92, 92, 92)
                        .addComponent(rejectButton)
                        .addGap(89, 89, 89)
                        .addComponent(refreshButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(411, 411, 411)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(logoutButton)
                .addGap(31, 31, 31))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(logoutButton)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(36, 36, 36)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshButton)
                    .addComponent(rejectButton)
                    .addComponent(approveButton))
                .addGap(58, 58, 58))
        );

        jTabbedPane1.addTab("예약 목록", jPanel1);

        roomTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "강의실", "상태"
            }
        ));
        jScrollPane4.setViewportView(roomTable);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("맑은 고딕", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("강의실 일정 관리");

        textField1.setText("강의실");

        dayCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "월", "화", "수", "목", "금", "토", "일" }));

        label1.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        label1.setText("강의실 :");

        label2.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        label2.setText("요일 :");

        label3.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        label3.setText("시간 :");

        timeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "09:00~09:50", "10:00~10:50", "11:00~11:50", "12:00~12:50", "13:00~13:50", "14:00~14:50", "15:00~15:50", "16:00~16:50", "17:00~17:50" }));

        scheduleTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "시간", "월", "화", "수", "목", "금", "담당 교수"
            }
        ));
        jScrollPane5.setViewportView(scheduleTable);

        jLabel3.setFont(new java.awt.Font("맑은 고딕", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("강의실 일정");

        registerButton.setText("등록하기");

        label5.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        label5.setText("교수명 :");

        label6.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        label6.setText("과목명 :");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(registerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(288, 288, 288))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 33, Short.MAX_VALUE)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 629, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dayCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(115, 115, 115)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(classField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(professorField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(timeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel2)
                .addGap(29, 29, 29)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dayCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(2, 2, 2)
                            .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(11, 11, 11)
                            .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addComponent(professorField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(classField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(timeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(registerButton)
                .addGap(20, 20, 20))
        );

        unblockButton.setText("차단해제");

        blockButton.setText("차단하기");

        label4.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        label4.setText("사유 :");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(blockButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(unblockButton)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reasonField, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(reasonField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unblockButton)
                    .addComponent(blockButton))
                .addGap(23, 23, 23))
        );

        jTabbedPane1.addTab("강의실 관리", jPanel2);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        // TODO add your handling code here:
        try {
            Message req = new Message();
            req.setDomain("user");
            req.setType(RequestType.DISCONNECT);
            req.setPayload(null);
            ClientMain.out.writeObject(req);
            ClientMain.out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2) 현재 창 닫고 로그인 화면으로 돌아가기
        this.dispose();
        new LoginView().setVisible(true);
    }//GEN-LAST:event_logoutButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                AdminReservationFrame frame = new AdminReservationFrame();
                // 컨트롤러 연결
                new AdminReservationController(frame);
                new AdminRoomController(frame);
                frame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton approveButton;
    private javax.swing.JButton blockButton;
    private java.awt.TextField classField2;
    private javax.swing.JComboBox<String> dayCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private java.awt.Label label1;
    private java.awt.Label label2;
    private java.awt.Label label3;
    private java.awt.Label label4;
    private java.awt.Label label5;
    private java.awt.Label label6;
    private javax.swing.JButton logoutButton;
    private java.awt.TextField professorField1;
    private java.awt.TextField reasonField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton registerButton;
    private javax.swing.JButton rejectButton;
    private javax.swing.JTable reservationTable;
    private javax.swing.JTable roomTable;
    private javax.swing.JTable scheduleTable;
    private java.awt.TextField textField1;
    private javax.swing.JComboBox<String> timeCombo;
    private javax.swing.JButton unblockButton;
    // End of variables declaration//GEN-END:variables
}
