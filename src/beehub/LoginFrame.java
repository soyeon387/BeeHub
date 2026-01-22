package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*; 
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import admin.AdminMainFrame;
import council.CouncilMainFrame;

public class LoginFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final int FRAME_W = 500;
    private static final int FRAME_H = 700;
    
    private static final Color BG_YELLOW = new Color(255, 250, 200); 
    private static final Color DOT_COLOR = new Color(245, 230, 200); 
    
    private static final Color BROWN = new Color(90, 50, 20);       
    private static final Color SOFT_BROWN = new Color(140, 100, 70); 
    
    private static final Color INPUT_BG = new Color(255, 255, 250);  
    private static final Color INPUT_BORDER = new Color(210, 180, 150); 
    private static final Color POINT_ORANGE = new Color(255, 195, 40); 
    private static final Color EXIT_RED = new Color(255, 100, 100);

    // 꽃 색상
    private static final Color FLOWER_PINK   = new Color(255, 200, 200);
    private static final Color FLOWER_WHITE  = new Color(255, 255, 245);
    private static final Color FLOWER_BLUE   = new Color(210, 240, 250);
    private static final Color FLOWER_CENTER = new Color(255, 240, 100);
    
    // 🐝 벌 색상 추가
    private static final Color BEE_YELLOW = new Color(255, 220, 50);  // 쨍한 노랑
    private static final Color BEE_STRIPE = new Color(60, 30, 10);    // 진한 고동색
    private static final Color BEE_WING   = new Color(255, 255, 255, 200); // 반투명 날개

    private static Font uiFont;
    private int mouseX, mouseY;

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
        setUndecorated(true);
        setTitle("서울여대 꿀단지");
        setSize(FRAME_W, FRAME_H);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 1. 프레임 모양 자체를 라운드로 깎기
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, FRAME_W, FRAME_H, 40, 40));

        cardLayout = new CardLayout();
        
        // 2. 테두리 선을 직접 그리는 컨테이너 패널
     // LoginFrame 생성자 내부의 containerPanel 부분을 이렇게 단순화하세요.
        containerPanel = new JPanel(cardLayout);
        containerPanel.setBorder(null); 
        containerPanel.setOpaque(false);
        
        // 기존의 검은 선 Border는 제거
        containerPanel.setBorder(null); 
        containerPanel.setOpaque(false); // 배경을 투명하게 해서 라운드가 잘 보이게 함

        containerPanel.add(createLoginPanel(), "login");
        containerPanel.add(createFindPwPanel(), "findPw");
        containerPanel.add(createAdminPanel(), "admin");

        add(containerPanel);
        setVisible(true);
    }
    // ===============================================================
    // 🎨 배경 패널 (꽃 + 🐝 귀여운 꿀벌 추가)
    // ===============================================================
 // ===============================================================
 // 🎨 배경 패널 (꽃, 벌 제거 및 얇은 테두리 적용)
 // ===============================================================
 private JPanel createBackgroundPanel() {
     return new JPanel() {
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D) g;
             // 선을 부드럽게 그리기 위한 설정
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             
             int w = getWidth();
             int h = getHeight();

             // 1. 배경색 채우기
             g2.setColor(BG_YELLOW); 
             g2.fillRect(0, 0, w, h);
             
             // 2. 격자 도트 패턴 그리기 (심심하지 않게 패턴은 유지)
             g2.setColor(DOT_COLOR);
             int dotSize = 6; 
             int gap = 30;     
             for (int y = 0; y < h; y += gap) {
                 for (int x = 0; x < w; x += gap) {
                     g2.fillRect(x, y, dotSize, dotSize);
                 }
             }

             // 3. 상단 그라데이션 효과 (유지)
             GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 150), 0, 150, new Color(255, 255, 255, 0));
             g2.setPaint(gp);
             g2.fillRect(0, 0, w, 150);

             // (꽃, 벌, 궤적 그리는 코드는 모두 제거했습니다)

             // ✅ [수정] 얇은 둥근 테두리 선 그리기
             g2.setColor(BROWN);
             g2.setStroke(new BasicStroke(2f)); // 두께를 2f로 얇게 설정
             
             // 프레임 가장자리에 맞춰 안쪽으로 살짝 들여서 그립니다.
             // (좌표 2,2에서 시작, 너비/높이에서 5만큼 뺌, 곡률 40)
             g2.drawRoundRect(2, 2, w - 5, h - 5, 40, 40);
         }
         // (내부에 있던 drawSimpleBee, drawDotFlower 등의 메서드도 사용하지 않으므로 제거했습니다)
     };
 }

            

    private void centerComponent(JComponent comp, int y, int w, int h) {
        int x = (FRAME_W - w) / 2;
        comp.setBounds(x, y, w, h);
    }

    private void showExitDialog() {
        JDialog dialog = new JDialog(this, "종료", true);
        dialog.setUndecorated(true);
        dialog.setSize(300, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_YELLOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 25, 25);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("프로그램을 종료하시겠습니까?");
        msgLabel.setFont(uiFont.deriveFont(15f));
        msgLabel.setForeground(BROWN);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        msgLabel.setBounds(0, 50, 300, 30);
        panel.add(msgLabel);

        JButton yesBtn = createStyledButton("네");
        yesBtn.setFont(uiFont.deriveFont(14f));
        yesBtn.setBounds(55, 110, 85, 40);
        yesBtn.addActionListener(e -> System.exit(0));
        panel.add(yesBtn);

        JButton noBtn = createSmallButton("아니오");
        noBtn.setFont(uiFont.deriveFont(14f));
        noBtn.setBounds(160, 110, 85, 40);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    // ===============================================================
    // 1️⃣ 일반 로그인 패널 (타이틀 외곽선 수정됨)
    // ===============================================================
    private JPanel createLoginPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);
        enableDrag(panel);
        panel.add(createCloseButton());

        // ✅ [위치 변경 1] 메인 타이틀을 위로 (Y: 60)
        OutlinedLabel title = new OutlinedLabel("서울여대 꿀단지", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(46f));
        title.setForeground(BROWN);
        title.setOutlineColor(Color.WHITE); 
        title.setStrokeWidth(0f); 
        centerComponent(title, 60, 450, 70); 
        panel.add(title);

        // ✅ [위치 변경 2] 서브 레이블을 아래로 (Y: 130)
        JLabel subLabel = new JLabel("슈니만을 위한 학교 생활 도우미", SwingConstants.CENTER);
        subLabel.setFont(uiFont.deriveFont(Font.PLAIN, 14f));
        subLabel.setForeground(SOFT_BROWN);
        centerComponent(subLabel, 130, 450, 25);
        panel.add(subLabel);

        // 입력 필드 시작 위치 조정 (startY를 조금 내림)
        int startY = 210; 
        int gapY = 125; 

        // 1. 학번 영역
        JLabel idLabel = new JLabel("학번 (ID)");
        idLabel.setFont(uiFont.deriveFont(17f));
        idLabel.setForeground(BROWN);
        centerComponent(idLabel, startY, 330, 30);
        idLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(idLabel);

        hakbunField = createStyledTextField();
        centerComponent(hakbunField, startY + 35, 330, 48);
        hakbunField.addActionListener(e -> handleUserLogin());
        panel.add(hakbunField);

        // 2. 비밀번호 영역
        JLabel pwLabel = new JLabel("비밀번호 (PW)");
        pwLabel.setFont(uiFont.deriveFont(17f));
        pwLabel.setForeground(BROWN);
        centerComponent(pwLabel, startY + gapY, 330, 30);
        pwLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(pwLabel);

        pwField = createStyledPasswordField();
        centerComponent(pwField, startY + gapY + 35, 330, 48);
        pwField.addActionListener(e -> handleUserLogin());
        panel.add(pwField);

        // 3. 버튼 영역
        JButton findPwBtn = createTextButton("비밀번호를 잊으셨나요?");
        findPwBtn.setBounds(415 - 200, startY + gapY + 85, 200, 30); 
        findPwBtn.setHorizontalAlignment(SwingConstants.RIGHT);
        findPwBtn.addActionListener(e -> cardLayout.show(containerPanel, "findPw"));
        panel.add(findPwBtn);

        JButton loginBtn = createStyledButton("로그인");
        centerComponent(loginBtn, 530, 330, 55);
        loginBtn.addActionListener(e -> handleUserLogin());
        panel.add(loginBtn);

        JButton adminBtn = createSmallButton("관리자 모드");
        centerComponent(adminBtn, 645, 110, 32);
        adminBtn.addActionListener(e -> cardLayout.show(containerPanel, "admin"));
        panel.add(adminBtn);

        return panel;
    }

    // ===============================================================
    // 2️⃣ 비밀번호 찾기 패널
    // ===============================================================
    private JPanel createFindPwPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);
        enableDrag(panel);
        panel.add(createCloseButton());

        OutlinedLabel title = new OutlinedLabel("비밀번호 찾기", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(34f));
        title.setForeground(BROWN);
        // 여기도 통일감을 위해 외곽선 추가 (선택사항)
        title.setOutlineColor(Color.WHITE);
        title.setStrokeWidth(4f);
        centerComponent(title, 65, 400, 50);
        panel.add(title);

        int startY = 155;
        int gap = 85;

        findNameField   = addCenteredLabelAndField(panel, "이름",     startY);
        findHakbunField = addCenteredLabelAndField(panel, "학번",     startY + gap);
        findPhoneField  = addCenteredLabelAndField(panel, "전화번호", startY + gap * 2);

        JButton cancelBtn = createSmallButton("취소");
        cancelBtn.setBounds(85, 460, 155, 52);
        cancelBtn.addActionListener(e -> {
            clearFields(); // ✅ 화면 나갈 때 입력했던 내용 싹 지우기
            cardLayout.show(containerPanel, "login");
        });
        panel.add(cancelBtn);

        JButton confirmBtn = createStyledButton("확인");
        confirmBtn.setBounds(260, 460, 155, 52);
        confirmBtn.addActionListener(e -> handleFindPassword());
        panel.add(confirmBtn);

        return panel;
    }

    // ===============================================================
    // 3️⃣ 관리자 패널
    // ===============================================================
    private JPanel createAdminPanel() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(null);
        enableDrag(panel);
        panel.add(createCloseButton());

        JLabel subTitle = new JLabel("관리자 모니터링 시스템", SwingConstants.CENTER);
        subTitle.setFont(uiFont.deriveFont(14f));
        subTitle.setForeground(SOFT_BROWN);
        centerComponent(subTitle, 55, 400, 30);
        panel.add(subTitle);

        OutlinedLabel title = new OutlinedLabel("ADMIN LOGIN", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(38f));
        title.setForeground(BROWN);
        // 여기도 통일감을 위해 외곽선 추가 (선택사항)
        title.setOutlineColor(Color.WHITE);
        title.setStrokeWidth(4f);
        centerComponent(title, 85, 400, 60);
        panel.add(title);

        int startY = 195;
        int gapY = 95;

        JLabel idLabel = new JLabel("관리자 ID");
        idLabel.setFont(uiFont.deriveFont(17f));
        idLabel.setForeground(BROWN);
        centerComponent(idLabel, startY, 330, 30);
        idLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(idLabel);

        adminIdField = createStyledTextField();
        centerComponent(adminIdField, startY + 35, 330, 48);
        adminIdField.addActionListener(e -> handleAdminLogin());
        panel.add(adminIdField);

        JLabel pwLabel = new JLabel("비밀번호");
        pwLabel.setFont(uiFont.deriveFont(17f));
        pwLabel.setForeground(BROWN);
        centerComponent(pwLabel, startY + gapY, 330, 30);
        pwLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(pwLabel);

        adminPwField = createStyledPasswordField();
        centerComponent(adminPwField, startY + gapY + 35, 330, 48);
        adminPwField.addActionListener(e -> handleAdminLogin());
        panel.add(adminPwField);

        JButton loginBtn = createStyledButton("시스템 접속");
        centerComponent(loginBtn, 480, 330, 58);
        loginBtn.addActionListener(e -> handleAdminLogin());
        panel.add(loginBtn);
        
        JButton backBtn = createSmallButton("학생 로그인으로");
        centerComponent(backBtn, 615, 140, 35);
        backBtn.addActionListener(e -> cardLayout.show(containerPanel, "login"));
        panel.add(backBtn);

        return panel;
    }

    // ===============================================================
    // 🛠️ UI 컴포넌트
    // ===============================================================
    private void enableDrag(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + (e.getX() - mouseX), getLocation().y + (e.getY() - mouseY));
            }
        });
    }

    private JButton createCloseButton() {
        JButton btn = new JButton("X");
        btn.setFont(uiFont.deriveFont(Font.BOLD, 15f));
        btn.setBounds(FRAME_W - 55, 15, 40, 40);
        btn.setBackground(new Color(0,0,0,0));
        btn.setForeground(BROWN);
        btn.setBorder(new RoundedBorder(12, BROWN));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(EXIT_RED);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new RoundedBorder(12, EXIT_RED));
            }
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setBackground(new Color(0,0,0,0));
                btn.setForeground(BROWN);
                btn.setBorder(new RoundedBorder(12, BROWN));
            }
        });
        btn.addActionListener(e -> showExitDialog()); 
        return btn;
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(uiFont.deriveFont(16f));
        f.setBackground(INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, INPUT_BORDER),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(12, POINT_ORANGE), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
            public void focusLost(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(12, INPUT_BORDER), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
        });
        return f;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setEchoChar('●'); 
        f.setFont(uiFont.deriveFont(16f));
        f.setBackground(INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, INPUT_BORDER),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(12, POINT_ORANGE), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
            public void focusLost(FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(12, INPUT_BORDER), BorderFactory.createEmptyBorder(5, 15, 5, 15))); }
        });
        return f;
    }

    private JTextField addCenteredLabelAndField(JPanel p, String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(16f));
        l.setForeground(BROWN);
        centerComponent(l, y, 330, 25);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        p.add(l);
        
        JTextField f = createStyledTextField();
        centerComponent(f, y + 28, 330, 48);
        p.add(f);
        return f;
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(Font.BOLD, 20f));
        b.setBackground(BROWN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(18, BROWN));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(SOFT_BROWN); }
            public void mouseExited(MouseEvent e) { b.setBackground(BROWN); }
        });
        return b;
    }

    private JButton createSmallButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 마우스 올렸을 때 피드백 (선택사항)
                if (getModel().isArmed()) {
                    g2.setColor(INPUT_BORDER);
                } else {
                    g2.setColor(getBackground());
                }
                
                // 버튼 배경 그리기
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        b.setFont(uiFont.deriveFont(17f));
        b.setBackground(new Color(255, 255, 255, 200)); // 투명도를 약간 높임
        b.setForeground(BROWN);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false); // ✅ 중요: 이 설정이 있어야 잔상이 안 남음
        b.setBorder(new RoundedBorder(12, INPUT_BORDER));
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

    // ===============================================================
    // 기능 로직
    // ===============================================================
    private void handleUserLogin() {
        String id = hakbunField.getText().trim();
        String pw = new String(pwField.getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) { showCustomAlert("아이디와 비밀번호를 \n모두 입력해주세요."); return; }
        UserDAO dao = new UserDAO();
        User loginUser = dao.loginAndGetUser(id, pw);
        if (loginUser != null) {
            if (!"USER".equalsIgnoreCase(loginUser.getRole())) { showCustomAlert("관리자 로그인 페이지를 이용해주세요."); return; }
            UserManager.setCurrentUser(loginUser);
            Member m = convertToMember(loginUser);
            LoginSession.setUser(m);
            new MainFrame(m.getName(), m.getHakbun());
            dispose();
        } else { showCustomAlert("정보가 일치하지 않습니다.\n다시 확인해주세요."); }
    }

    private void handleAdminLogin() {
        String id = adminIdField.getText().trim();
        String pw = new String(adminPwField.getPassword()).trim();
        if(id.isEmpty() || pw.isEmpty()) { showCustomAlert("관리자 정보를 입력해주세요."); return; }
        UserDAO dao = new UserDAO();
        if (dao.checkAdminLogin(id, pw)) { new AdminMainFrame(); dispose(); return; } 
        User loginUser = dao.loginAndGetUser(id, pw);
        if (loginUser != null) {
            if ("USER".equalsIgnoreCase(loginUser.getRole())) { showCustomAlert("관리 권한이 없는 계정입니다."); return; }
            UserManager.setCurrentUser(loginUser);
            Member m = convertToMember(loginUser);
            LoginSession.setUser(m);
            if ("ADMIN_COUNCIL".equals(m.getRole()) || "COUNCIL".equalsIgnoreCase(m.getRole())) { new CouncilMainFrame(m.getHakbun(), m.getMajor()); dispose(); }
            else if ("ADMIN_TOTAL".equals(m.getRole()) || "ADMIN".equalsIgnoreCase(m.getRole())) { new AdminMainFrame(); dispose(); }
        } else { showCustomAlert("관리자 인증에 실패했습니다."); }
    }

    private void handleFindPassword() {
        String name = findNameField.getText().trim();
        String hakbun = findHakbunField.getText().trim();
        String phone = findPhoneField.getText().trim();
        
        // 입력 검증
        if (name.isEmpty() || hakbun.isEmpty() || phone.isEmpty()) { 
            showCustomAlert("모든 정보를 입력해주세요."); 
            return; 
        }
        
        UserDAO dao = new UserDAO();
        String pw = dao.findPassword(name, hakbun, phone);
        
        if (pw != null) { 
            // 1. 비밀번호 알림창 표시
            showCustomAlert("비밀번호: " + pw); 
            
            // 2. [수정] 취소 버튼과 동일하게 모든 필드 초기화 및 화면 전환
            clearFields(); 
            cardLayout.show(containerPanel, "login"); 
        } 
        else { 
            showCustomAlert("정보와 일치하는 회원이 없습니다."); 
            // 실패했을 때는 다시 입력할 수 있도록 화면을 유지하는 것이 일반적입니다.
        }
    }

    private void showCustomAlert(String message) {
        JDialog dialog = new JDialog(this, "알림", true);
        dialog.setUndecorated(true);
        dialog.setSize(380, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_YELLOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 25, 25);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        JTextPane msgPane = new JTextPane();
        msgPane.setText(message);
        msgPane.setFont(uiFont.deriveFont(17f));
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
        okBtn.setFont(uiFont.deriveFont(15f));
        okBtn.setBounds(130, 130, 120, 45);
        okBtn.addActionListener(e -> dialog.dispose());
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
    
    private void clearFields() {
        if (hakbunField != null) hakbunField.setText("");
        if (pwField != null) pwField.setText("");
        if (findNameField != null) findNameField.setText("");
        if (findHakbunField != null) findHakbunField.setText("");
        if (findPhoneField != null) findPhoneField.setText("");
        if (adminIdField != null) adminIdField.setText("");
        if (adminPwField != null) adminPwField.setText("");
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