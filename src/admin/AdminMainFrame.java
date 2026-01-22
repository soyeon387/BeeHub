package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import beehub.LoginFrame; 

public class AdminMainFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color BG_YELLOW = new Color(255, 250, 205);
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BROWN = new Color(139, 90, 43);
    
    private static Font uiFont;

    static {
        try {
            InputStream is = AdminMainFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.BOLD, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.BOLD, 12);
        }
    }

    public AdminMainFrame() {
        setTitle("서울여대 꿀단지 - 총 관리자");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_YELLOW);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        // --- 상단 헤더 ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지 [관리자]");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 400, 40);
        headerPanel.add(logoLabel);

        // [수정] 로그아웃 버튼 (확인 팝업 연결)
        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.setFont(uiFont.deriveFont(14f));
        logoutBtn.setBackground(BROWN);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBounds(680, 25, 90, 35);
        logoutBtn.setBorder(new RoundedBorder(15, BROWN));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> showLogoutConfirmDialog()); // 변경된 메소드 호출
        headerPanel.add(logoutBtn);

        // --- 메인 메뉴 버튼들 ---
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new GridLayout(2, 2, 20, 20)); 
        menuContainer.setBounds(100, 130, 600, 400);
        menuContainer.setOpaque(false);
        add(menuContainer);

        menuContainer.add(createMenuButton("물품 관리", e -> {
            new AdminItemManageFrame(); 
              dispose();
        }));
        menuContainer.add(createMenuButton("대여 관리", e -> {
            new AdminRentManageFrame(); 
            dispose();
        }));
        menuContainer.add(createMenuButton("장소 대여", e -> {
            new AdminSpaceManageFrame(); 
            dispose();
        }));
        menuContainer.add(createMenuButton("경품 추첨", e -> {
            new AdminLotteryFrame(); 
            dispose();
        }));
    }

    private JButton createMenuButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(24f));
        btn.setBackground(Color.WHITE);
        btn.setForeground(BROWN);
        btn.setBorder(new RoundedBorder(30, BROWN));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(255, 245, 220)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        
        return btn;
    }

    // [수정] 기본 팝업을 예쁜 커스텀 팝업으로 변경
    private void showMsg(String msg) {
        showCustomDialog(msg);
    }

    // ===============================================================
    // 🎨 [추가] 예쁜 커스텀 알림창 (LoginFrame 스타일)
    // ===============================================================
    private void showCustomDialog(String message) {
        JDialog dialog = new JDialog(this, "알림", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = createPopupBackgroundPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JTextArea msgLabel = createPopupMessage(message);
        panel.add(msgLabel);

        JButton okBtn = createStyledButton("확인");
        okBtn.setBounds(120, 160, 160, 50);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    // ===============================================================
    // 🎨 [추가] 예쁜 로그아웃 확인창
    // ===============================================================
    private void showLogoutConfirmDialog() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = createPopupBackgroundPanel();
        panel.setLayout(null);
        dialog.add(panel);

        // [수정] JTextArea 대신 JLabel을 사용하여 중앙 정렬 적용
        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(20f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(0, 60, 400, 80); // 패널 전체 너비(400)를 사용하여 정중앙에 위치
        panel.add(msgLabel);
        
        JButton okBtn = createStyledButton("네");
        okBtn.setFont(uiFont.deriveFont(18f));
        okBtn.setBounds(50, 160, 130, 50);;
        okBtn.addActionListener(e -> {
            dialog.dispose();
            new LoginFrame(); 
            dispose();        
        });
        panel.add(okBtn);

        // 취소 버튼
        JButton cancelBtn = new JButton("아니오");
        cancelBtn.setFont(uiFont.deriveFont(18f));
        cancelBtn.setBackground(BROWN);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBounds(200, 160, 130, 50);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn);

        // 확인(로그아웃) 버튼
        

        dialog.setVisible(true);
    }
    // --- 팝업 UI 헬퍼 메소드들 ---
    private JPanel createPopupBackgroundPanel() {
        return new JPanel() {
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
    }

    private JTextArea createPopupMessage(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(uiFont.deriveFont(20f));
        area.setForeground(BROWN);
        area.setOpaque(false);
        area.setEditable(false);
        area.setHighlighter(null);
        area.setBounds(30, 60, 340, 80);
        // 중앙 정렬 느낌을 위해 줄바꿈 처리 등이 필요할 수 있으나 기본적으로 좌측 정렬됨.
        // 필요시 JLabel로 변경하거나 텍스트 정렬 로직 추가 가능. 여기선 간단히 유지.
        return area;
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(18f));
        b.setBackground(BROWN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(20, BROWN));
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
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
        }
    }

    
}