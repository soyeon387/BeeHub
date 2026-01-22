package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import beehub.Item;
import beehub.ItemDAO;

public class ItemListFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color GREEN_AVAILABLE = new Color(180, 230, 180);
    private static final Color RED_UNAVAILABLE = new Color(255, 200, 200);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;

    static {
        try {
            InputStream is = ItemListFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private String userName = "사용자";
    private String userId;  // 🔹 로그인한 학번/ID
    private JPanel itemListPanel;
    private JTextField searchField;

    public ItemListFrame() {
        setTitle("서울여대 꿀단지 - 물품대여");
        setSize(800, 600);

        User currentUser = UserManager.getCurrentUser();
        if (currentUser != null) userName = currentUser.getName();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        loadItems();

        setVisible(true);
    }

    // 🔹 로그인한 사용자 ID를 같이 받고 싶을 때 쓰는 생성자
    public ItemListFrame(String userId) {
        this();             // 위 기본 생성자 먼저 실행
        this.userId = userId;
    }

    private void initUI() {
        // --- 헤더 ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
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

        // 상단 사용자 정보
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
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 0);
            navPanel.add(menuBtn);
        }

        // --- 콘텐츠 영역 ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
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
        searchField.addActionListener(e -> searchItems());
        contentPanel.add(searchField);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        searchIcon.setBounds(560, 25, 30, 30);
        searchIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { searchItems(); }
        });
        contentPanel.add(searchIcon);

        itemListPanel = new JPanel();
        itemListPanel.setLayout(null);
        itemListPanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(itemListPanel);
        scrollPane.setBounds(25, 80, 750, 370);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        contentPanel.add(scrollPane);
    }

    private void loadItems() {
        itemListPanel.removeAll();
        List<Item> items = ItemDAO.getInstance().getAllItems();
        int yPos = 10;

        for (Item item : items) {
            addItemCard(item, yPos);
            yPos += 130;
        }

        itemListPanel.setPreferredSize(new Dimension(730, yPos));
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    private void searchItems() {
        String keyword = searchField.getText().trim();
        List<Item> allItems = ItemDAO.getInstance().getAllItems();

        itemListPanel.removeAll();
        int yPos = 10;
        boolean found = false;

        for (Item item : allItems) {
            if (keyword.isEmpty() || item.getName().contains(keyword)) {
                addItemCard(item, yPos);
                yPos += 130;
                found = true;
            }
        }

        if (!found) {
            JLabel noResult = new JLabel("검색 결과가 없습니다.", SwingConstants.CENTER);
            noResult.setFont(uiFont.deriveFont(20f));
            noResult.setForeground(new Color(150, 150, 150));
            noResult.setBounds(0, 100, 750, 50);
            itemListPanel.add(noResult);
        }
        itemListPanel.setPreferredSize(new Dimension(730, Math.max(yPos, 350)));
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    private void addItemCard(Item item, int y) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBounds(10, y, 730, 110);
        card.setBackground(Color.WHITE);
        card.setBorder(new RoundedBorder(15, new Color(200, 200, 200), 2));

        JLabel iconLabel = new JLabel();
        iconLabel.setBounds(20, 20, 70, 70);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(245, 245, 245));
        iconLabel.setBorder(new RoundedBorder(10, new Color(220, 220, 220), 1));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        boolean imgLoaded = false;
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                URL url = getClass().getResource(item.getImagePath().startsWith("/") ? item.getImagePath() : "/" + item.getImagePath());
                if (url == null) {
                    ImageIcon icon = new ImageIcon(item.getImagePath());
                    if (icon.getIconWidth() > 0) {
                        Image img = icon.getImage().getScaledInstance(65, 65, Image.SCALE_SMOOTH);
                        iconLabel.setIcon(new ImageIcon(img));
                        imgLoaded = true;
                    }
                } else {
                    ImageIcon icon = new ImageIcon(url);
                    Image img = icon.getImage().getScaledInstance(65, 65, Image.SCALE_SMOOTH);
                    iconLabel.setIcon(new ImageIcon(img));
                    imgLoaded = true;
                }
            } catch (Exception e) {}
        }

        if (!imgLoaded) {
            iconLabel.setText("📦");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        }
        card.add(iconLabel);

        int stock = item.getAvailableStock();
        String statusText = (stock > 0) ? "대여 가능" : "대여 불가";
        Color statusColor = (stock > 0) ? GREEN_AVAILABLE : RED_UNAVAILABLE;

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 13f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(110, 20, 90, 25);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(statusColor);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel);

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 26f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(110, 50, 300, 40);
        card.add(nameLabel);

        JLabel stockLabel = new JLabel("재고: " + stock);
        stockLabel.setFont(uiFont.deriveFont(14f));
        stockLabel.setForeground(Color.GRAY);
        stockLabel.setBounds(600, 45, 100, 20);
        card.add(stockLabel);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new ItemDetailFrame(item, userId).setVisible(true);
                dispose();
            }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(250, 250, 250)); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        itemListPanel.add(card);
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
                    if (text.equals("물품대여")) return;
                    if (text.equals("과행사")) { new EventListFrame(); dispose(); }
                    else if (text.equals("공간대여")) { new SpaceRentFrame(); dispose(); }
                    else if (text.equals("마이페이지")) { new MyPageFrame(); dispose(); }
                    else if (text.equals("빈 강의실")) { new EmptyClassFrame(); dispose(); }
                    else if (text.equals("커뮤니티")) { new CommunityFrame(); dispose(); }
                    else { showSimplePopup("알림", "준비 중입니다."); }
                }
            });
        }
        return btn;
    }

    // ===============================
    // ✅ 기본 JOptionPane 스타일 대신 커스텀 팝업 사용용 메서드 추가
    // ===============================
    private void showErrorPopup(String title, String message) {
        // 현재 프로젝트 팝업 스타일 유지하면서 에러도 동일하게
        showSimplePopup(title, message);
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

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(uiFont.deriveFont(18f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 45, 360, 25);
        panel.add(titleLabel);

        JLabel msgLabel = new JLabel(
                "<html><div style='text-align:center;'>" + message.replace("\n","<br>") + "</div></html>",
                SwingConstants.CENTER
        );
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 75, 360, 60);
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
        yes.setBorder(new RoundedBorder(15, BROWN, 1));
        yes.setFocusPainted(false);
        yes.addActionListener(e -> { dialog.dispose(); new LoginFrame(); dispose(); });
        panel.add(yes);

        JButton no = new JButton("아니오");
        no.setFont(uiFont);
        no.setBounds(220, 150, 120, 45);
        no.setBackground(BROWN);
        no.setForeground(Color.WHITE);
        no.setBorder(new RoundedBorder(15, BROWN, 1));
        no.setFocusPainted(false);
        no.addActionListener(e -> dialog.dispose());
        panel.add(no);

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
