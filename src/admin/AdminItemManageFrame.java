package admin;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

// 🔥 beehub 패키지에서 Item / ItemDAO 불러오기
import beehub.Item;
import beehub.ItemListFrame;
import beehub.ItemDAO;

public class AdminItemManageFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = AdminItemManageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else {
                Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
                uiFont = base.deriveFont(14f);
            }
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private JPanel itemListPanel;

    public AdminItemManageFrame() {
        setTitle("관리자 - 물품 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        refreshList();
        setVisible(true);
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

        // ✅ JLabel의 <html> 렌더링은 커스텀 폰트가 기본 폰트로 fallback 되는 경우가 있어
        //    멀티라인은 JTextPane(비-HTML)로 렌더링하면 ‘삭제 불가’와 동일 폰트가 100% 적용됨
        JTextPane msgPane = new JTextPane();
        msgPane.setText(message);
        msgPane.setEditable(false);
        msgPane.setOpaque(false);
        msgPane.setBorder(null);
        msgPane.setHighlighter(null);
        msgPane.setForeground(BROWN);
        msgPane.setFont(uiFont.deriveFont(16f));
        msgPane.setBounds(20, 75, 360, 60);

        StyledDocument doc = msgPane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(attrs, uiFont.getFamily());
        StyleConstants.setFontSize(attrs, 16);
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);

        panel.add(msgPane);

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

    private void showErrorPopup(String title, String message) {
        showSimplePopup(title, message);
    }

    private void initUI() {
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 800, 80);
        header.setBackground(HEADER_YELLOW);
        add(header);

        JLabel title = new JLabel("물품 관리");
        title.setFont(uiFont.deriveFont(32f));
        title.setForeground(BROWN);
        title.setBounds(30, 20, 200, 40);
        header.add(title);

        JButton homeBtn = new JButton("<-메인으로");
        homeBtn.setBounds(650, 25, 110, 35);
        homeBtn.setFont(uiFont.deriveFont(14f));
        homeBtn.setBackground(BROWN);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBorder(new RoundedBorder(15, BROWN));
        homeBtn.addActionListener(e -> { new AdminMainFrame(); dispose(); });
        header.add(homeBtn);

        JButton addBtn = new JButton("+ 물품 등록");
        addBtn.setBounds(630, 100, 130, 40);
        addBtn.setFont(uiFont.deriveFont(16f));
        addBtn.setBackground(BROWN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorder(new RoundedBorder(15, BROWN));
        // 🔥 AdminItemAddDialog 는 beehub.Item 을 받도록 수정해둔다는 전제
        addBtn.addActionListener(e -> new AdminItemAddDialog(this, null));
        add(addBtn);

        itemListPanel = new JPanel(null);
        itemListPanel.setBackground(BG_MAIN);

        JScrollPane scroll = new JScrollPane(itemListPanel);
        scroll.setBounds(30, 150, 730, 400);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        add(scroll);
    }

    // 🔥 DB 에서 전체 아이템(비활성 포함) 가져오기
    public void refreshList() {
        itemListPanel.removeAll();

        // 기존: ItemManager.getAllItems()
        List<Item> items = ItemDAO.getInstance().getAllItemsAdmin();

        int y = 0;
        for (Item item : items) {
            JPanel card = createItemCard(item);
            card.setBounds(10, y, 700, 100);
            itemListPanel.add(card);
            y += 110;
        }

        itemListPanel.setPreferredSize(new Dimension(700, y));
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    private JPanel createItemCard(Item item) {
        JPanel p = new JPanel(null);
        // 비활성인 경우 살짝 회색 (기존 is_active 컬럼 그대로 활용)
        p.setBackground(item.isActive() ? Color.WHITE : new Color(245, 245, 245));
        p.setBorder(new RoundedBorder(15, Color.LIGHT_GRAY));

        JLabel icon = new JLabel("📦", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setBounds(15, 15, 70, 70);
        icon.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        boolean imgLoaded = false;
        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                URL url = getClass().getResource(path.startsWith("/") ? path : "/" + path);
                if (url == null) {
                    ImageIcon ii = new ImageIcon(path);
                    if (ii.getIconWidth() > 0) {
                        icon.setIcon(new ImageIcon(ii.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)));
                        icon.setText("");
                        imgLoaded = true;
                    }
                } else {
                    ImageIcon ii = new ImageIcon(url);
                    icon.setIcon(new ImageIcon(ii.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)));
                    icon.setText("");
                    imgLoaded = true;
                }
            } catch (Exception e) { }
        }
        if (!imgLoaded) {
            icon.setText("📦");
        }
        p.add(icon);

        // 이름 + (비활성) 표시
        String nameText = item.getName();
        if (!item.isActive()) {
            nameText += "  (비활성)";
        }

        JLabel name = new JLabel(nameText);
        name.setFont(uiFont.deriveFont(20f));
        name.setForeground(BROWN);
        name.setBounds(100, 15, 400, 25);
        p.add(name);

        // 재고: available / total, 기간, 대상 학과
        String infoText = String.format(
                "재고: %d / %d | 기간: %d일 | %s",
                item.getAvailableStock(),
                item.getTotalStock(),
                item.getMaxRentDays(),
                item.getTargetMajor()
        );

        JLabel info = new JLabel(infoText);
        info.setFont(uiFont.deriveFont(14f));
        info.setForeground(Color.GRAY);
        info.setBounds(100, 50, 450, 20);
        p.add(info);

        JButton edit = new JButton("수정");
        edit.setBounds(530, 30, 70, 40);
        edit.setFont(uiFont.deriveFont(12f));
        edit.setBackground(new Color(255, 238, 140));
        edit.setForeground(BROWN);
        edit.setBorder(new RoundedBorder(10, BROWN));
        edit.addActionListener(e -> new AdminItemAddDialog(this, item));
        p.add(edit);

        // 🔥 삭제 버튼 (이제 비활성/활성 토글 대신 완전 삭제)
        JButton del = new JButton("삭제");
        del.setBounds(610, 30, 70, 40);
        del.setFont(uiFont.deriveFont(12f));
        del.setBackground(new Color(200, 50, 50));
        del.setForeground(Color.WHITE);
        del.setBorder(new RoundedBorder(10, new Color(160, 40, 40)));

        del.addActionListener(e -> {
            // 1) 삭제 재확인 팝업
            boolean confirm = showConfirmPopup(
                    "삭제 확인",
                    "[" + item.getName() + "] 물품을\n정말 삭제하시겠습니까?\n(삭제 시 되돌릴 수 없습니다)"
            );

            if (!confirm) return;

            // 2) 현재 대여중인지 체크
            boolean rented = ItemDAO.getInstance().isItemRented(item.getItemId());

            if (rented) {
                // ✅ 기본 JOptionPane 대신 커스텀 팝업으로
            	showErrorPopup("삭제 불가", "현재 누군가 대여 중인 물품은\n삭제할 수 없습니다.");

                return;
            }

            // 3) 삭제 실행
            boolean ok = ItemDAO.getInstance().deleteItem(item.getItemId());

            if (ok) {
                showSimplePopup("삭제 완료", "물품이 정상적으로\n삭제되었습니다.");
                refreshList();
            } else {
                showErrorPopup("삭제 실패", "물품 삭제에\n실패했습니다.");
            }

        });
        p.add(del);

        return p;
    }

    private boolean showConfirmPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));
        final boolean[] res = {false};

        JPanel p = new JPanel() {
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
        p.setLayout(null);
        dialog.add(p);

        String[] lines = msg.split("\n");
        int y = lines.length == 1 ? 80 : 60;
        for (String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, y, 360, 30);
            p.add(l);
            y += 30;
        }

        JButton yes = new JButton("네");
        yes.setFont(uiFont);
        yes.setBounds(60, 160, 120, 45);
        yes.setBackground(BROWN);
        yes.setForeground(Color.WHITE);
        yes.addActionListener(e -> { res[0] = true; dialog.dispose(); });
        p.add(yes);

        JButton no = new JButton("아니오");
        no.setFont(uiFont);
        no.setBounds(220, 160, 120, 45);
        no.setBackground(BROWN);
        no.setForeground(Color.WHITE);
        no.addActionListener(e -> { res[0] = false; dialog.dispose(); });
        p.add(no);

        dialog.setVisible(true);
        return res[0];
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
        private int r; private Color c; private int thickness;
        public RoundedBorder(int r, Color c) { this.r=r; this.c=c; }
        public RoundedBorder(int radius, Color color, int thickness) {
            this.r = radius;
            this.c = color;
            this.thickness = thickness;
        }
        public Insets getBorderInsets(Component c) { return new Insets(r/2,r/2,r/2,r/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.c);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x,y,w-1,h-1,r,r);
        }
    }
}
