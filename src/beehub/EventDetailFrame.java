package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDateTime;

import council.EventManager;
import council.EventManager.EventData;

public class EventDetailFrame extends JFrame {

    private static final Color HEADER_YELLOW    = new Color(255, 238, 140);
    private static final Color NAV_BG           = new Color(255, 255, 255);
    private static final Color BG_MAIN          = new Color(255, 255, 255);
    private static final Color BROWN            = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color GREEN_PROGRESS   = new Color(180, 230, 180);
    private static final Color ORANGE_CLOSED    = new Color(255, 200, 180);
    private static final Color GRAY_BTN         = new Color(180, 180, 180);
    private static final Color POPUP_BG         = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = EventDetailFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private String userName  = "사용자";
    private String userId    = "20230000"; // 기본값
    private int    userPoint = 0;

    // ✅ 학과 정보 (지금은 테스트용, 나중에 User에서 받아오면 됨)
    private String  userDept        = "";
    private boolean isSchoolFeePaid = true;   // 학교 학생회비 납부 여부
    private boolean isDeptFeePaid   = false;  // 과 학생회비 납부 여부 (테스트: 미납)

    private EventData eventData;
    private boolean isApplied = false;

    public EventDetailFrame(EventData event) {
        this.eventData = event;
        setTitle("서울여대 꿀단지 - " + event.title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 로그인 사용자 정보
        User currentUser = UserManager.getCurrentUser();
        if (currentUser != null) {
            this.userName  = currentUser.getName();
            this.userId    = currentUser.getId();
            this.userPoint = currentUser.getPoints();

            if (currentUser.getDept() != null) {
                this.userDept = currentUser.getDept();
            }
        }

        initUI();
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

        JLabel userInfoText = new JLabel("[" + userName + "]님 | ");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoPanel.add(userInfoText);

        JLabel logoutBtn = new JLabel("로그아웃");
        logoutBtn.setFont(uiFont.deriveFont(14f));
        logoutBtn.setForeground(BROWN);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });
        userInfoPanel.add(logoutBtn);

        headerPanel.add(userInfoPanel);

