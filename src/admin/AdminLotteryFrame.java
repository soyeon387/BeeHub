package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import admin.LotteryManager.Applicant;
import admin.LotteryManager.LotteryRound;

public class AdminLotteryFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN       = new Color(255, 255, 255);
    private static final Color BROWN         = new Color(139, 90, 43);
    private static final Color BLUE_BTN      = new Color(100, 150, 255);
    private static final Color RED_WIN       = new Color(255, 100, 100);
    private static final Color GRAY_LOSE     = new Color(150, 150, 150);
    private static final Color POPUP_BG      = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = AdminLotteryFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JComboBox<String> roundCombo;
    private JPanel listPanel;
    private JButton drawBtn;
    private JLabel infoLabel;

    private List<LotteryRound> rounds = new ArrayList<>();

    public AdminLotteryFrame() {
        setTitle("관리자 - 경품 추첨");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        refreshList();
        setVisible(true);
    }

    private void initUI() {

        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 850, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel titleLabel = new JLabel("경품 추첨 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(titleLabel);

        JButton homeBtn = new JButton("<-메인으로");
        homeBtn.setFont(uiFont.deriveFont(14f));
        homeBtn.setBackground(BROWN);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBounds(700, 25, 110, 35);
        homeBtn.setBorder(new RoundedBorder(15, BROWN));
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> {
            new AdminMainFrame();
            dispose();
        });
        headerPanel.add(homeBtn);

        JPanel controlPanel = new JPanel(null);
        controlPanel.setBounds(30, 90, 780, 60);
        controlPanel.setBackground(BG_MAIN);
        add(controlPanel);

        JLabel comboLabel = new JLabel("진행 회차 :");
        comboLabel.setFont(uiFont.deriveFont(16f));
        comboLabel.setForeground(BROWN);
        comboLabel.setBounds(0, 15, 90, 30);
        controlPanel.add(comboLabel);

        rounds = LotteryManager.getAllRounds();

        roundCombo = new JComboBox<>();
        roundCombo.setFont(uiFont.deriveFont(14f));
        roundCombo.setBounds(90, 15, 300, 35);
        roundCombo.setBackground(Color.WHITE);

        for (int i = 0; i < rounds.size(); i++) {
            LotteryRound r = rounds.get(i);
            String display = (i + 1) + "회차: " + r.name;
            roundCombo.addItem(display);
        }
        roundCombo.addActionListener(e -> refreshList());
        controlPanel.add(roundCombo);

        JButton regBtn = new JButton("+ 추첨 등록");
        regBtn.setFont(uiFont.deriveFont(14f));
        regBtn.setBackground(BROWN);
        regBtn.setForeground(Color.WHITE);
        regBtn.setBounds(400, 15, 120, 35);
        regBtn.setBorder(new RoundedBorder(15, BROWN));
        regBtn.setFocusPainted(false);
        regBtn.addActionListener(e -> new AdminLotteryAddDialog(this));
        controlPanel.add(regBtn);

        drawBtn = new JButton("추첨 시작");
        drawBtn.setFont(uiFont.deriveFont(14f));
        drawBtn.setBackground(BLUE_BTN);
        drawBtn.setForeground(Color.WHITE);
        drawBtn.setBounds(530, 15, 120, 35);
        drawBtn.setBorder(new RoundedBorder(15, BLUE_BTN));
        drawBtn.setFocusPainted(false);
        drawBtn.addActionListener(e -> runLottery());
        controlPanel.add(drawBtn);

        infoLabel = new JLabel("");
        infoLabel.setFont(uiFont.deriveFont(13f));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setBounds(30, 155, 780, 60);
        add(infoLabel);

        JPanel listHeader = new JPanel(new GridLayout(1, 4));
        listHeader.setBounds(30, 220, 780, 30);
        listHeader.setBackground(new Color(240, 240, 240));
        listHeader.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        String[] cols = {"응모자", "학번", "응모 횟수", "당첨 여부"};
        for (String col : cols) {
            JLabel l = new JLabel(col, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(Font.BOLD, 14f));
            l.setForeground(BROWN);
            listHeader.add(l);
        }
        add(listHeader);

        listPanel = new JPanel(null);
        listPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBounds(30, 250, 780, 330);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 모던 스크롤바 적용
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        
        add(scrollPane);
    }

    public void refreshList() {
        listPanel.removeAll();

        if (rounds == null || rounds.isEmpty()) {
            infoLabel.setText("등록된 경품 추첨 회차가 없습니다.");
            return;
        }

        int idx = roundCombo.getSelectedIndex();
        if (idx < 0 || idx >= rounds.size()) return;

        LotteryRound r = rounds.get(idx);

        infoLabel.setText("<html>" +
                "<span style='color:#8B5A2B; font-weight:bold;'>경품: " +
                r.prizeName + " (" + r.winnerCount + "명)</span><br>" +
                "발표: " + r.announcementDate + "<br>" +
                "응모기간: " + r.applicationPeriod + "<br>" +
                "수령장소: " + r.pickupLocation + "<br>" +
                "수령기간: " + r.pickupPeriod +
                "</html>");

        if (r.isDrawn) {
            drawBtn.setText("추첨 완료");
            drawBtn.setEnabled(false);
            drawBtn.setBackground(Color.GRAY);
        } else {
            drawBtn.setText("추첨 시작");
            drawBtn.setEnabled(true);
            drawBtn.setBackground(BLUE_BTN);
        }

        int y = 0;

        for (Applicant a : r.applicants) {
            JPanel row = new JPanel(new GridLayout(1, 4));
            row.setBounds(0, y, 780, 40);
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(230,230,230)));

            addCell(row, a.name, Color.BLACK);
            addCell(row, a.hakbun, Color.BLACK);
            addCell(row, a.count + "회", Color.BLACK);

            JLabel status = new JLabel(a.status, SwingConstants.CENTER);
            status.setFont(uiFont.deriveFont(14f));
            if ("당첨".equals(a.status)) {
                status.setForeground(RED_WIN);
            } else if ("미당첨".equals(a.status)) {
                status.setForeground(GRAY_LOSE);
            }
            row.add(status);

            listPanel.add(row);
            y += 40;
        }

        listPanel.setPreferredSize(new Dimension(760, y));
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void addCell(JPanel p, String text, Color c) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(c);
        p.add(l);
    }

    private void runLottery() {
        if (rounds == null || rounds.isEmpty()) return;

        int idx = roundCombo.getSelectedIndex();
        if (idx < 0 || idx >= rounds.size()) return;

        LotteryRound r = rounds.get(idx);

        // 응모 기간 체크
        try {
            if (r.applicationPeriod != null && r.applicationPeriod.contains("~")) {
                String[] parts = r.applicationPeriod.split("~");
                if (parts.length >= 2) {
                    String endStr = parts[1].trim(); 
                    DateTimeFormatter checkFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");
                    LocalDateTime endDate = LocalDateTime.parse(endStr, checkFmt);
                    
                    if (LocalDateTime.now().isBefore(endDate)) {
                        showMsgPopup("추첨 불가", 
                                "아직 응모 기간이 끝나지 않았습니다.\n" +
                                "마감일: " + endStr + "\n" +
                                "(기간 종료 후 추첨 가능합니다)");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsgPopup("오류", "기간 정보를 읽을 수 없어 추첨을 진행할 수 없습니다.\n(" + e.getMessage() + ")");
            return;
        }

        boolean confirm = showConfirmPopup(
                "",
                "[" + r.name + "] \n추첨을 시작하시겠습니까?\n(총 " + r.winnerCount + "명 선정)"
        );

        if (!confirm) return;


        if (r.applicants.isEmpty()) {
            showMsgPopup("알림", "응모자가 없습니다.");
            return;
        }

        List<Applicant> shuffled = new ArrayList<>(r.applicants);
        Collections.shuffle(shuffled);

        for (Applicant a : r.applicants) {
            a.status = "미당첨";
        }

        for (int i = 0; i < r.winnerCount && i < shuffled.size(); i++) {
            shuffled.get(i).status = "당첨";
        }

        r.isDrawn = true;

        boolean ok = LotteryManager.saveDrawResult(r);
        if (!ok) {
            showMsgPopup("오류", "결과 저장 중 오류가 발생했습니다.");
            return;
        }

        rounds = LotteryManager.getAllRounds();
        refreshList();
        showMsgPopup("완료", "추첨이 완료되었습니다!");
    }
    
    // 예쁜 팝업
    private void showMsgPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
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
        panel.setLayout(null);
        dialog.add(panel);

        JTextPane msgPane = new JTextPane();
        msgPane.setText(msg);
        msgPane.setFont(uiFont.deriveFont(18f));
        msgPane.setForeground(BROWN);
        msgPane.setOpaque(false);
        msgPane.setEditable(false);

        javax.swing.text.StyledDocument doc = msgPane.getStyledDocument();
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        msgPane.setBounds(20, 65, 360, 80);
        panel.add(msgPane);

        JButton okBtn = new JButton("확인");
        okBtn.setFont(uiFont.deriveFont(16f));
        okBtn.setBackground(BROWN);
        okBtn.setForeground(Color.WHITE);
        okBtn.setBounds(135, 170, 130, 45);
        okBtn.setBorder(new RoundedBorder(15, BROWN));
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);
        


        dialog.setVisible(true);
    }
    
 // ✅ 예/아니오 확인 팝업 (우리 UI)
    private boolean showConfirmPopup(String title, String msg) {
        final boolean[] result = { false };

        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(430, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 배경
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // 테두리
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        // 제목
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(25, 18, 320, 30);
        panel.add(titleLabel);

        // 닫기(X)
        JButton closeBtn = new JButton("X");
        closeBtn.setFont(uiFont.deriveFont(Font.BOLD, 16f));
        closeBtn.setForeground(BROWN);
        closeBtn.setBackground(new Color(0, 0, 0, 0));
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setBounds(385, 10, 35, 35);
        closeBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        panel.add(closeBtn);

        // 메시지 (가운데 정렬)
        JTextPane msgPane = new JTextPane();
        msgPane.setText(msg);
        msgPane.setFont(uiFont.deriveFont(17f));
        msgPane.setForeground(BROWN);
        msgPane.setOpaque(false);
        msgPane.setEditable(false);

        javax.swing.text.StyledDocument doc = msgPane.getStyledDocument();
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        msgPane.setBounds(25, 65, 380, 90);
        panel.add(msgPane);

        // 버튼들
        JButton yesBtn = new JButton("예");
        yesBtn.setFont(uiFont.deriveFont(16f));
        yesBtn.setBackground(BLUE_BTN);
        yesBtn.setForeground(Color.WHITE);
        yesBtn.setBorder(new RoundedBorder(15, BLUE_BTN));
        yesBtn.setFocusPainted(false);
        yesBtn.setBounds(85, 175, 120, 45);
        yesBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBackground(BROWN);
        noBtn.setForeground(Color.WHITE);
        noBtn.setBorder(new RoundedBorder(15, BROWN));
        noBtn.setFocusPainted(false);
        noBtn.setBounds(225, 175, 120, 45);
        noBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        panel.add(noBtn);

        // ✅ Enter=예 / ESC=아니오
        dialog.getRootPane().setDefaultButton(yesBtn);
        dialog.getRootPane().registerKeyboardAction(
                e -> noBtn.doClick(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setVisible(true);
        return result[0];
    }


    public void addRound(String title,
                         String prize,
                         int count,
                         LocalDate announcementDate,
                         LocalDateTime applicationStart,
                         LocalDateTime applicationEnd,
                         String loc,
                         LocalDateTime pickupStart,
                         LocalDateTime pickupEnd) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String ann = announcementDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String appS = applicationStart.format(dtf);
        String appE = applicationEnd.format(dtf);
        String pickS = pickupStart.format(dtf);
        String pickE = pickupEnd.format(dtf);

        LotteryManager.addRound(
                title,
                prize,
                count,
                ann,
                appS,
                appE,
                loc,
                pickS,
                pickE
        );

        rounds = LotteryManager.getAllRounds();
        roundCombo.removeAllItems();
        for (int i = 0; i < rounds.size(); i++) {
            roundCombo.addItem((i + 1) + "회차: " + rounds.get(i).name);
        }

        if (!rounds.isEmpty()) {
            roundCombo.setSelectedIndex(rounds.size() - 1);
        }

        refreshList();
    }

    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        public RoundedBorder(int r, Color c) {
            radius = r;
            color = c;
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
        public boolean isBorderOpaque() {
            return false;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
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
}