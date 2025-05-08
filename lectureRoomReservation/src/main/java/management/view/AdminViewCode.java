/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.view;

/**
 *
 * @author limmi
 */
import javax.swing.*;
import java.awt.*;

public class AdminViewCode extends JFrame {
    public JList<String> roomList;
    public JButton btnAdd, btnDel, btnToggle;
    public JTable resTable;
    public JButton btnApp, btnRej, btnReload;

    public AdminViewCode() {
        setTitle("관리자 페이지 (코드 생성 스타일)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800,600);
        setLocationRelativeTo(null);

        // 레이아웃
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(250);

        // 좌측
        JPanel left = new JPanel(new BorderLayout());
        roomList = new JList<>();
        left.add(new JScrollPane(roomList), BorderLayout.CENTER);
        JPanel p1 = new JPanel();
        btnAdd = new JButton("추가"); p1.add(btnAdd);
        btnDel = new JButton("삭제"); p1.add(btnDel);
        btnToggle = new JButton("차단/해제"); p1.add(btnToggle);
        left.add(p1, BorderLayout.SOUTH);

        // 우측
        JPanel right = new JPanel(new BorderLayout());
        resTable = new JTable();
        right.add(new JScrollPane(resTable), BorderLayout.CENTER);
        JPanel p2 = new JPanel();
        btnApp    = new JButton("승인");   p2.add(btnApp);
        btnRej    = new JButton("거절");   p2.add(btnRej);
        btnReload = new JButton("새로고침"); p2.add(btnReload);
        right.add(p2, BorderLayout.SOUTH);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        add(split);
    }
}
