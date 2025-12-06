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

    private static final Color HEADER_YELLOW    = new Color(255, 238, 140);
    private static final Color NAV_BG           = new Color(255, 255, 255);
    private static final Color BG_MAIN          = new Color(255, 255, 255);
    private static final Color BROWN            = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color GREEN_PROGRESS   = new Color(180, 230, 180);
    private static final Color ORANGE_CLOSED    = new Color(255, 200, 180);
    private static final Color POPUP_BG         = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = EventListFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private String userName  = "사용자";
    private String userMajor = "";          // 🔹 로그인한 유저의 전공 저장

    private JPanel eventListPanel;
    private JTextField searchField;

    public EventListFrame() {
        setTitle("서울여대 꿀단지 - 과행사");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 🔹 로그인 정보에서 이름 + 전공 가져오기
        User currentUser = UserManager.getCurrentUser();
        if (currentUser != null) {
            userName  = currentUser.getName();
            // 학과 정보
            if (currentUser.getDept() != null) {
                userMajor = currentUser.getDept();
            }
            // getDept() 대신 getMajor() 쓰는 구조라면 거기에 맞게 변경
        }

        initUI();
        loadEvents();
        setVisible(true);
    }

    private void initUI() {
        // --- 헤더 ---
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(logoLabel);

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

        // --- 네비게이션 ---
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

        // --- 콘텐츠 영역 ---
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        searchField = new JTextField();
        searchField.setFont(uiFont.deriveFont(16f));
        searchField.setBounds(200, 20, 350, 40);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.addActionListener(e -> searchEvents());
        contentPanel.add(searchField);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        searchIcon.setBounds(560, 25, 30, 30);
        searchIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { searchEvents(); }
        });
        contentPanel.add(searchIcon);

        eventListPanel = new JPanel(null);
        eventListPanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(eventListPanel);
        scrollPane.setBounds(25, 80, 750, 370);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        contentPanel.add(scrollPane);
    }

    /** 🔹 학과 필터 적용해서 행사 목록 불러오기 */
    private void loadEvents() {
        eventListPanel.removeAll();

        List<EventData> events = EventManager.getAllEvents();

        int yPos = 10;
        for (EventData e : events) {

            // ✅ 내 학과 + 전체학과만 보이도록 필터
            if (!canSeeEvent(e)) continue;

            addEventCard(e, yPos);
            yPos += 110;
        }

        eventListPanel.setPreferredSize(new Dimension(730, Math.max(yPos, 350)));
        eventListPanel.revalidate();
        eventListPanel.repaint();
    }

    /** 🔹 검색에도 학과 필터 같이 적용 */
    private void searchEvents() {
        String keyword = searchField.getText().trim();

        List<EventData> events = EventManager.getAllEvents();

        eventListPanel.removeAll();
        int yPos = 10;
        boolean found = false;

        for (EventData e : events) {

            if (!canSeeEvent(e)) continue;   // 학과 필터

            if (keyword.isEmpty() || e.title.contains(keyword)) {
                addEventCard(e, yPos);
                yPos += 110;
                found = true;
            }
        }

        if (!found) {
            JLabel noResult = new JLabel("검색 결과가 없습니다.", SwingConstants.CENTER);
            noResult.setFont(uiFont.deriveFont(20f));
            noResult.setForeground(new Color(150, 150, 150));
            noResult.setBounds(0, 100, 750, 50);
            eventListPanel.add(noResult);
        }

        eventListPanel.setPreferredSize(new Dimension(730, Math.max(yPos, 350)));
        eventListPanel.revalidate();
        eventListPanel.repaint();
    }

    private boolean canSeeEvent(EventData e) {
        String target = e.targetDept;   // 필드 이름에 맞게!

        // 1) 전체 대상 처리
        if (target == null) return true;
        String t = target.trim();
        if (t.isEmpty()) return true;
        if ("ALL".equalsIgnoreCase(t)) return true;
        if (t.contains("전체")) return true;  // "전체학과", "전체 학과", "전체학과 대상" 등 다 통과

        // 2) 내 학과 정보 없으면 일단 다 보여주기
        if (userMajor == null || userMajor.trim().isEmpty()) return true;

        // 3) 그 외에는 정확히 같은 학과만
        return t.equals(userMajor.trim());
    }

    private void addEventCard(EventData e, int y) {
        JPanel card = new JPanel(null);
        card.setBounds(10, y, 730, 100);
        card.setBackground(Color.WHITE);
        card.setBorder(new RoundedBorder(15, new Color(200, 200, 200), 2));

        // ✅ 상태 계산 (세부화면과 동일)
        String status = computeEventStatus(e);
        e.status = status;

        Color statusColor =
                ("종료".equals(status) || "신청마감".equals(status))
                        ? ORANGE_CLOSED
                        : GREEN_PROGRESS;

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 13f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(20, 15, 90, 25);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(statusColor);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel);

        JLabel titleLabel = new JLabel(e.title);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBounds(130, 15, 400, 30);
        card.add(titleLabel);

        String dateStr = e.date.format(EventManager.DATE_FMT);
        JLabel dateLabel = new JLabel("일시 : " + dateStr);
        dateLabel.setFont(uiFont.deriveFont(14f));
        dateLabel.setForeground(new Color(80, 80, 80));
        dateLabel.setBounds(130, 50, 250, 20);
        card.add(dateLabel);

        JLabel locLabel = new JLabel("장소 : " + e.location);
        locLabel.setFont(uiFont.deriveFont(14f));
        locLabel.setForeground(new Color(80, 80, 80));
        locLabel.setBounds(130, 70, 250, 20);
        card.add(locLabel);

        JLabel slotsLabel = new JLabel("신청 : " + e.currentCount + " / " + e.totalCount + "명");
        slotsLabel.setFont(uiFont.deriveFont(14f));
        slotsLabel.setForeground(new Color(100, 100, 100));
        slotsLabel.setBounds(520, 40, 180, 20);
        card.add(slotsLabel);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                new EventDetailFrame(e);
                dispose();
            }
            public void mouseEntered(MouseEvent ev) { card.setBackground(new Color(250, 250, 250)); }
            public void mouseExited (MouseEvent ev) { card.setBackground(Color.WHITE); }
        });

        eventListPanel.add(card);
    }

    // 🔹 지난 행사면 무조건 "종료"
    private String computeEventStatus(EventData e) {
        String base = (e.status == null || e.status.isEmpty()) ? "진행중" : e.status;

        if (e.date != null) {
            LocalDateTime now = LocalDateTime.now();
            if (e.date.isBefore(now)) {
                return "종료";
            }
        }
        return base;
    }

    // ---------------- 공통 유틸들 ----------------

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
                public void mouseExited (MouseEvent e) { btn.setBackground(NAV_BG); }
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("물품대여"))      { new ItemListFrame();   dispose(); }
                    else if (text.equals("공간대여")) { new SpaceRentFrame();  dispose(); }
                    else if (text.equals("마이페이지")) { new MyPageFrame();   dispose(); }
                    else if (text.equals("빈 강의실")) { new EmptyClassFrame();dispose(); }
                    else if (text.equals("커뮤니티"))  { new CommunityFrame(); dispose(); }
                    else { showSimplePopup("알림", "준비 중입니다."); }
                }
            });
        }
        return btn;
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
                g2.fillRoundRect(0,0,getWidth(),getHeight(),30,30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,30,30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        JLabel l = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        l.setFont(uiFont.deriveFont(18f));
        l.setForeground(BROWN);
        l.setBounds(20, 70, 360, 30);
        panel.add(l);

        JButton yes = new JButton("네");
        yes.setFont(uiFont);
        yes.setBounds(60, 150, 120, 45);
        yes.setBackground(BROWN);
        yes.setForeground(Color.WHITE);
        yes.addActionListener(e -> {
            dialog.dispose();
            new LoginFrame();
            dispose();
        });
        panel.add(yes);

        JButton no = new JButton("아니오");
        no.setFont(uiFont);
        no.setBounds(220, 150, 120, 45);
        no.setBackground(BROWN);
        no.setForeground(Color.WHITE);
        no.addActionListener(e -> dialog.dispose());
        panel.add(no);

        dialog.setVisible(true);
    }

    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel() {
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
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
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
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int r, Color c, int t) { radius = r; color = c; thickness = t; }
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
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