        // --- 네비게이션 ---
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "간식행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("간식행사") || menu.equals("과행사"));
            navPanel.add(menuBtn);
        }

        // --- 메인 컨텐츠 ---
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        JButton backButton = new JButton("이전 화면");
        backButton.setFont(uiFont.deriveFont(14f));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(GRAY_BTN);
        backButton.setBounds(680, 20, 90, 30);
        backButton.setFocusPainted(false);
        backButton.setBorder(new RoundedBorder(10, GRAY_BTN, 1));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            new EventListFrame();
            dispose();
        });
        contentPanel.add(backButton);

        // ✅ 상태 계산 (행사 시간이 지났으면 무조건 "종료")
        String computedStatus = computeEventStatus(eventData);
        eventData.status = computedStatus;

        JLabel statusLabel = new JLabel(computedStatus);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(50, 70, 110, 35);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(
                "신청마감".equals(computedStatus) || "신청 마감".equals(computedStatus) || "종료".equals(computedStatus)
                        ? ORANGE_CLOSED
                        : GREEN_PROGRESS
        );
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(statusLabel);

        JLabel nameLabel = new JLabel(eventData.title);
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 32f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(50, 115, 600, 40);
        contentPanel.add(nameLabel);

        JTextArea descArea = new JTextArea(eventData.description);
        descArea.setFont(uiFont.deriveFont(16f));
        descArea.setForeground(new Color(100, 100, 100));
        descArea.setBackground(BG_MAIN);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBorder(null);

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBounds(50, 165, 650, 60);
        descScroll.setBorder(null);
        contentPanel.add(descScroll);

        int yPos = 240;
        String dateStr = eventData.date.format(EventManager.DATE_FMT);

        addDetailLabel(contentPanel, "일시 : " + dateStr, yPos); yPos += 30;
        addDetailLabel(contentPanel, "장소 : " + eventData.location, yPos); yPos += 30;
        addDetailLabel(contentPanel, "신청 기간 : " + eventData.getPeriodString(), yPos); yPos += 30;
        addDetailLabel(contentPanel, "참여 조건 : " + eventData.requiredFee.getLabel(), yPos); yPos += 30;

        JLabel slotsLabel = new JLabel("신청 현황 : " + eventData.currentCount + " / " + eventData.totalCount + "명");
        slotsLabel.setFont(uiFont.deriveFont(17f));
        slotsLabel.setForeground(new Color(80, 80, 80));
        slotsLabel.setBounds(50, yPos, 600, 25);
        contentPanel.add(slotsLabel);

        // ✅ 진행/신청 중 + 정원이 안 찼을 때만 신청 버튼
        String st = eventData.status != null ? eventData.status : "";
        boolean isOpenStatus =
                "진행중".equals(st) || "진행 중".equals(st) ||
                "신청중".equals(st) || "신청 중".equals(st);

        if (isOpenStatus && eventData.currentCount < eventData.totalCount) {
            JButton applyButton = new JButton("신청하기");
            applyButton.setFont(uiFont.deriveFont(Font.BOLD, 18f));
            applyButton.setForeground(Color.WHITE);
            applyButton.setBackground(BROWN);
            applyButton.setBounds(570, 360, 180, 50);
            applyButton.setFocusPainted(false);
            applyButton.setBorder(new RoundedBorder(15, BROWN, 1));
            applyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            applyButton.addActionListener(e -> {

                // 0. 이미 신청 여부
                if (isApplied) {
                    showSimplePopup("알림", "이미 신청하셨습니다.");
                    return;
                }

                // 1. 로그인 체크
                User user = UserManager.getCurrentUser();
                if (user == null) {
                    showSimplePopup("안내", "로그인이 필요합니다.");
                    return;
                }

                // 2. 학과 체크
                String myMajor = userDept; // 현재는 userDept 사용
                if (myMajor == null) myMajor = "";
                myMajor = myMajor.trim();

                String target = eventData.targetDept;   // DB에 저장된 대상 학과
                if (target == null) target = "";
                target = target.trim();

                // target이 비어있거나 전체학과/ALL 이면 누구나 가능
                if (!target.isEmpty()
                        && !"전체학과".equals(target)
                        && !"전체".equals(target)
                        && !"ALL".equalsIgnoreCase(target)) {

                    if (!target.equals(myMajor)) {
                        showSimplePopup("참여 불가",
                                "본 행사는 '" + target + "' 학생만 참여 가능합니다.");
                        return;
                    }
                }

                // 3. 회비 납부 체크
                boolean canJoin = true;
                String feeMsg = "";

                if (eventData.requiredFee == EventManager.FeeType.SCHOOL) {
                    if (!isSchoolFeePaid) {
                        canJoin = false;
                        feeMsg = "학교 학생회비 납부자만";
                    }
                } else if (eventData.requiredFee == EventManager.FeeType.DEPT) {
                    if (!isDeptFeePaid) {
                        canJoin = false;
                        feeMsg = "과 학생회비 납부자만";
                    }
                }

                if (!canJoin) {
                    showSimplePopup("신청 불가", feeMsg + "\n참여 가능한 행사입니다.");
                    return;
                }

                // 4. 비밀코드 체크
                if (eventData.secretCode != null && !eventData.secretCode.isEmpty()) {
                    showSecretCodeDialog(slotsLabel, statusLabel, applyButton);
                } else {
                    applyEvent(slotsLabel, statusLabel, applyButton);
                }
            });

            contentPanel.add(applyButton);
        }
    }

    // ✅ 상태 계산 메소드 (행사 시간이 지났으면 종료)
    private String computeEventStatus(EventData e) {
        String baseStatus = (e.status == null || e.status.isEmpty()) ? "진행중" : e.status;

        if (e.date != null) {
            LocalDateTime now = LocalDateTime.now();
            if (e.date.isBefore(now)) {
                return "종료";
            }
        }
        return baseStatus;
    }

    private void addDetailLabel(JPanel p, String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(17f));
        l.setForeground(new Color(80, 80, 80));
        l.setBounds(50, y, 650, 25);
        p.add(l);
    }

    // 이하 나머지 메서드는 그대로 -----------------------

    private void showSecretCodeDialog(JLabel slotsLabel, JLabel statusLabel, JButton applyButton) {
        JDialog dialog = new JDialog(this, "비밀코드 입력", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(uiFont.deriveFont(20f));
        closeBtn.setForeground(BROWN);
        closeBtn.setBounds(410, 20, 20, 20);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dialog.dispose(); }
        });
        panel.add(closeBtn);

        JLabel msgLabel = new JLabel("비밀코드를 입력해주세요", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(20f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(50, 60, 350, 30);
        panel.add(msgLabel);

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codePanel.setBounds(90, 110, 270, 50);
        codePanel.setOpaque(false);

        JPasswordField[] codeFields = new JPasswordField[4];
        for (int i = 0; i < 4; i++) {
            JPasswordField field = new JPasswordField(1);
            field.setFont(uiFont.deriveFont(24f));
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setPreferredSize(new Dimension(50, 50));
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(BROWN, 2));
            field.setForeground(BROWN);

            final int index = i;
            field.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (field.getPassword().length >= 1) {
                        e.consume();
                        if (index < 3) codeFields[index + 1].requestFocus();
                    }
                }
            });
            codeFields[i] = field;
            codePanel.add(field);
        }
        panel.add(codePanel);

        JButton confirmBtn = new JButton("확인");
        confirmBtn.setFont(uiFont.deriveFont(16f));
        confirmBtn.setBackground(BROWN);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBounds(150, 200, 150, 45);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            String inputCode = "";
            for (JPasswordField field : codeFields)
                inputCode += new String(field.getPassword());

            if (inputCode.equals(eventData.secretCode)) {
                dialog.dispose();
                applyEvent(slotsLabel, statusLabel, applyButton);
            } else {
                showSimplePopup("오류", "비밀코드가 일치하지 않습니다.");
            }
        });
        panel.add(confirmBtn);

        dialog.setVisible(true);
    }

    private void applyEvent(JLabel slotsLabel, JLabel statusLabel, JButton applyButton) {
        boolean success = eventData.addRecipient(userName, userId, "O");

        if (!success) {
            showSimplePopup("알림", "이미 이 행사에 신청하셨습니다.");
            return;
        }

        slotsLabel.setText("신청 현황 : " + eventData.currentCount + " / " + eventData.totalCount + "명");
        isApplied = true;
        showSimplePopup("성공", "신청이 완료되었습니다.");

        if (eventData.currentCount >= eventData.totalCount) {
            applyButton.setVisible(false);
            statusLabel.setText("신청마감");
            statusLabel.setBackground(ORANGE_CLOSED);
            eventData.status = "신청마감";
        }
    }

    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        String[] lines = message.split("\n");
        int yPos = (lines.length == 1) ? 80 : 60;

        for (String line : lines) {
            JLabel lbl = new JLabel(line, SwingConstants.CENTER);
            lbl.setFont(uiFont.deriveFont(18f));
            lbl.setForeground(BROWN);
            lbl.setBounds(20, yPos, 360, 30);
            panel.add(lbl);
            yPos += 30;
        }

        JButton confirmBtn = new JButton("확인");
        confirmBtn.setFont(uiFont.deriveFont(16f));
        confirmBtn.setBackground(BROWN);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBounds(135, 170, 130, 45);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> dialog.dispose());
        panel.add(confirmBtn);

        dialog.setVisible(true);
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
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
            UserManager.logout();
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
                public void mouseExited (MouseEvent e)  { btn.setBackground(NAV_BG); }
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("간식행사") || text.equals("과행사")) return;

                    if (text.equals("물품대여"))      { new ItemListFrame();   dispose(); }
                    else if (text.equals("공간대여")) { new SpaceRentFrame();  dispose(); }
                    else if (text.equals("마이페이지")) { new MyPageFrame();   dispose(); }
                    else if (text.equals("커뮤니티"))  { new CommunityFrame(); dispose(); }
                    else if (text.equals("빈 강의실")) { new EmptyClassFrame();dispose(); }
                    else if (text.equals("서울여대 꿀단지")) { new MainFrame(); dispose(); }
                    else { showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다."); }
                }
            });
        }
        return btn;
    }

    private static class RoundedBorder implements Border {
        private int   radius;
        private Color color;
        private int   thickness;
        public RoundedBorder(int r, Color c, int t) {
            radius = r; color = c; thickness = t;
        }
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
}
