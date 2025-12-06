package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beehub.DBUtil;

public class AdminSpaceManageFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color RED_CANCEL = new Color(255, 100, 100);
    private static final Color GRAY_TEXT = new Color(150, 150, 150);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = AdminSpaceManageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JPanel listPanel;
    private ArrayList<SpaceData> reserveList = new ArrayList<>();

    public AdminSpaceManageFrame() {
        setTitle("관리자 - 장소 대여 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        loadReservationsFromDB();
        refreshList();

        setVisible(true);
    }

    // ===========================
    // 🔹 DB에서 장소 예약 목록 불러오기
    // ===========================
    private void loadReservationsFromDB() {
        reserveList.clear();

        /*
         * DB 구조
         *  - space_info(space_id, building_name, room_name, min_people, max_people, ...)
         *  - space_reservation(reservation_id, space_id, reserve_date, time_slot, hakbun, status, created_at, ...)
         */
        String sql =
                "SELECT r.reservation_id, " +
                "       r.space_id, " +
                "       s.building_name, " +
                "       s.room_name, " +
                "       s.max_people, " +
                "       r.hakbun, " +
                "       r.reserve_date, " +
                "       r.time_slot, " +
                "       r.status " +
                "FROM space_reservation r " +
                "JOIN space_info s ON r.space_id = s.space_id " +
                "WHERE r.reserve_date >= CURDATE() " +
                "  AND s.room_type IN ('세미나실', '실습실') " +   // 🔹 세미나실/실습실만
                "ORDER BY r.reserve_date, r.time_slot";


        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                int spaceId       = rs.getInt("space_id");
                String building   = rs.getString("building_name");
                String roomName   = rs.getString("room_name");
                int maxPeople     = rs.getInt("max_people");

                String userId     = rs.getString("hakbun");
                String userName   = userId; // member 테이블 안 쓰고, 일단 학번 그대로 표시

                LocalDate date    = rs.getDate("reserve_date").toLocalDate();
                String timeSlot   = rs.getString("time_slot"); // 예: "09:00~10:00"
                String statusRaw  = rs.getString("status");

                LocalTime startTime;
                LocalTime endTime;

                try {
                    String[] parts = timeSlot.split("~");
                    startTime = LocalTime.parse(parts[0].trim());
                    endTime   = LocalTime.parse(parts[1].trim());
                } catch (Exception ex) {
                    // 혹시 포맷이 이상해도 화면은 뜨게 기본값
                    startTime = LocalTime.of(0, 0);
                    endTime   = LocalTime.of(0, 0);
                }

                String statusKor;
                if ("RESERVED".equalsIgnoreCase(statusRaw)) {
                    statusKor = "예약중";
                } else if ("CANCELED".equalsIgnoreCase(statusRaw)) {
                    statusKor = "취소됨";
                } else if ("NO_SHOW".equalsIgnoreCase(statusRaw)) {
                    statusKor = "미입실 취소";
                } else {
                    statusKor = statusRaw;
                }

                reserveList.add(
                        new SpaceData(
                                reservationId,
                                spaceId,
                                building,
                                roomName,
                                userId,
                                userName,
                                date,
                                startTime,
                                endTime,
                                timeSlot,
                                maxPeople,
                                statusKor,
                                statusRaw
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showMsgPopup("DB 오류", "예약 목록을 불러오는 중 오류가 발생했습니다...\n" + e.getMessage());
        }
    }

    private void initUI() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel titleLabel = new JLabel("장소 대여 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(titleLabel);

        JButton homeBtn = new JButton("<-메인으로");
        homeBtn.setFont(uiFont.deriveFont(14f));
        homeBtn.setBackground(BROWN);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBounds(650, 25, 110, 35);
        homeBtn.setBorder(new RoundedBorder(15, BROWN));
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> {
            new AdminMainFrame();
            dispose();
        });
        headerPanel.add(homeBtn);

        listPanel = new JPanel();
        listPanel.setLayout(null);
        listPanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBounds(30, 100, 730, 440);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }

    private void refreshList() {
        listPanel.removeAll();
        int yPos = 10;

        for (SpaceData data : reserveList) {
            JPanel card = createSpaceCard(data);
            card.setBounds(10, yPos, 690, 110);
            listPanel.add(card);
            yPos += 120;
        }

        listPanel.setPreferredSize(new Dimension(690, yPos));
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createSpaceCard(SpaceData data) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(15, Color.LIGHT_GRAY));

        JLabel roomLabel = new JLabel("[" + data.buildingName + "] " + data.roomName);
        roomLabel.setFont(uiFont.deriveFont(20f));
        roomLabel.setForeground(BROWN);
        roomLabel.setBounds(20, 15, 350, 30);
        panel.add(roomLabel);

        int warn = PenaltyManager.getWarningCount(data.userId);
        String statusText = data.statusKor;
        if (warn > 0) statusText += " (경고 " + warn + "회)";

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(uiFont.deriveFont(14f));
        statusLabel.setForeground(
                data.statusKor.equals("취소됨") || data.statusKor.equals("미입실 취소")
                        ? RED_CANCEL
                        : new Color(100, 180, 100)
        );
        statusLabel.setBounds(380, 20, 250, 20);
        panel.add(statusLabel);

        JLabel userLabel = new JLabel("예약자: " + data.userId + " | " + data.userName +
                " (정원 " + data.peopleCount + "명)");
        userLabel.setFont(uiFont.deriveFont(14f));
        userLabel.setForeground(GRAY_TEXT);
        userLabel.setBounds(20, 50, 450, 20);
        panel.add(userLabel);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String timeStr = data.date.format(dtf) + "  " + data.timeSlot;
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(uiFont.deriveFont(16f));
        timeLabel.setForeground(BROWN);
        timeLabel.setBounds(20, 75, 400, 25);
        panel.add(timeLabel);

        JButton cancelBtn = new JButton();

        if (data.statusKor.equals("취소됨") || data.statusKor.equals("미입실 취소")) {
            cancelBtn.setText("취소완료");
            cancelBtn.setEnabled(false);
            cancelBtn.setBackground(new Color(240, 240, 240));
            cancelBtn.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        } else {
            cancelBtn.setText("미입실 취소");
            cancelBtn.setBackground(RED_CANCEL);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setBorder(new RoundedBorder(10, RED_CANCEL));

            cancelBtn.addActionListener(e -> {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime reserveStart = LocalDateTime.of(data.date, data.startTime);
                LocalDateTime cancelAllowedTime = reserveStart.plusMinutes(10);

                if (now.isBefore(cancelAllowedTime)) {
                    String msg = "아직 미입실 처리를 할 수 없습니다.\n" +
                            "입장 시간 10분 후 (" +
                            cancelAllowedTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                            ") 부터\n취소 가능합니다.";
                    showMsgPopup("취소 불가", msg);
                    return;
                }

                boolean confirm = showConfirmPopup(
                        "패널티 부여",
                        "[" + data.userName + "]님 미입실로 취소하시겠습니까?\n(누적 시 패널티 부여)"
                );

                if (confirm) {
                    boolean dbOk = updateReservationAsNoShow(data);
                    if (!dbOk) {
                        showMsgPopup("DB 오류", "예약 취소 처리 중 오류가 발생했습니다.");
                        return;
                    }

                    data.statusKor = "미입실 취소";
                    data.statusRaw = "NO_SHOW";

                    PenaltyManager.addWarning(data.userId);

                    if (PenaltyManager.isBanned(data.userId)) {
                        showMsgPopup("예약 정지", "🚫 경고 2회 누적!\n해당 회원은 7일간 예약이 정지되었습니다.");
                    } else {
                        int currentWarn = PenaltyManager.getWarningCount(data.userId);
                        showMsgPopup("경고 부여", "경고가 부여되었습니다.\n(현재 누적: " + currentWarn + "회)");
                    }

                    refreshList();
                }
            });
        }

        cancelBtn.setFont(uiFont.deriveFont(14f));
        cancelBtn.setBounds(530, 35, 130, 40);
        cancelBtn.setFocusPainted(false);
        panel.add(cancelBtn);

        return panel;
    }

    // ================================
    // 🔹 DB에 미입실 취소 상태 반영
    // ================================
    private boolean updateReservationAsNoShow(SpaceData data) {
        // PK인 reservation_id 로 단일 행 업데이트
        String sql =
                "UPDATE space_reservation " +
                "SET status = 'NO_SHOW' " +
                "WHERE reservation_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, data.reservationId);

            int updated = pstmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // 🎨 팝업 메소드들
    // ==========================================
    private void showMsgPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        String[] lines = msg.split("\n");
        int yPos = lines.length == 1 ? 80 : 60;
        for (String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, yPos, 360, 30);
            panel.add(l);
            yPos += 30;
        }

        JButton okBtn = new JButton("확인");
        okBtn.setFont(uiFont.deriveFont(16f));
        okBtn.setBackground(BROWN);
        okBtn.setForeground(Color.WHITE);
        okBtn.setBounds(135, 170, 130, 45);
        okBtn.setBorder(new RoundedBorder(15, BROWN));
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private boolean showConfirmPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));
        final boolean[] result = {false};

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        String[] lines = msg.split("\n");
        int yPos = lines.length == 1 ? 80 : 60;
        for (String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, yPos, 360, 30);
            panel.add(l);
            yPos += 30;
        }

        JButton yesBtn = new JButton("네");
        yesBtn.setBounds(60, 160, 120, 45);
        yesBtn.setBackground(BROWN);
        yesBtn.setForeground(Color.WHITE);
        yesBtn.setFont(uiFont.deriveFont(16f));
        yesBtn.setBorder(new RoundedBorder(15, BROWN));
        yesBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setBounds(220, 160, 120, 45);
        noBtn.setBackground(BROWN);
        noBtn.setForeground(Color.WHITE);
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBorder(new RoundedBorder(15, BROWN));
        noBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        panel.add(noBtn);

        dialog.setVisible(true);
        return result[0];
    }

    // ===========================
    // DTO
    // ===========================
    class SpaceData {
        int reservationId;
        int spaceId;
        String buildingName;
        String roomName;
        String userId;
        String userName;
        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        String timeSlot;
        int peopleCount;
        String statusKor;
        String statusRaw;

        public SpaceData(int reservationId,
                         int spaceId,
                         String buildingName,
                         String roomName,
                         String userId,
                         String userName,
                         LocalDate date,
                         LocalTime startTime,
                         LocalTime endTime,
                         String timeSlot,
                         int peopleCount,
                         String statusKor,
                         String statusRaw) {
            this.reservationId = reservationId;
            this.spaceId = spaceId;
            this.buildingName = buildingName;
            this.roomName = roomName;
            this.userId = userId;
            this.userName = userName;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.timeSlot = timeSlot;
            this.peopleCount = peopleCount;
            this.statusKor = statusKor;
            this.statusRaw = statusRaw;
        }
    }

    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;

        public RoundedBorder(int r, Color c) {
            radius = r;
            color = c;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }
}
