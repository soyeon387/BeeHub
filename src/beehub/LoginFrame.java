package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*; 
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL; // URL 클래스 추가
import java.sql.Statement;
import admin.AdminMainFrame;
import council.CouncilMainFrame;

public class LoginFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color BG_YELLOW = new Color(255, 250, 205); 
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color INPUT_BG = new Color(255, 255, 255);
    private static final Color GRAY = new Color(200, 200, 200);

    // ===============================
    // 🔤 폰트 설정 (리소스 로드 방식 통일)
    // ===============================
    private static Font uiFont;

    static {
        try {
            // [수정] 클래스패스 리소스 로딩 방식 사용
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

    // ===============================
    // UI 컴포넌트
    // ===============================
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
    // 1️⃣ 일반 로그인 화면
    // ===============================================================
    private JPanel createLoginPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);

        // [수정] 이미지 로딩 방식 통일 (getResource 사용)
        JLabel beeIcon = new JLabel();
        URL imgUrl = getClass().getResource("/img/login-bee.png");
        
        if (imgUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imgUrl);
            Image img = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            beeIcon.setIcon(new ImageIcon(img));
            beeIcon.setBounds(380, 20, 100, 100); 
        } else {
            // 이미지가 없을 경우 대체 텍스트
            beeIcon.setText("🐝");
            beeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
            beeIcon.setBounds(400, 30, 80, 80);
        }
        panel.add(beeIcon);

        // 타이틀
        OutlinedLabel title = new OutlinedLabel("서울여대 꿀단지", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(45f));
        title.setForeground(BROWN);
        title.setOutlineColor(Color.WHITE);
        title.setStrokeWidth(8f);
        title.setBounds(25, 100, 450, 80);
        panel.add(title);

        // 입력창
        JLabel idLabel = new JLabel("아이디 :");
        idLabel.setFont(uiFont.deriveFont(20f));
        idLabel.setForeground(BROWN);
        idLabel.setBounds(80, 230, 150, 30);
        panel.add(idLabel);

        hakbunField = createStyledTextField();
        hakbunField.setBounds(80, 265, 340, 50);
        hakbunField.addActionListener(e -> handleUserLogin());
        panel.add(hakbunField);

        JLabel pwLabel = new JLabel("비밀번호 :");
        pwLabel.setFont(uiFont.deriveFont(20f));
        pwLabel.setForeground(BROWN);
        pwLabel.setBounds(80, 340, 150, 30);
        panel.add(pwLabel);

        pwField = createStyledPasswordField();
        pwField.setBounds(80, 375, 340, 50);
        pwField.addActionListener(e -> handleUserLogin());
        panel.add(pwField);

        JButton findPwBtn = createTextButton("비밀번호 찾기");
        findPwBtn.setBounds(300, 435, 130, 30);
        findPwBtn.addActionListener(e -> cardLayout.show(containerPanel, "findPw"));
        panel.add(findPwBtn);

        JButton loginBtn = createStyledButton("로그인");
        loginBtn.setBounds(100, 500, 300, 60);
        loginBtn.addActionListener(e -> handleUserLogin());
        panel.add(loginBtn);

        JButton adminBtn = createSmallButton("관리자 로그인");
        adminBtn.setBounds(340, 600, 120, 40);
        adminBtn.setFont(uiFont.deriveFont(14f));
        adminBtn.addActionListener(e -> cardLayout.show(containerPanel, "admin"));
        panel.add(adminBtn);

        return panel;
    }

    // ===============================================================
    // 2️⃣ 비밀번호 찾기 화면
    // ===============================================================
    private JPanel createFindPwPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);

        OutlinedLabel title = new OutlinedLabel("비밀번호 찾기", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(38f));
        title.setForeground(BROWN);
        title.setOutlineColor(Color.WHITE);
        title.setStrokeWidth(7f);
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

    // ===============================================================
    // 3️⃣ 관리자 로그인 화면
    // ===============================================================
    private JPanel createAdminPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);

        // [수정] 이미지 로딩 방식 통일 (getResource 사용)
        JLabel beeIcon = new JLabel();
        URL imgUrl = getClass().getResource("/img/login-bee.png");

        if (imgUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imgUrl);
            Image img = originalIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            beeIcon.setIcon(new ImageIcon(img));
            beeIcon.setBounds(410, 10, 70, 70);
        } else {
            beeIcon.setText("🐝");
            beeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            beeIcon.setBounds(420, 20, 50, 50);
        }
        panel.add(beeIcon);

        JLabel subTitle = new JLabel("관리자 VER", SwingConstants.CENTER);
        subTitle.setFont(uiFont.deriveFont(20f));
        subTitle.setForeground(BROWN);
        subTitle.setOpaque(true);
        subTitle.setBackground(Color.WHITE);
        subTitle.setBorder(new RoundedBorder(10, BROWN));
        subTitle.setBounds(190, 60, 120, 35);
        panel.add(subTitle);

        OutlinedLabel title = new OutlinedLabel("서울여대 꿀단지", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(40f));
        title.setForeground(BROWN);
        title.setOutlineColor(Color.WHITE);
        title.setStrokeWidth(7f);
        title.setBounds(50, 110, 400, 60);
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

        JButton loginBtn = createStyledButton("로그인");
        loginBtn.setBounds(100, 460, 300, 60);
        loginBtn.addActionListener(e -> handleAdminLogin());
        panel.add(loginBtn);
        
        JButton backBtn = createSmallButton("뒤로가기");
        backBtn.setBounds(380, 600, 90, 50);
        backBtn.addActionListener(e -> cardLayout.show(containerPanel, "login"));
        panel.add(backBtn);

        return panel;
    }

 // ===============================================================
 // 💾 일반 사용자 로그인 처리
 // ===============================================================
    private void handleUserLogin() {
        String id = hakbunField.getText().trim();
        String pw = new String(pwField.getPassword()).trim();

        if (id.isEmpty() || pw.isEmpty()) {
            showCustomDialog("아이디와 비밀번호를\n모두 입력해주세요.", false);
            return;
        }

        UserDAO dao = new UserDAO();

        // 🔹 DB에서 로그인 시도 후 User 객체 받아오기
        User loginUser = dao.loginAndGetUser(id, pw);

        if (loginUser != null) {
            // 기존 UserManager 유지
            UserManager.setCurrentUser(loginUser);

            // ⭐ User → Member 로 변환해서 세션에 저장
            Member m = convertToMember(loginUser);
            LoginSession.setUser(m);

            // 🔥 role 에 따라 화면 분기
            String role = m.getRole();   // members 테이블의 role 값

            if ("ADMIN_COUNCIL".equals(role) || "COUNCIL".equalsIgnoreCase(role)) {
                // 👉 학생회 계정: CouncilMainFrame 으로
                //   id = 학생회 아이디(hakbun), name = 학과명(major)
                new CouncilMainFrame(m.getHakbun(), m.getMajor());

            } else if ("ADMIN_TOTAL".equals(role) || "ADMIN".equalsIgnoreCase(role)) {
                // 👉 총관리자 계정: AdminMainFrame
                new AdminMainFrame();

            } else {
                // 👉 일반 회원: 기존 메인 화면
                new MainFrame(m.getName(), m.getHakbun());
            }

            dispose();   // 로그인창 닫기

        } else {
            showCustomDialog("로그인 실패\n아이디 또는 비밀번호를 확인하세요.", false);
        }
    }


 // 💾 비밀번호 찾기 처리
    private void handleFindPassword() {
        String name   = findNameField.getText().trim();
        String hakbun = findHakbunField.getText().trim();
        String phone  = findPhoneField.getText().trim();

        if (name.isEmpty() || hakbun.isEmpty() || phone.isEmpty()) {
            showCustomDialog("이름, 학번, 전화번호를\n모두 입력해주세요.", false);
            return;
        }

        UserDAO dao = new UserDAO();
        String pw = dao.findPassword(name, hakbun, phone);  // ← UserDAO에서 DB 조회

        if (pw != null) {
            showCustomDialog("비밀번호는\n" + pw + " 입니다.", true);  // 확인 누르면 로그인 화면으로
        } else {
            showCustomDialog("일치하는 회원 정보가 없습니다.", false);
        }
    }

    
    private void handleAdminLogin() {
        String id = adminIdField.getText().trim();
        String pw = new String(adminPwField.getPassword()).trim();
        
        if(id.isEmpty() || pw.isEmpty()) {
            showCustomDialog("관리자 정보를 입력해주세요.", false);
            return;
        }

        UserDAO dao = new UserDAO();
        
        if (dao.checkAdminLogin(id, pw)) {
            showCustomDialog("총 관리자님 환영합니다!", false);
            new admin.AdminMainFrame(); 
            dispose();
            return;
        } 
        
        UserDAO.CouncilInfo council = dao.getCouncilInfo(id, pw);
        if (council != null) {
            new council.CouncilMainFrame(council.id, council.name); 
            dispose();
            return;
        }

        showCustomDialog("로그인 실패\n정보를 확인해주세요.", false);
    }

    // ===============================================================
    // 🎨 예쁜 커스텀 팝업창
    // ===============================================================
    private void showCustomDialog(String message, boolean goBackToLogin) {
        JDialog dialog = new JDialog(this, "알림", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_YELLOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setOpaque(false);
        textPanel.setBounds(30, 40, 340, 110); 
        panel.add(textPanel);

        JTextPane msgPane = new JTextPane();
        msgPane.setText(message);
        msgPane.setFont(uiFont.deriveFont(20f));
        msgPane.setForeground(BROWN);
        msgPane.setOpaque(false);
        msgPane.setEditable(false);
        msgPane.setFocusable(false);
        
        StyledDocument doc = msgPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        textPanel.add(msgPane);

        JButton okBtn = createStyledButton("확인");
        okBtn.setFont(uiFont.deriveFont(18f));
        okBtn.setBounds(120, 160, 160, 50);
        okBtn.addActionListener(e -> {
            dialog.dispose();
            if (goBackToLogin) {
                cardLayout.show(containerPanel, "login");
            }
        });
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    // ===============================================================
    // 🛠️ 공통 UI Helper
    // ===============================================================
    class OutlinedLabel extends JLabel {
        private Color outlineColor = Color.WHITE;
        private float strokeWidth = 4f;

        public OutlinedLabel(String text, int alignment) { super(text, alignment); }
        public void setOutlineColor(Color color) { this.outlineColor = color; }
        public void setStrokeWidth(float w) { this.strokeWidth = w; }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Font f = getFont();
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            Shape textShape = f.createGlyphVector(g2.getFontRenderContext(), getText()).getOutline(x, y);
            
            g2.setColor(outlineColor);
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(textShape);
            g2.setColor(getForeground());
            g2.fill(textShape);
            g2.dispose();
        }
    }

    private JPanel createBackgroundPanel() {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BG_YELLOW);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(new Color(255, 235, 59, 50));
                g2d.setStroke(new BasicStroke(3));
                int size = 70;
                for (int row = -1; row < 12; row++) {
                    for (int col = -1; col < 8; col++) {
                        int x = col * size * 3 / 2;
                        int y = (int) (row * size * Math.sqrt(3));
                        if (col % 2 == 1) y += (int) (size * Math.sqrt(3) / 2);
                        Polygon hex = new Polygon();
                        for (int i = 0; i < 6; i++) {
                            hex.addPoint((int)(x + size * Math.cos(Math.PI/3*i)), (int)(y + size * Math.sin(Math.PI/3*i)));
                        }
                        g2d.draw(hex);
                    }
                }
            }
        };
    }

 // 🔥 리턴 타입을 void → JTextField 로 변경
    private JTextField addLabelAndField(JPanel p, String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(18f));
        l.setForeground(BROWN);
        l.setBounds(70, y, 100, 30);
        p.add(l);

        JTextField f = createStyledTextField();
        f.setBounds(160, y - 5, 250, 40);
        p.add(f);

        return f;   // 이제 정상
    }


    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(uiFont.deriveFont(18f));
        f.setBackground(INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, GRAY), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
      
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(uiFont.deriveFont(18f));
        f.setBackground(INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, GRAY), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(24f));
        b.setBackground(BROWN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(20, BROWN));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createSmallButton(String text) {
        JButton b = new JButton(text); 
        b.setFont(uiFont.deriveFont(14f));
        b.setBackground(GRAY);
        b.setForeground(BROWN);
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(15, GRAY));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createTextButton(String text) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(14f));
        b.setContentAreaFilled(false);
        b.setBorder(null);
        b.setForeground(BROWN);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
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

        // 기본 정보
        m.setHakbun(user.getId());
        m.setPw(user.getPassword());
        m.setName(user.getName());
        m.setMajor(user.getDept());
        m.setPoint(user.getPoints());

        // ⭐ 닉네임 그대로 넣기 (기존 null 삭제)
        m.setNickname(user.getNickname());

        // 기본값 처리
        if (m.getIsFeePaid() == null) m.setIsFeePaid("N");
        if (m.getGrade() == null)     m.setGrade("일벌");
        if (m.getPenaltyDate() == null) m.setPenaltyDate(null);
        if (m.getWarningCount() == 0) m.setWarningCount(0);

        // 권한
        m.setRole(user.getRole());

        return m;
    }



    }