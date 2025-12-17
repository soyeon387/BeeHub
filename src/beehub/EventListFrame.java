package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import council.EventManager;
import council.EventManager.EventData;

public class EventListFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    
    // ✅ 상태별 색상 정의
    private static final Color GREEN_PROGRESS = new Color(180, 230, 180); // 진행 중, 신청 중
    private static final Color ORANGE_CLOSED  = new Color(255, 200, 180); // 종료
    private static final Color GRAY_BEFORE    = new Color(225, 225, 225); // 진행 전, 신청 전 (회색)
    private static final Color RED_CLOSED     = new Color(255, 160, 160); // 신청 마감 (빨간색)
    
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = EventListFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private String userName = "사용자";
    private String userId = "";
    private int userPoint = 100;
    private JComboBox<String> councilDropdown;
    private JPanel eventListPanel;

    private final String[] councils = {
        "전체", "총학생회", "───────────────",
        "인문대학", "글로벌ICT인문융합학부", "국어국문학과", "영어영문학과", "중어중문학과", "일어일문학과", "사학과", "기독교학과",
        "───────────────",
        "사회과학대학", "경제학과", "문헌정보학과", "사회복지학과", "아동학과", "행정학과", "언론영상학부", "심리.인지과학학부", "스포츠운동과학과",
        "───────────────",
        "과학기술융합대학", "수학과", "화학과", "생명환경공학과", "바이오헬스융합학과", "원예생명조경학과", "식품공학과", "식품영양학과",
        "───────────────",
        "미래산업융합대학", "경영학과", "패션산업학과", "디지털미디어학과", "지능정보보호학부", "소프트웨어융합학과", "데이터사이언스학과", "산업디자인학과"
    };

    public EventListFrame() {
        setTitle("서울여대 꿀단지 - 과행사");
        setSize(800, 600);

        User currentUser = UserManager.getCurrentUser();
        if(currentUser != null) {
            userName = currentUser.getName();
            userId = currentUser.getId();
            userPoint = currentUser.getPoints();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        loadEvents(); 
        setVisible(true);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(logoLabel);
        
        logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        logoLabel.addMouseListener(new MouseAdapter() {      
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); 
                new MainFrame(); 
            }
        });

        JLabel jarIcon = new JLabel();
        jarIcon.setBounds(310, 25, 40, 40);
        headerPanel.add(jarIcon);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfoText = new JLabel("[" + userName + "]님 | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });
        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);

        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 1);
            navPanel.add(menuBtn);
        }

        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        JLabel councilLabel = new JLabel("학생회");
        councilLabel.setFont(uiFont.deriveFont(Font.BOLD, 20f));
        councilLabel.setForeground(BROWN);
        councilLabel.setBounds(50, 20, 100, 30);
        contentPanel.add(councilLabel);

        JLabel dropdownIcon = new JLabel("▼");
        dropdownIcon.setFont(uiFont.deriveFont(14f));
        dropdownIcon.setForeground(new Color(255, 180, 50));
        dropdownIcon.setBounds(140, 25, 20, 20);
        contentPanel.add(dropdownIcon);

        councilDropdown = new JComboBox<>(councils);
        councilDropdown.setFont(uiFont.deriveFont(14f));
        councilDropdown.setBounds(50, 60, 270, 35);
        councilDropdown.setBackground(Color.WHITE);
        councilDropdown.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        councilDropdown.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && value.toString().startsWith("───")) {
                    setEnabled(false);
                    setBackground(new Color(240, 240, 240));
                }
                return this;
            }
        });
        contentPanel.add(councilDropdown);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        searchIcon.setForeground(BROWN);
        searchIcon.setBounds(330, 62, 30, 30);
        searchIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { loadEvents(); }
        });
        contentPanel.add(searchIcon);

        eventListPanel = new JPanel();
        eventListPanel.setLayout(null);
        eventListPanel.setBackground(BG_MAIN);
        eventListPanel.setPreferredSize(new Dimension(750, 500));

        JScrollPane scrollPane = new JScrollPane(eventListPanel);
        scrollPane.setBounds(25, 120, 750, 330);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel.add(scrollPane);
    }

    private void loadEvents() {
        String selectedCouncil = (String) councilDropdown.getSelectedItem();
        if (selectedCouncil == null || selectedCouncil.startsWith("───")) {
            selectedCouncil = "전체";
        }

        String filterTarget = null; 
        if (!"전체".equals(selectedCouncil)) {
            if ("총학생회".equals(selectedCouncil)) {
                filterTarget = "ALL";          
            } else {
                filterTarget = selectedCouncil.trim();   
            }
        }

        eventListPanel.removeAll();
        List<EventData> events = EventManager.getAllEvents();

        int yPos = 10;
        int count = 0;

        for (EventData event : events) {

            if ("삭제".equals(event.status)) continue;

            String status = computeEventStatus(event);
            event.status = status;

            if ("종료".equals(status)) continue;

            if (filterTarget != null) {                   
                String target = event.targetDept != null ? event.targetDept.trim() : "";
                if (target.isEmpty()) continue;
                if (!target.equals(filterTarget)) continue;
            }

            addEventCard(event, yPos);
            yPos += 140;
            count++;
        }

        if (count == 0) {
            JLabel noResult = new JLabel("해당하는 행사가 없습니다.", SwingConstants.CENTER);
            noResult.setFont(uiFont.deriveFont(20f));
            noResult.setForeground(new Color(150, 150, 150));
            noResult.setBounds(0, 100, 750, 50);
            eventListPanel.add(noResult);
        }

        eventListPanel.setPreferredSize(new Dimension(750, Math.max(yPos, 320)));
        eventListPanel.revalidate();
        eventListPanel.repaint();
    }

    private String computeEventStatus(EventData e) {
        if ("삭제".equals(e.status)) return "삭제";

        LocalDateTime now = LocalDateTime.now();

        String type = (e.eventType != null) ? e.eventType.trim() : "";
        boolean isSnack = false;
        boolean isActivity = false;

        if (!type.isEmpty()) {
            if (type.equalsIgnoreCase("SNACK") || type.contains("간식")) {
                isSnack = true;
            } else if (type.equalsIgnoreCase("ACTIVITY")
                    || type.contains("참여형") || type.contains("과행사")) {
                isActivity = true;
            }
        }

        if (!isSnack && !isActivity) isActivity = true;

        LocalDateTime eventTime  = e.date;
        LocalDateTime applyStart = e.applyStart;
        LocalDateTime applyEnd   = e.applyEnd;

        int total   = e.totalCount;
        int current = e.currentCount;

        // ✅ 간식행사: 기존 로직 유지 (신청 시작~종료시간 기준 + 정원 마감)
        if (isSnack) {
            LocalDateTime snackStart = (applyStart != null) ? applyStart : eventTime;
            LocalDateTime snackEnd   = applyEnd;

            if (snackEnd == null && snackStart != null) {
                snackEnd = snackStart.plusHours(1);
            }

            if (snackEnd != null && now.isAfter(snackEnd)) {
                return "종료";
            }
            if (snackStart != null && now.isBefore(snackStart)) {
                return "진행 전";
            }
            if (total > 0 && current >= total) {
                return "신청 마감";
            } else {
                return "진행 중";
            }
        }

        // ✅ 참여형(과행사): "행사일시(eventTime)"로 종료시키지 말고, "신청기간(applyStart~applyEnd)" 기준
        // 신청 시작이 null이면 eventTime을 시작으로 대체(기존 데이터 호환)
        LocalDateTime start = (applyStart != null) ? applyStart : eventTime;
        LocalDateTime end   = applyEnd;

        if (start != null && now.isBefore(start)) {
            return "신청 전";
        }

        // 18:00까지 신청 가능하게 하려면 end "포함" 처리(= end와 같은 시각은 마감 아님)
        if (end != null && now.isAfter(end)) {
            return "신청 마감";
        }

        if (total > 0 && current >= total) {
            return "신청 마감";
        }

        return "신청 중";
    }


    private void addEventCard(EventData event, int y) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBounds(10, y, 730, 120);
        card.setBackground(Color.WHITE);
        card.setBorder(new RoundedBorder(15, new Color(200, 200, 200), 2));

        // 상태 계산
        String status = event.status != null ? event.status : computeEventStatus(event);
        JLabel typeLabel = new JLabel(status);
        typeLabel.setFont(uiFont.deriveFont(Font.BOLD, 13f));
        typeLabel.setForeground(BROWN);
        typeLabel.setBounds(20, 20, 100, 25);
        typeLabel.setOpaque(true);

        // ✅ 상태별 색상 적용 (수정됨)
        Color statusColor;
        switch (status) {
            case "진행 전":
            case "신청 전":
                statusColor = GRAY_BEFORE; // 회색
                break;
            case "신청 마감":
            case "신청마감":
                statusColor = RED_CLOSED; // 빨간색
                break;
            case "종료":
                statusColor = ORANGE_CLOSED; // 기존 오렌지
                break;
            default:
                statusColor = GREEN_PROGRESS; // 진행 중, 신청 중 등
                break;
        }
        typeLabel.setBackground(statusColor);
        typeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(typeLabel);

        // 제목
        JLabel nameLabel = new JLabel(event.title);
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(20, 55, 400, 35);
        card.add(nameLabel);

        // 남은 인원
        int remaining = event.totalCount - event.currentCount;
        if (remaining < 0) remaining = 0;

        JLabel slotsLabel = new JLabel("남은 인원 : " + remaining + "명");
        slotsLabel.setFont(uiFont.deriveFont(18f));
        slotsLabel.setForeground(new Color(100, 100, 100));
        slotsLabel.setBounds(550, 55, 180, 30);
        card.add(slotsLabel);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new EventDetailFrame(event);
                dispose();
            }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(250, 250, 250)); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        eventListPanel.add(card);
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setForeground(BROWN);
        btn.setBackground(isActive ? HIGHLIGHT_YELLOW : NAV_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (!isActive) {
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("과행사")) return;
                    if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                    else if (text.equals("공간대여")) { new SpaceRentFrame(); dispose(); }
                    else if (text.equals("빈 강의실")) { new EmptyClassFrame(); dispose(); }
                    else if (text.equals("마이페이지")) { new MyPageFrame(); dispose(); }
                    else { showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다."); }
                }
            });
        }
        return btn;
    }

    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
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
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 80, 360, 30);
        panel.add(msgLabel);

        JButton okBtn = new JButton("확인");
        okBtn.setFont(uiFont.deriveFont(16f));
        okBtn.setBackground(BROWN);
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel() {
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
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = new JButton("네");
        yesBtn.setFont(uiFont.deriveFont(16f));
        yesBtn.setBackground(BROWN);
        yesBtn.setForeground(Color.WHITE);
        yesBtn.setFocusPainted(false);
        yesBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            new LoginFrame();
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBackground(BROWN);
        noBtn.setForeground(Color.WHITE);
        noBtn.setFocusPainted(false);
        noBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;
        public RoundedBorder(int r, Color c, int t) {
            radius = r; color = c; thickness = t;
        }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EventListFrame::new);
    }
}