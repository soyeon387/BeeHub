package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import beehub.CommunityFrame.Post;

public class CommunityWriteFrame extends JFrame {

    // 🎨 컬러 테마
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG        = new Color(255, 255, 255);
    private static final Color BG_MAIN       = new Color(255, 255, 255);
    private static final Color BROWN         = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color BORDER_COLOR  = new Color(220, 220, 220);
    private static final Color POPUP_BG      = new Color(255, 250, 205); 

    private static Font uiFont;

    // 폰트 로드
    static {
        try {
            File fontFile = new File("resource/fonts/DNFBitBitv2.ttf");
            if (fontFile.exists()) {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);
            } else {
                InputStream is = CommunityWriteFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
                if (is != null) {
                    uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                } else {
                    uiFont = new Font("SansSerif", Font.PLAIN, 14);
                }
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
            e.printStackTrace();
        }
    }

    private String currentUser;                
    private CommunityFrame.Post postToEdit;    

    private JTextField titleField;
    private JTextArea  contentArea;
    private CommunityDAO communityDAO = new CommunityDAO();

    // 생성자 (새 글 작성)
    public CommunityWriteFrame(String user) {
        this(user, null);
    }

    // 생성자 (글 수정)
    public CommunityWriteFrame(String user, CommunityFrame.Post postToEdit) {
        this.currentUser = user;
        this.postToEdit = postToEdit;

        setTitle(postToEdit != null ? "서울여대 꿀단지 - 게시글 수정" : "서울여대 꿀단지 - 게시글 작성");
        // ✅ [수정] 프레임 크기 변경 (850 -> 800)
        setSize(800, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initHeader();
        initNav();
        initContent();

        setVisible(true);
    }

    // 1. 헤더
    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
        // ✅ [수정] 너비 800으로 조정
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
                new MainFrame();
                dispose();
            }
        });

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        // ✅ [수정] 우측 정렬 위치 조정 (800 - 350 = 450)
        userInfoPanel.setBounds(430, 0, 350, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfoText = new JLabel("[" + currentUser + "]님" +  " | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });
        
        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);
    }

    // 2. 네비게이션 바
    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        // ✅ [수정] 너비 800
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("커뮤니티"));
            navPanel.add(menuBtn);
        }
    }

    // 3. 메인 콘텐츠
    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        // ✅ [수정] 너비 800
        contentPanel.setBounds(0, 130, 800, 520);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        JLabel pageTitle = new JLabel(postToEdit != null ? "> 게시글 수정" : "> 게시글 작성");
        pageTitle.setFont(uiFont.deriveFont(20f));
        pageTitle.setForeground(BROWN);
        pageTitle.setBounds(40, 10, 200, 30);
        contentPanel.add(pageTitle);

        JLabel titleLabel = new JLabel("제목");
        titleLabel.setFont(uiFont.deriveFont(16f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(40, 50, 50, 30);
        contentPanel.add(titleLabel);

        titleField = new JTextField();
        titleField.setFont(uiFont.deriveFont(16f));
        // ✅ [수정] 너비 줄임 (700 -> 660)
        titleField.setBounds(90, 50, 660, 35);
        titleField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        contentPanel.add(titleField);

        contentArea = new JTextArea();
        contentArea.setFont(uiFont.deriveFont(16f));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(contentArea);
        // ✅ [수정] 스크롤 패널 너비 조정 (750 -> 710)
        scrollPane.setBounds(40, 100, 710, 320);
        scrollPane.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        contentPanel.add(scrollPane);

        if (postToEdit != null) {
            titleField.setText(postToEdit.title);
            contentArea.setText(postToEdit.content);
        }

        // 취소 버튼 (이전으로)
        JButton cancelBtn = createStyledButton("이전으로", 100, 45);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setForeground(BROWN);
        // ✅ [수정] 버튼 위치 조정 (800폭 기준 우측 정렬)
        // Submit(600) - Gap(10) - Cancel(100) = 490
        cancelBtn.setBounds(490, 430, 100, 45);
        cancelBtn.addActionListener(e -> {
            showCustomConfirmPopup("작성을 취소하고 돌아가시겠습니까?", () -> {
                new CommunityFrame(); 
                dispose();
            });
        });
        contentPanel.add(cancelBtn);

        // 등록/수정 버튼
        JButton submitBtn = createStyledButton(postToEdit != null ? "수정완료" : "등록하기", 150, 45);
        // ✅ [수정] 버튼 위치 조정 (800 - 40 - 150 = 610)
        submitBtn.setBounds(600, 430, 150, 45);
        submitBtn.addActionListener(e -> handleSubmit());
        contentPanel.add(submitBtn);
    }

    private void handleSubmit() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || title.length() > 20) {
            showCustomAlertPopup("경고", "제목은 1자 이상 20자 이내여야 합니다.");
            return;
        }
        if (content.isEmpty() || content.length() > 500) {
            showCustomAlertPopup("경고", "내용은 1자 이상 500자 이내여야 합니다.");
            return;
        }

        Member loginUser = LoginSession.getUser();
        if (loginUser == null) {
            showCustomAlertPopup("오류", "로그인 정보가 없습니다.");
            return;
        }

        String confirmMsg = (postToEdit != null) ? "게시글을 수정하시겠습니까?" : "게시글을 등록하시겠습니까?";
        showCustomConfirmPopup(confirmMsg, () -> {
            if (postToEdit != null) {
                // 수정
                communityDAO.updatePost(postToEdit.no, title, content);
                showCustomAlertPopup("알림", "수정되었습니다.", () -> {
                    new CommunityFrame();
                    dispose();
                });
            } else {
                // 등록
                String nick = (loginUser.getNickname() != null) ? loginUser.getNickname() : loginUser.getName();
                communityDAO.insertPost(loginUser.getHakbun(), nick, title, content);
                showCustomAlertPopup("알림", "등록되었습니다.", () -> {
                    new CommunityFrame();
                    dispose();
                });
            }
        });
    }

    // --- UI 및 팝업 헬퍼 메소드 ---

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
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
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

    private void showCustomAlertPopup(String title, String message) {
        showCustomAlertPopup(title, message, null);
    }
    
    private void showCustomAlertPopup(String title, String message, Runnable onOk) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        // HTML 태그 제거
        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f)); 
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 60, 360, 80);
        panel.add(msgLabel);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.addActionListener(e -> {
            dialog.dispose();
            if (onOk != null) onOk.run();
        });
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private void showCustomConfirmPopup(String message, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, "확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        // HTML 태그 제거
        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f)); 
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    private void showLogoutPopup() {
        showCustomConfirmPopup("로그아웃 하시겠습니까?", () -> {
            new LoginFrame();
            dispose();
        });
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setForeground(BROWN);
        btn.setBackground(isActive ? HIGHLIGHT_YELLOW : NAV_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            if (text.equals("글쓰기")) return;
            
            if (text.equals("커뮤니티")) new CommunityFrame();
            else if (text.equals("빈 강의실")) new EmptyClassFrame();
            else if (text.equals("공간대여")) new SpaceRentFrame();
            else if (text.equals("물품대여")) new ItemListFrame();
            else if (text.equals("과행사")) new EventListFrame();
            else if (text.equals("마이페이지")) new MyPageFrame();
            else return;

            dispose();
        });
        return btn;
    }

    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(18f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int r, Color c, int t) { radius = r; color = c; thickness = t; }
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
}