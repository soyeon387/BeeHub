// 파일명: EventDetailFrame.java
package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDateTime;

import council.EventManager;
import council.EventManager.EventData;
import council.EventManager.FeeType;

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
    private static final Color GRAY_BEFORE = new Color(225, 225, 225); // 진행 전/신청 전
    private static final Color RED_CLOSED  = new Color(255, 160, 160); // 신청 마감


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
    private String userId    = "20230000";
    private int    userPoint = 0;
    private String userDept  = "";

    private EventData eventData;
    private boolean isApplied = false;

    public EventDetailFrame(EventData event) {
        this.eventData = event;
        setTitle("서울여대 꿀단지 - " + event.title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 🔹 로그인 사용자 정보 세팅
        Member currentUser = LoginSession.getUser();
        if (currentUser != null) {
            this.userName  = currentUser.getName();
            this.userId    = currentUser.getHakbun();
            this.userPoint = currentUser.getPoint();
            if (currentUser.getMajor() != null) {
                this.userDept = currentUser.getMajor();
            }
        }

        initUI();
        setVisible(true);
    }

    private void initUI() {
    	System.out.println("[DEBUG] " + eventData.title
    	        + " type=" + eventData.eventType
    	        + " dbStatus=" + eventData.status
    	        + " date=" + eventData.date
    	        + " applyStart=" + eventData.applyStart
    	        + " applyEnd=" + eventData.applyEnd);

        // ===== 헤더 =====
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(logoLabel);
        
        logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 1. 마우스 올리면 손가락 모양으로 변경
        logoLabel.addMouseListener(new MouseAdapter() {      // 2. 마우스 기능 추가
            @Override
            public void mouseClicked(MouseEvent e) {
                // 현재 창 닫기
                dispose(); 
                
                // 메인 화면(MainFrame) 새로 열기
                new MainFrame(); 
            }
        });

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
            @Override
            public void mouseClicked(MouseEvent e) {
                showLogoutPopup();
            }
        });
        userInfoPanel.add(logoutBtn);

        headerPanel.add(userInfoPanel);

        // ===== 상단 네비 =====
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("과행사"));
            navPanel.add(menuBtn);
        }

        // ===== 메인 컨텐츠 =====
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

        // 🔹 상태 계산
        String computedStatus = computeEventStatus(eventData);
        eventData.status = computedStatus;

        JLabel statusLabel = new JLabel(computedStatus);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(50, 70, 110, 35);
        statusLabel.setOpaque(true);
        Color statusColor;
        switch (computedStatus) {
            case "진행 전":
            case "신청 전":
                statusColor = GRAY_BEFORE;   // ✅ 회색
                break;

            case "신청 마감":
            case "신청마감":
                statusColor = RED_CLOSED;    // ✅ 마감은 빨강(리스트랑 통일)
                break;

            default:
                statusColor = GREEN_PROGRESS;
                break;
        }
        statusLabel.setBackground(statusColor);

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

        // 🔹 상태 / 정원 체크
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

                if (isApplied) {
                    showSimplePopup("알림", "이미 신청하셨습니다.");
                    return;
                }

                Member user = LoginSession.getUser();
                if (user == null) {
                    showSimplePopup("안내", "로그인이 필요합니다.");
                    return;
                }

                // 학과 체크
                String myMajor = (user.getMajor() != null) ? user.getMajor().trim() : "";
                String target  = (eventData.targetDept != null) ? eventData.targetDept.trim() : "";

                if (!target.isEmpty()
                        && !"전체학과".equals(target)
                        && !"전체".equals(target)
                        && !"ALL".equalsIgnoreCase(target)) {

                    if (!target.equals(myMajor)) {
                        showSimplePopup("참여 불가",
                                "본 행사는 '" + target + "' 학생만\n 참여 가능합니다.");
                        return;
                    }
                }

                // 🔹 회비 조건 체크 (DB에서 최신 값 읽어오기)
                if (!checkFeeCondition(eventData, user)) {
                    return;
                }

                // 비밀코드
                if (eventData.secretCode != null && !eventData.secretCode.isEmpty()) {
                    showSecretCodeDialog(slotsLabel, statusLabel, applyButton);
                } else {
                    applyEvent(slotsLabel, statusLabel, applyButton);
                }
            });

            contentPanel.add(applyButton);
        }
    }

    // 🔹 회비 조건 체크 : DB에서 다시 읽어서 비교
    private boolean checkFeeCondition(EventData event, Member sessionUser) {
        FeeType fee = event.requiredFee;
        if (fee == null || fee == FeeType.NONE) {
            return true;
        }

        // DB에서 최신 회원 정보 다시 읽기
        MemberDAO dao = new MemberDAO();
        Member user = dao.findByHakbun(sessionUser.getHakbun());
        if (user == null) {
            showSimplePopup("오류", "회원 정보를 불러오지 못했습니다.");
            return false;
        }

        String schoolYn = user.getIsFeePaid(); // is_fee_paid
        String deptYn   = user.getDeptFeeYn(); // dept_fee_yn

        System.out.println("DEBUG feeType=" + fee +
                ", schoolYn=" + schoolYn + ", deptYn=" + deptYn);

        if (fee == FeeType.SCHOOL) {
            if (!"Y".equalsIgnoreCase(schoolYn)) {
                showSimplePopup("신청 불가",
                        "이 행사는 '학교 학생회비 납부자'만 신청할 수 있습니다.");
                return false;
            }
            return true;
        }

        if (fee == FeeType.DEPT) {
            if (!"Y".equalsIgnoreCase(deptYn)) {
                showSimplePopup("신청 불가",
                        "이 행사는 과 학생회비 납부자만 신청할 수 있습니다.");
                return false;
            }
            return true;
        }

        return true;
    }

    // 🔹 행사 시간이 지났으면 종료
 // ✅ 상세화면 상태 계산: event_date(09:00)로 종료시키지 말고 apply_end(18:00) 기준
    private String computeEventStatus(EventData e) {
        if (e == null) return "삭제";
        if ("삭제".equals(e.status)) return "삭제";

        LocalDateTime now = LocalDateTime.now();

        String type = (e.eventType != null) ? e.eventType.trim() : "";
        boolean isSnack = type.equalsIgnoreCase("SNACK") || type.contains("간식");

        LocalDateTime eventTime  = e.date;
        LocalDateTime applyStart = e.applyStart;
        LocalDateTime applyEnd   = e.applyEnd;

        int total   = e.totalCount;
        int current = e.currentCount;

        if (isSnack) {
            LocalDateTime start = (applyStart != null) ? applyStart : eventTime;
            LocalDateTime end   = applyEnd;

            // end가 없을 때만 예외적으로 1시간짜리 처리(데이터 누락 방어)
            if (end == null && start != null) end = start.plusHours(1);

            if (end != null && now.isAfter(end)) return "종료";
            if (start != null && now.isBefore(start)) return "진행 전";

            if (total > 0 && current >= total) return "신청 마감";
            return "진행 중";
        }

        // 참여형도 신청기간 기준
        LocalDateTime start = (applyStart != null) ? applyStart : eventTime;
        LocalDateTime end   = applyEnd;

        if (start != null && now.isBefore(start)) return "신청 전";
        if (end != null && now.isAfter(end)) return "신청 마감";

        if (total > 0 && current >= total) return "신청 마감";
        return "신청 중";
    }


    private void addDetailLabel(JPanel p, String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(17f));
        l.setForeground(new Color(80, 80, 80));
        l.setBounds(50, y, 650, 25);
        p.add(l);
    }

    // ================= 비밀코드 팝업 =================
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
            @Override
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
            field.setEchoChar('*');
            field.setFont(uiFont.deriveFont(24f));
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setPreferredSize(new Dimension(50, 50));
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(BROWN, 2));
            field.setForeground(BROWN);

            final int index = i;
            field.addKeyListener(new KeyAdapter() {
                @Override
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
            for (JPasswordField field : codeFields) {
                inputCode += new String(field.getPassword());
            }

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



 // ================= 실제 신청 처리 =================
    private void applyEvent(JLabel slotsLabel, JLabel statusLabel, JButton applyButton) {
        boolean success = eventData.addRecipient(userName, userId, "O");

        if (!success) {
            showSimplePopup("알림", "이미 신청하셨습니다.");
            return;
        }

        // [수정] DB 조회 시점 차이로 인해 eventData.currentCount가 갱신 전 값일 수 있습니다.
        // 따라서 화면에는 신청 성공이 확실하므로 강제로 +1을 하여 '왼쪽' 숫자를 즉시 늘려줍니다.
        int newCount = eventData.currentCount + 1; 
        
        // (선택사항) 데이터 객체 값도 맞춰줍니다.
        eventData.currentCount = newCount;

        // 화면 텍스트 갱신 (왼쪽: 현재 인원 / 오른쪽: 총 인원)
        slotsLabel.setText("신청 현황 : " + newCount + " / " + eventData.totalCount + "명");
        
        // UI 즉시 새로고침 (혹시 모를 렌더링 지연 방지)
        if (slotsLabel.getParent() != null) {
            slotsLabel.getParent().revalidate();
            slotsLabel.getParent().repaint();
        }

        isApplied = true;
        showSimplePopup("성공", "신청이 완료되었습니다.");

        // 마감 여부 체크 (갱신된 newCount 사용)
        if (newCount >= eventData.totalCount) {
            applyButton.setVisible(false);
            statusLabel.setText("신청마감");
            statusLabel.setBackground(ORANGE_CLOSED);
            eventData.status = "신청마감";
        }
    }
    
    // ================= 공통 팝업 / 로그아웃 =================
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

    // ================= 공통 컴포넌트 =================
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
                @Override
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                @Override
                public void mouseExited (MouseEvent e) { btn.setBackground(NAV_BG); }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("과행사")) return;

                    if (text.equals("물품대여"))        { new ItemListFrame();   dispose(); }
                    else if (text.equals("공간대여"))   { new SpaceRentFrame();  dispose(); }
                    else if (text.equals("마이페이지")) { new MyPageFrame();    dispose(); }
                    else if (text.equals("커뮤니티"))   { new CommunityFrame();  dispose(); }
                    else if (text.equals("빈 강의실"))  { new EmptyClassFrame(); dispose(); }
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
            radius = r;
            color = c;
            thickness = t;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }

        @Override
        public boolean isBorderOpaque() { return false; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }
}
