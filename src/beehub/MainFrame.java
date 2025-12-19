package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 매니저 및 DAO 임포트
import council.EventManager;
import council.EventManager.EventData;

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

    // 폰트 로드 및 등록
    static {
        try {
            InputStream is = MainFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            } else {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(uiFont);
            }
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    // 로그인한 사용자 정보
    private String currentUserName = "게스트";
    private String currentUserId   = "";

    // UI 컴포넌트
    private JLabel todayHeaderLabel;
    private JPanel todayContentPanel;
    private JScrollPane todayScrollPane;   // ✅ 추가: TODAY 스크롤
    private JPanel todayPanel;
    private JPanel futureListPanel;

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
        refreshSchedule();
    }

    public MainFrame(String userName, String userId) {
        this.currentUserName = userName;
        this.currentUserId   = userId;
        initFrame();
        refreshSchedule();
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

    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 900, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton btn = createNavButton(menu);
            navPanel.add(btn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 900, 520);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 1. 상단: 오늘 일정 (Today)
        JLabel beeLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(MainFrame.class.getResource("/img/login-bee.png"));
            Image img = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            beeLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            beeLabel.setText("🐝");
            beeLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        }

        beeLabel.setBounds(60, 15, 60, 60);
        contentPanel.add(beeLabel);

        JLabel titleLabel = new JLabel("일정 알리비");
        titleLabel.setFont(uiFont.deriveFont(28f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(130, 25, 250, 40);
        contentPanel.add(titleLabel);

        todayPanel = new JPanel(null);
        todayPanel.setBounds(50, 85, 800, 150);
        todayPanel.setBackground(Color.WHITE);
        todayPanel.setBorder(new RoundedBorder(20, BROWN, 2));
        contentPanel.add(todayPanel);

        JPanel todayHeader = new JPanel(null);
        todayHeader.setBounds(2, 2, 796, 40);
        todayHeader.setBackground(HIGHLIGHT_YELLOW);
        todayPanel.add(todayHeader);

        todayHeaderLabel = new JLabel("TODAY");
        todayHeaderLabel.setFont(uiFont.deriveFont(20f));
        todayHeaderLabel.setForeground(BROWN);
        todayHeaderLabel.setBounds(20, 10, 300, 25);
        todayHeader.add(todayHeaderLabel);

        // ✅ TODAY 내용 패널 (BoxLayout) + 스크롤 적용
        todayContentPanel = new JPanel();
        todayContentPanel.setLayout(new BoxLayout(todayContentPanel, BoxLayout.Y_AXIS));
        todayContentPanel.setOpaque(false); // 배경은 todayPanel이 담당

        todayScrollPane = new JScrollPane(
                todayContentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        // 헤더(40) 아래 영역: y=50부터 적당히 넉넉하게
        todayScrollPane.setBounds(20, 50, 760, 90);
        todayScrollPane.setBorder(null);
        todayScrollPane.setOpaque(false);
        todayScrollPane.getViewport().setOpaque(false);

        // 커스텀 스크롤바 적용 (TODAY에도)
        todayScrollPane.getVerticalScrollBar().setUI(new HoneyScrollBarUI());
        todayScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        todayScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        todayPanel.add(todayScrollPane);

        // 2. 하단: 미래 일정 리스트 (Scroll)
        futureListPanel = new JPanel();
        futureListPanel.setLayout(new BoxLayout(futureListPanel, BoxLayout.Y_AXIS));
        futureListPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(futureListPanel);
        scrollPane.setBounds(50, 255, 800, 240);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 커스텀 스크롤바 적용
        scrollPane.getVerticalScrollBar().setUI(new HoneyScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        contentPanel.add(scrollPane);
    }

    // ===============================
    // 📅 데이터 로드 및 정렬 로직
    // ===============================
    private void refreshSchedule() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter todayFmt = DateTimeFormatter.ofPattern("M월 d일");
        todayHeaderLabel.setText("(TODAY) " + today.format(todayFmt));

        List<ScheduleItem> allItems = new ArrayList<>();

        // 1. [물품 반납]
        List<Rent> allRents = RentDAO.getInstance().getAllRentals();
        for (Rent r : allRents) {
            if (r.getRenterId().equals(currentUserId) && !r.isReturned()) {
                allItems.add(new ScheduleItem(r.getDueDate(), r.getItemName(), "RENTAL"));
            }
        }

        // 2. [과 행사]
        List<EventData> events = EventManager.getAllEvents();
        for (EventData e : events) {
            if (e.date != null) {
                allItems.add(new ScheduleItem(e.date.toLocalDate(), e.title, "EVENT"));
            }
        }

        // 3. 정렬
        Collections.sort(allItems);

        // 4. 화면 갱신
        todayContentPanel.removeAll();
        futureListPanel.removeAll();

        boolean hasToday = false;

        for (ScheduleItem item : allItems) {
            // (1) 오늘 일정
            if (item.date.isEqual(today) || (item.type.equals("RENTAL") && item.date.isBefore(today))) {
                JLabel itemLabel = new JLabel();
                itemLabel.setFont(uiFont.deriveFont(18f));
                itemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                if (item.type.equals("RENTAL")) {
                    itemLabel.setText("[물품] '" + item.title + "' 반납일입니다!");
                    itemLabel.setForeground(Color.gray);
                } else {
                    itemLabel.setText("- [행사] " + item.title);
                    itemLabel.setForeground(Color.gray);
                }

                todayContentPanel.add(itemLabel);
                todayContentPanel.add(Box.createVerticalStrut(5));
                hasToday = true;
            }
            // (2) 미래 일정
            else if (item.date.isAfter(today)) {
                addFutureItemRow(item);
            }
        }

        if (!hasToday) {
            JLabel emptyLabel = new JLabel("오늘의 주요 일정이 없습니다.");
            emptyLabel.setFont(uiFont.deriveFont(18f));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            todayContentPanel.add(emptyLabel);
        }

        if (futureListPanel.getComponentCount() == 0) {
            JLabel emptyLabel = new JLabel("예정된 일정이 없습니다.");
            emptyLabel.setFont(uiFont.deriveFont(18f));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            futureListPanel.add(emptyLabel);
        }

        // ✅ 스크롤 높이 반영
        todayContentPanel.revalidate();
        todayContentPanel.repaint();
        futureListPanel.revalidate();
        futureListPanel.repaint();

        // ✅ TODAY 스크롤을 항상 위로 올리고 싶으면(선택)
        if (todayScrollPane != null) {
            SwingUtilities.invokeLater(() -> todayScrollPane.getVerticalScrollBar().setValue(0));
        }
    }

    private void addFutureItemRow(ScheduleItem item) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(780, 40));

        String dateStr = item.date.format(DateTimeFormatter.ofPattern("MM월 dd일"));
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(uiFont.deriveFont(18f));
        dateLabel.setForeground(BROWN);

        JLabel barLabel = new JLabel("|");
        barLabel.setFont(uiFont.deriveFont(18f));
        barLabel.setForeground(Color.LIGHT_GRAY);

        String contentText;
        if (item.type.equals("RENTAL")) {
            contentText = "\" " + item.title + " \" 반납";
        } else {
            contentText = item.title;
        }

        JLabel contentLabel = new JLabel(contentText);
        contentLabel.setFont(uiFont.deriveFont(18f));
        contentLabel.setForeground(Color.BLACK);

        row.add(dateLabel);
        row.add(barLabel);
        row.add(contentLabel);

        futureListPanel.add(row);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(780, 1));
        sep.setForeground(new Color(240, 240, 240));
        futureListPanel.add(sep);
    }

    class ScheduleItem implements Comparable<ScheduleItem> {
        LocalDate date;
        String title;
        String type;
        public ScheduleItem(LocalDate date, String title, String type) {
            this.date = date;
            this.title = title;
            this.type = type;
        }
        @Override
        public int compareTo(ScheduleItem o) {
            return this.date.compareTo(o.date);
        }
    }

    // ===============================
    // 네비게이션 & 팝업
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
                    case "물품대여": new ItemListFrame(); dispose(); break;
                    case "과행사": new EventListFrame(); dispose(); break;
                    case "공간대여": new SpaceRentFrame(); dispose(); break;
                    case "빈 강의실": new EmptyClassFrame(); dispose(); break;
                    case "커뮤니티": new CommunityFrame(); dispose(); break;
                    case "마이페이지": new MyPageFrame(); dispose(); break;
                }
            }
        });
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
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
            try { LoginSession.setUser(null); } catch (Exception ex) {}
            SwingUtilities.invokeLater(() -> { new LoginFrame().setVisible(true); });
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        return btn;
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int radius, Color color, int thickness) { this.radius = radius; this.color = color; this.thickness = thickness; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    // ===============================
    // 🍯 커스텀 스크롤바 UI (Honey Theme)
    // ===============================
    private static class HoneyScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = BROWN;
            this.trackColor = new Color(250, 250, 250);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BROWN);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                    thumbBounds.width - 2, thumbBounds.height - 2, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(250, 250, 250));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
