package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color HEADER_YELLOW    = new Color(255, 238, 140);
    private static final Color NAV_BG           = new Color(255, 255, 255);
    private static final Color BG_MAIN          = new Color(255, 255, 255);
    private static final Color BROWN            = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color POPUP_BG         = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = MainFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    // 로그인한 사용자 정보
    private String currentUserName = "게스트";
    private String currentUserId   = "";

    // 오늘 일정 영역
    private JLabel todayHeaderLabel;
    private JLabel todaySubLabel;
    private JPanel todayPanel;

    // ===============================
    // 생성자
    // ===============================
    public MainFrame() {
        Member m = LoginSession.getUser();
        if (m != null) {
            currentUserName = m.getName();
            currentUserId   = m.getHakbun();
        }

        initFrame();
        loadTodayScheduleFromDB();
    }

    public MainFrame(String userName, String userId) {
        this.currentUserName = userName;
        this.currentUserId   = userId;

        initFrame();
        loadTodayScheduleFromDB();
    }

    // ===============================
    // 프레임 & 기본 UI
    // ===============================
    private void initFrame() {
        setTitle("서울여대 꿀단지 - 메인");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initHeader();
        initNav();
        initContent();

        setVisible(true);
    }

    // 상단 헤더
    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 900, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MainFrame(currentUserName, currentUserId);
                dispose();
            }
        });
        headerPanel.add(logoLabel);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(450, 0, 430, 80);
        userInfoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("[" + currentUserName + "]님");
        nameLabel.setFont(uiFont.deriveFont(14f));
        nameLabel.setForeground(BROWN);
        userInfoPanel.add(nameLabel);

        JLabel logoutLabel = new JLabel(" | 로그아웃");
        logoutLabel.setFont(uiFont.deriveFont(14f));
        logoutLabel.setForeground(BROWN);
        logoutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLogoutPopup();
            }
        });

        userInfoPanel.add(logoutLabel);
        headerPanel.add(userInfoPanel);
    }

    // 네비게이션 바
    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 900, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220))
        );
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton btn = createNavButton(menu);
            navPanel.add(btn);
        }
    }

    // 메인 컨텐츠
    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 900, 520);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 벌 아이콘
        JLabel beeLabel = new JLabel();
        beeLabel.setBounds(60, 30, 60, 60);
        try {
            java.net.URL imgUrl = getClass().getResource("/img/login-bee.png");
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                beeLabel.setIcon(new ImageIcon(img));
            } else {
                beeLabel.setText("🐝");
                beeLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            }
        } catch (Exception e) {
            beeLabel.setText("🐝");
            beeLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        }
        contentPanel.add(beeLabel);

        // "일정 알리비" 타이틀
        JLabel titleLabel = new JLabel("일정 알리비");
        titleLabel.setFont(uiFont.deriveFont(28f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(130, 40, 250, 40);
        contentPanel.add(titleLabel);

        // 오늘 일정 패널
        todayPanel = new JPanel(null);
        todayPanel.setBounds(50, 100, 800, 170);
        todayPanel.setBackground(Color.WHITE);
        todayPanel.setBorder(new RoundedBorder(20, BROWN, 2));
        contentPanel.add(todayPanel);

        // 상단 노란 헤더
        JPanel todayHeader = new JPanel(null);
        todayHeader.setBounds(2, 2, 796, 50);
        todayHeader.setBackground(HIGHLIGHT_YELLOW);
        todayPanel.add(todayHeader);

        todayHeaderLabel = new JLabel("TODAY");
        todayHeaderLabel.setFont(uiFont.deriveFont(20f));
        todayHeaderLabel.setForeground(BROWN);
        todayHeaderLabel.setBounds(20, 15, 300, 25);
        todayHeader.add(todayHeaderLabel);

        // 오늘 일정 텍스트 (왼쪽 정렬 + 위쪽 정렬)
        todaySubLabel = new JLabel("");
        todaySubLabel.setFont(uiFont.deriveFont(20f));
        todaySubLabel.setForeground(BROWN);
        todaySubLabel.setHorizontalAlignment(SwingConstants.LEFT);
        todaySubLabel.setVerticalAlignment(SwingConstants.TOP);
        // 좌우 여백 40, 위에서 65부터 그리기
        todaySubLabel.setBounds(40, 65, 740, 90);
        todayPanel.add(todaySubLabel);
    }

    private void loadTodayScheduleFromDB() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M월 d일");
        todayHeaderLabel.setText(today.format(fmt) + " TODAY");

        String sql =
            "SELECT event_name, DATE_FORMAT(event_date, '%H:%i') AS start_time " +
            "FROM events " +
            "WHERE DATE(event_date) = CURDATE() " +
            "ORDER BY event_date";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean hasEvent = false;
            int eventCount = 0;
            StringBuilder html = new StringBuilder("<html>");

            while (rs.next()) {
                hasEvent = true;
                eventCount++;

                String time  = rs.getString("start_time");
                String title = rs.getString("event_name");

                // ● 동그라미 bullet + 줄 간격
                html.append("● ").append(time).append(" ").append(title).append("<br><br>");
            }

            if (!hasEvent) {
                todaySubLabel.setFont(uiFont.deriveFont(18f));
                todaySubLabel.setForeground(new Color(150, 150, 150));
                todaySubLabel.setHorizontalAlignment(SwingConstants.CENTER);
                todaySubLabel.setText("<html>오늘의 주요 일정이 없습니다.<br>편안한 하루 보내세요!</html>");

                // 일정 없으면 기본 높이
                todayPanel.setBounds(50, 100, 800, 170);
                todaySubLabel.setBounds(40, 65, 740, 90);

            } else {
                todaySubLabel.setFont(uiFont.deriveFont(16f));
                todaySubLabel.setForeground(BROWN);
                todaySubLabel.setHorizontalAlignment(SwingConstants.LEFT);
                html.append("</html>");
                todaySubLabel.setText(html.toString());

                // ✅ 행사 개수만큼 패널/라벨 높이 넉넉하게 늘리기 (상한 제거)
                int baseHeight = 120;   // 헤더 + 기본 여백
                int perLine    = 32;    // 행사 1개당 높이
                int newHeight  = baseHeight + eventCount * perLine;

                todayPanel.setBounds(
                        todayPanel.getX(),
                        todayPanel.getY(),
                        todayPanel.getWidth(),
                        newHeight
                );
                todaySubLabel.setBounds(40, 65, 740, newHeight - 80);
            }

        } catch (Exception e) {
            e.printStackTrace();
            todaySubLabel.setFont(uiFont.deriveFont(18f));
            todaySubLabel.setForeground(new Color(150, 150, 150));
            todaySubLabel.setHorizontalAlignment(SwingConstants.CENTER);
            todaySubLabel.setText("일정 정보를 불러오지 못했습니다.");
        }
    }


    // ===============================
    // 네비 버튼
    // ===============================
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setForeground(BROWN);
        btn.setBackground(NAV_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(HIGHLIGHT_YELLOW);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(NAV_BG);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                switch (text) {
                    case "물품대여":
                        new ItemListFrame();
                        dispose();
                        break;
                    case "과행사":
                        new EventListFrame();
                        dispose();
                        break;
                    case "공간대여":
                        new SpaceRentFrame();
                        dispose();
                        break;
                    case "빈 강의실":
                        new EmptyClassFrame();
                        dispose();
                        break;
                    case "커뮤니티":
                        new CommunityFrame();
                        dispose();
                        break;
                    case "마이페이지":
                        new MyPageFrame();
                        dispose();
                        break;
                }
            }
        });

        return btn;
    }

    // ===============================
    // 로그아웃 팝업
    // ===============================
    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msg = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msg.setFont(uiFont.deriveFont(18f));
        msg.setForeground(BROWN);
        msg.setBounds(20, 60, 360, 40);
        panel.add(msg);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();

            try {
                LoginSession.setUser(null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });

            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    // ===============================
    // 팝업 UI 공통
    // ===============================
    private JPanel createPopupPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
    }

    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ===============================
    // 둥근 Border
    // ===============================
    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    // 테스트용 main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
