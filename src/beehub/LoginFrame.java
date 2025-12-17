package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*; 
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL;
import admin.AdminMainFrame;
import council.CouncilMainFrame;

public class LoginFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마 (따스하고 세련된 꿀 배색)
    // ===============================
    private static final Color BG_YELLOW = new Color(255, 250, 205); // 더 부드러운 노랑
    private static final Color BROWN = new Color(89, 54, 25);       // 진한 초콜릿 브라운
    private static final Color SOFT_BROWN = new Color(130, 90, 60);  // 서브 텍스트용 브라운
    private static final Color INPUT_BG = new Color(255, 255, 255);
    private static final Color POINT_ORANGE = new Color(255, 167, 38); // 포인트 컬러

    private static Font uiFont;

    static {
        try {
            InputStream is = LoginFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                uiFont = new Font("맑은 고딕", Font.BOLD, 12);
            } else {
                Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                uiFont = base.deriveFont(12f);
            }
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.BOLD, 12);
        }
    }

    private CardLayout cardLayout;
    private JPanel containerPanel;
    
    private JTextField hakbunField;
    private JPasswordField pwField;
    
    private JTextField adminIdField;
    private JPasswordField adminPwField;

    private JTextField findNameField;
    private JTextField findHakbunField;
    private JTextField findPhoneField;

    public LoginFrame() {
        setTitle("서울여대 꿀단지");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        containerPanel.add(createLoginPanel(), "login");
        containerPanel.add(createFindPwPanel(), "findPw");
        containerPanel.add(createAdminPanel(), "admin");

        add(containerPanel);
        setVisible(true);
    }

    // ===============================================================
    // 1️⃣ 일반 로그인 화면 (디자인 강화 버전)
    // ===============================================================
    private JPanel createLoginPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);

        // 🏷️ 서브 타이틀 (휑한 느낌 제거)
        JLabel subLabel = new JLabel("슈니만을 위한 학교 생활 도우미", SwingConstants.CENTER);
        subLabel.setFont(uiFont.deriveFont(Font.PLAIN, 15f));
        subLabel.setForeground(SOFT_BROWN);
        subLabel.setBounds(25, 45, 450, 30);
        panel.add(subLabel);

        // 🏷️ 메인 타이틀 (외곽선 없이 깔끔하게)
        OutlinedLabel title = new OutlinedLabel("서울여대 꿀단지", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(48f));
        title.setForeground(BROWN);
        title.setOutlineColor(new Color(255, 255, 255, 0)); // 외곽선 제거
        title.setStrokeWidth(0f);
        title.setBounds(25, 75, 450, 80);
        panel.add(title);

        // ⌨️ 입력 섹션 (위치 재조정)
        JLabel idLabel = new JLabel("학번 (ID)");
        idLabel.setFont(uiFont.deriveFont(19f));
        idLabel.setForeground(BROWN);
        idLabel.setBounds(85, 180, 150, 30);
        panel.add(idLabel);

        hakbunField = createStyledTextField();
        hakbunField.setBounds(80, 225, 340, 50);
        hakbunField.addActionListener(e -> handleUserLogin());
        panel.add(hakbunField);

        JLabel pwLabel = new JLabel("비밀번호 (PW)");
        pwLabel.setFont(uiFont.deriveFont(19f));
        pwLabel.setForeground(BROWN);
        pwLabel.setBounds(85, 310, 150, 30);
        panel.add(pwLabel);

        pwField = createStyledPasswordField();
        pwField.setBounds(80, 365, 340, 50);
        pwField.addActionListener(e -> handleUserLogin());
        panel.add(pwField);

        // 🔗 보조 버튼
        JButton findPwBtn = createTextButton("비밀번호 찾기");
        findPwBtn.setBounds(255, 440, 260, 30);
        findPwBtn.addActionListener(e -> cardLayout.show(containerPanel, "findPw"));
        panel.add(findPwBtn);

        // 🚀 로그인 버튼 (세련된 둥근 버튼)
        JButton loginBtn = createStyledButton("로그인");
     // x를 90에서 105로 변경
        loginBtn.setBounds(105, 490, 290, 55);
        loginBtn.addActionListener(e -> handleUserLogin());
        panel.add(loginBtn);

        // 👤 관리자 버튼 (하단 배치)
        JButton adminBtn = createSmallButton("관리자 모드");
        adminBtn.setBounds(360, 610, 110, 35);
        adminBtn.addActionListener(e -> cardLayout.show(containerPanel, "admin"));
        panel.add(adminBtn);

        return panel;
    }

    // ===============================================================
    // 🎨 픽셀 스타일 배경 (세련된 버전)
    // ===============================================================
    private JPanel createBackgroundPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 1. 메인 배경색
                g2d.setColor(BG_YELLOW); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // 2. 고해상도 픽셀/도트 패턴
                int rectSize = 4;
                int gap = 24;
                
                for (int x = 0; x < getWidth(); x += gap) {
                    for (int y = 0; y < getHeight(); y += gap) {
                        int offsetX = ((y / gap) % 2 == 0) ? 0 : gap / 2;
                        
                        // 위치에 따라 미세하게 색상을 변화시켜 깊이감 부여
                        float alpha = 0.1f + (float)(Math.sin((x+y)/50.0)*0.05);
                        g2d.setColor(new Color(130, 90, 60, (int)(alpha * 255)));
                        g2d.fillRect(x + offsetX, y, rectSize, rectSize);
                    }
                }

                // 3. 은은한 상단 그라데이션 (입체감)
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 100), 0, 150, new Color(255, 255, 255, 0));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), 150);
            }
        };
    }

    // ===============================================================
    // 🛠️ 세련된 UI 컴포넌트 생성기
    // ===============================================================
    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(uiFont.deriveFont(17f));
        f.setBackground(INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, new Color(210, 210, 210)), 
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // 포커스 이벤트 (선택 시 테두리 색상 변경)
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, POINT_ORANGE), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
            public void focusLost(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, new Color(210, 210, 210)), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
        });
        return f;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setEchoChar('●'); 
        f.setFont(uiFont.deriveFont(17f));
        f.setBackground(INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, new Color(210, 210, 210)), 
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, POINT_ORANGE), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
            public void focusLost(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, new Color(210, 210, 210)), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
        });
        return f;
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        b.setBackground(BROWN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(20, BROWN));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 버튼 호버 효과
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(SOFT_BROWN); }
            public void mouseExited(MouseEvent e) { b.setBackground(BROWN); }
        });
        return b;
    }

    private JButton createSmallButton(String text) {
        JButton b = new JButton(text); 
        b.setFont(uiFont.deriveFont(13f));
        b.setBackground(new Color(255, 255, 255, 150));
        b.setForeground(BROWN);
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(12, new Color(200, 200, 200)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createTextButton(String text) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(Font.PLAIN, 13f));
        b.setContentAreaFilled(false);
        b.setBorder(null);
        b.setForeground(SOFT_BROWN);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(POINT_ORANGE); }
            public void mouseExited(MouseEvent e) { b.setForeground(SOFT_BROWN); }
        });
        return b;
    }

    // ───────────────────────────────────────────────────────────────
    // 기존 기능 메서드 (handleUserLogin, handleAdminLogin, createFindPwPanel 등)
    // ───────────────────────────────────────────────────────────────
    
    private JPanel createFindPwPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);

        OutlinedLabel title = new OutlinedLabel("비밀번호 찾기", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(38f));
        title.setForeground(BROWN);
        title.setOutlineColor(Color.WHITE);
        title.setStrokeWidth(0f);
        title.setBounds(50, 60, 400, 60);
        panel.add(title);

        findNameField   = addLabelAndField(panel, "이름 :",     160);
        findHakbunField = addLabelAndField(panel, "학번 :",     240);
        findPhoneField  = addLabelAndField(panel, "전화번호 :", 320);

        JButton cancelBtn = createSmallButton("취소");
        cancelBtn.setBounds(100, 420, 120, 55);
        cancelBtn.addActionListener(e -> cardLayout.show(containerPanel, "login"));
        panel.add(cancelBtn);

        JButton confirmBtn = createStyledButton("확인");
        confirmBtn.setBounds(240, 420, 160, 55);
        confirmBtn.addActionListener(e -> handleFindPassword());
        panel.add(confirmBtn);

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);

        JLabel subTitle = new JLabel("관리자 모니터링 시스템", SwingConstants.CENTER);
        subTitle.setFont(uiFont.deriveFont(15f));
        subTitle.setForeground(SOFT_BROWN);
        subTitle.setBounds(50, 60, 400, 30);
        panel.add(subTitle);

        OutlinedLabel title = new OutlinedLabel("ADMIN LOGIN", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(40f));
        title.setForeground(BROWN);
        title.setStrokeWidth(0f);
        title.setBounds(50, 90, 400, 60);
        panel.add(title);

        JLabel idLabel = new JLabel("관리자 ID");
        idLabel.setFont(uiFont.deriveFont(18f));
        idLabel.setForeground(BROWN);
        idLabel.setBounds(80, 220, 150, 30);
        panel.add(idLabel);

        adminIdField = createStyledTextField();
        adminIdField.setBounds(80, 255, 340, 50);
        adminIdField.addActionListener(e -> handleAdminLogin());
        panel.add(adminIdField);

        JLabel pwLabel = new JLabel("비밀번호");
        pwLabel.setFont(uiFont.deriveFont(18f));
        pwLabel.setForeground(BROWN);
        pwLabel.setBounds(80, 330, 150, 30);
        panel.add(pwLabel);

        adminPwField = createStyledPasswordField();
        adminPwField.setBounds(80, 365, 340, 50);
        adminPwField.addActionListener(e -> handleAdminLogin());
        panel.add(adminPwField);

        JButton loginBtn = createStyledButton("시스템 접속");
        loginBtn.setBounds(100, 460, 300, 60);
        loginBtn.addActionListener(e -> handleAdminLogin());
        panel.add(loginBtn);
        
        JButton backBtn = createSmallButton("학생 로그인으로");
        backBtn.setBounds(340, 600, 130, 50);
        backBtn.addActionListener(e -> cardLayout.show(containerPanel, "login"));
        panel.add(backBtn);

        return panel;
    }

    private void handleUserLogin() {
        String id = hakbunField.getText().trim();
        String pw = new String(pwField.getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) { showCustomDialog("아이디와 비밀번호를 \n모두 입력해주세요.", false); return; }
        UserDAO dao = new UserDAO();
        User loginUser = dao.loginAndGetUser(id, pw);
        if (loginUser != null) {
            if (!"USER".equalsIgnoreCase(loginUser.getRole())) { showCustomDialog("관리자 로그인 페이지를 이용해주세요.", false); return; }
            UserManager.setCurrentUser(loginUser);
            Member m = convertToMember(loginUser);
            LoginSession.setUser(m);
            showCustomDialog("반가워요, " + m.getName() + "님!", false);
            new MainFrame(m.getName(), m.getHakbun());
            dispose();
        } else { showCustomDialog("정보가 일치하지 않습니다.\n다시 확인해주세요.", false); }
    }

    private void handleAdminLogin() {
        String id = adminIdField.getText().trim();
        String pw = new String(adminPwField.getPassword()).trim();
        if(id.isEmpty() || pw.isEmpty()) { showCustomDialog("관리자 정보를 입력해주세요.", false); return; }
        UserDAO dao = new UserDAO();
        if (dao.checkAdminLogin(id, pw)) { showCustomDialog("총 관리자 시스템에 접속합니다.", false); new AdminMainFrame(); dispose(); return; } 
        User loginUser = dao.loginAndGetUser(id, pw);
        if (loginUser != null) {
            if ("USER".equalsIgnoreCase(loginUser.getRole())) { showCustomDialog("관리 권한이 없는 계정입니다.", false); return; }
            UserManager.setCurrentUser(loginUser);
            Member m = convertToMember(loginUser);
            LoginSession.setUser(m);
            if ("ADMIN_COUNCIL".equals(m.getRole()) || "COUNCIL".equalsIgnoreCase(m.getRole())) { new CouncilMainFrame(m.getHakbun(), m.getMajor()); dispose(); }
            else if ("ADMIN_TOTAL".equals(m.getRole()) || "ADMIN".equalsIgnoreCase(m.getRole())) { new AdminMainFrame(); dispose(); }
        } else { showCustomDialog("관리자 인증에 실패했습니다.", false); }
    }

    private void handleFindPassword() {
        String name = findNameField.getText().trim();
        String hakbun = findHakbunField.getText().trim();
        String phone = findPhoneField.getText().trim();
        if (name.isEmpty() || hakbun.isEmpty() || phone.isEmpty()) { showCustomDialog("모든 정보를 입력해주세요.", false); return; }
        UserDAO dao = new UserDAO();
        String pw = dao.findPassword(name, hakbun, phone);
        if (pw != null) { showCustomDialog("비밀번호: " + pw, true); }
        else { showCustomDialog("정보와 일치하는 회원이 없습니다.", false); }
    }

    private void showCustomDialog(String message, boolean goBackToLogin) {
        JDialog dialog = new JDialog(this, "BeeHub 알림", true);
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        JTextPane msgPane = new JTextPane();
        msgPane.setText(message);
        msgPane.setFont(uiFont.deriveFont(19f));
        msgPane.setForeground(BROWN);
        msgPane.setEditable(false);
        msgPane.setOpaque(false);
        StyledDocument doc = msgPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        msgPane.setBounds(30, 50, 320, 80);
        panel.add(msgPane);

        JButton okBtn = createStyledButton("확인");
        okBtn.setFont(uiFont.deriveFont(16f));
        okBtn.setBounds(130, 140, 120, 45);
        okBtn.addActionListener(e -> { dialog.dispose(); if (goBackToLogin) cardLayout.show(containerPanel, "login"); });
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    class OutlinedLabel extends JLabel {
        private Color outlineColor = Color.WHITE;
        private float strokeWidth = 4f;
        public OutlinedLabel(String text, int alignment) { super(text, alignment); }
        public void setOutlineColor(Color color) { this.outlineColor = color; }
        public void setStrokeWidth(float w) { this.strokeWidth = w; }
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Font f = getFont(); g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            Shape textShape = f.createGlyphVector(g2.getFontRenderContext(), getText()).getOutline(x, y);
            if (strokeWidth > 0) {
                g2.setColor(outlineColor);
                g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(textShape);
            }
            g2.setColor(getForeground());
            g2.fill(textShape);
            g2.dispose();
        }
    }

    private JTextField addLabelAndField(JPanel p, String text, int y) {
        JLabel l = new JLabel(text); l.setFont(uiFont.deriveFont(16f));
        l.setForeground(BROWN); l.setBounds(70, y, 100, 30); p.add(l);
        JTextField f = createStyledTextField(); f.setBounds(160, y - 5, 250, 40); p.add(f);
        return f;
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color;
        public RoundedBorder(int r, Color c) { radius = r; color = c; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    private Member convertToMember(User user) {
        Member m = new Member();
        m.setHakbun(user.getId()); m.setPw(user.getPassword()); m.setName(user.getName());
        m.setMajor(user.getDept()); m.setPoint(user.getPoints()); m.setNickname(user.getNickname());
        if (m.getIsFeePaid() == null) m.setIsFeePaid("N");
        if (m.getGrade() == null) m.setGrade("일벌");
        m.setRole(user.getRole());
        return m;
    }
}