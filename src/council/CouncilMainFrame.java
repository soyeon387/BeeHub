// 파일명: CouncilMainFrame.java
package council;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import beehub.LoginFrame;

public class CouncilMainFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN       = new Color(250, 250, 250);
    private static final Color BROWN         = new Color(139, 90, 43);
    private static final Color BLUE_BTN      = new Color(70, 130, 180);
    private static final Color RED_BTN       = new Color(220, 80, 80);
    private static final Color GREEN_BTN     = new Color(60, 160, 60);
    private static final Color POPUP_BG      = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = CouncilMainFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JPanel ongoingPanel, endedPanel;
    private String councilId, councilName;
    
    private static final String TOTAL_COUNCIL_DISPLAY_NAME = "총학생회";
    private static final String TOTAL_COUNCIL_NAME_TO_REPLACE = "전체학생";
    

    public CouncilMainFrame(String id, String name) {
        this.councilId = id;
        this.councilName = name;
        
        if (this.councilName != null && this.councilName.trim().equals(TOTAL_COUNCIL_NAME_TO_REPLACE.trim())) {
            this.councilName = TOTAL_COUNCIL_DISPLAY_NAME;
        }

        setTitle("서울여대 꿀단지 - " + name + " 행사 관리");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        refreshLists();
        setVisible(true);
    }

    private void initUI() {
        // ---------- 헤더 ----------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBounds(0, 0, 950, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        headerPanel.setBorder(new EmptyBorder(0, 30, 0, 30));
        add(headerPanel);

        JLabel titleLabel = new JLabel(councilName + " 행사 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        btnPanel.setOpaque(false);

        JButton logoutBtn = createStyledButton("로그아웃", BROWN);
        logoutBtn.setPreferredSize(new Dimension(100, 40));
        logoutBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });
        btnPanel.add(logoutBtn);

        headerPanel.add(btnPanel, BorderLayout.EAST);

        // ---------- 탭 ----------
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setBounds(30, 100, 880, 530);
        tabPane.setFont(uiFont.deriveFont(16f));

        // 진행 중 탭
        JPanel ongoingTab = new JPanel(new BorderLayout());
        ongoingTab.setBackground(BG_MAIN);
        ongoingTab.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(BG_MAIN);

        JButton addBtn = createStyledButton("+ 새 행사 등록", BROWN);
        addBtn.setPreferredSize(new Dimension(140, 40));
        addBtn.addActionListener(e ->
        new CouncilEventAddDialog(this, councilId, this::refreshLists).setVisible(true)
    );
        topBar.add(addBtn);
        ongoingTab.add(topBar, BorderLayout.NORTH);

        ongoingPanel = new JPanel();
        ongoingPanel.setLayout(new BoxLayout(ongoingPanel, BoxLayout.Y_AXIS));
        ongoingPanel.setBackground(BG_MAIN);

        JScrollPane scroll1 = new JScrollPane(ongoingPanel);
        scroll1.setBorder(null);
        scroll1.getVerticalScrollBar().setUnitIncrement(16);
        ongoingTab.add(scroll1, BorderLayout.CENTER);

        tabPane.addTab("진행 중인 행사", ongoingTab);

        // 종료 탭
        JPanel endedTab = new JPanel(new BorderLayout());
        endedTab.setBackground(BG_MAIN);
        endedTab.setBorder(new EmptyBorder(15, 15, 15, 15));

        endedPanel = new JPanel();
        endedPanel.setLayout(new BoxLayout(endedPanel, BoxLayout.Y_AXIS));
        endedPanel.setBackground(BG_MAIN);

        JScrollPane scroll2 = new JScrollPane(endedPanel);
        scroll2.setBorder(null);
        scroll2.getVerticalScrollBar().setUnitIncrement(16);
        endedTab.add(scroll2, BorderLayout.CENTER);

        tabPane.addTab("종료된 행사", endedTab);

        add(tabPane);
    }

    // ✅ DB에서 다시 읽어서 탭 두 개 갱신
    public void refreshLists() {
        ongoingPanel.removeAll();
        endedPanel.removeAll();

        List<EventManager.EventData> events = EventManager.getEventsByOwner(councilId);
        LocalDateTime now = LocalDateTime.now();

        for (EventManager.EventData event : events) {

            boolean isEnded = false;
            if ("종료".equals(event.status)) isEnded = true;

            // ✅ 종료 판정은 "신청 마감시간(applyEnd)" 기준
            LocalDateTime end = event.applyEnd;

            // applyEnd가 없으면(데이터 누락) 최소한 event_date 기준(간식은 +1시간)으로 방어
            if (end == null && event.date != null) {
                if (event.eventType != null && event.eventType.equalsIgnoreCase("SNACK")) {
                    end = event.date.plusHours(1);
                } else {
                    end = event.date;
                }
            }

            if (end != null) {
                isEnded = end.isBefore(now); // end < now 이면 종료
            }

            if (isEnded) {
                endedPanel.add(createEventCard(event, false));
                endedPanel.add(Box.createVerticalStrut(15));
            } else {
                ongoingPanel.add(createEventCard(event, true));
                ongoingPanel.add(Box.createVerticalStrut(15));
            }
        }

        ongoingPanel.revalidate();
        ongoingPanel.repaint();
        endedPanel.revalidate();
        endedPanel.repaint();
    }

    private JPanel createEventCard(EventManager.EventData event, boolean isOngoing) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(850, 160));
        card.setPreferredSize(new Dimension(850, 160));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // ----- 정보 영역 -----
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel(event.title);
        title.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        title.setForeground(BROWN);

        String dateStr = (event.date != null)
                ? event.date.format(EventManager.DATE_FMT)
                : "일정 미정";

        String detail1 = String.format("일시: %s  |  장소: %s",
                dateStr,
                (event.location != null ? event.location : "-"));
        JLabel l1 = new JLabel(detail1);
        l1.setFont(uiFont.deriveFont(15f));
        l1.setForeground(Color.DARK_GRAY);

        infoPanel.add(title);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(l1);
        infoPanel.add(Box.createVerticalStrut(5));

        if (isOngoing) {
            String period = event.getPeriodString();
            String detail2 = String.format(
                    "신청: %s  |  인원: %d/%d",
                    (period.isEmpty() ? "기간 미설정" : period),
                    event.currentCount,
                    event.totalCount
            );
            JLabel l2 = new JLabel(detail2);
            l2.setFont(uiFont.deriveFont(14f));
            l2.setForeground(new Color(100, 100, 100));
            infoPanel.add(l2);

            if (event.secretCode != null && !event.secretCode.isEmpty()) {
                infoPanel.add(Box.createVerticalStrut(5));
                JLabel l3 = new JLabel("비밀코드: " + event.secretCode);
                l3.setFont(uiFont.deriveFont(Font.BOLD, 14f));
                l3.setForeground(new Color(200, 100, 100));
                infoPanel.add(l3);
            }
        } else {
            JLabel l2 = new JLabel("행사가 종료되었습니다.");
            l2.setFont(uiFont.deriveFont(14f));
            l2.setForeground(new Color(100, 100, 100));
            infoPanel.add(l2);
        }

        card.add(infoPanel, BorderLayout.CENTER);

        // ----- 버튼 영역 -----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 35));
        btnPanel.setBackground(Color.WHITE);

        JButton listBtn = createStyledButton("명단확인", GREEN_BTN);
        listBtn.setPreferredSize(new Dimension(100, 40));
        listBtn.addActionListener(e -> new CouncilRecipientDialog(this, event));
        btnPanel.add(listBtn);

        if (isOngoing) {
            JButton editBtn = createStyledButton("수정", BLUE_BTN);
            editBtn.setPreferredSize(new Dimension(70, 40));
            editBtn.addActionListener(e ->
            new CouncilEventAddDialog(this, event, this::refreshLists).setVisible(true)
        );
            btnPanel.add(editBtn);
        }

        JButton delBtn = createStyledButton("삭제", RED_BTN);
        delBtn.setPreferredSize(new Dimension(70, 40));
        delBtn.addActionListener(e -> {
            showCustomConfirmPopup(
                    "[" + event.title + "]\n행사를 정말 삭제하시겠습니까?",
                    () -> {
                        EventManager.deleteEvent(event.eventId);
                        refreshLists();
                    }
            );
        });
        btnPanel.add(delBtn);

        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    private void showCustomConfirmPopup(String message, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, "확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel() {
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

        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(uiFont.deriveFont(18f));
        msgArea.setForeground(BROWN);
        msgArea.setOpaque(false);
        msgArea.setEditable(false);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBounds(30, 60, 340, 80);
        panel.add(msgArea);

        JButton yesBtn = createStyledButton("네", BROWN);
        yesBtn.setBounds(60, 160, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });
        panel.add(yesBtn);

        JButton noBtn = createStyledButton("아니오", BROWN);
        noBtn.setBounds(220, 160, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new RoundedBorder(10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class RoundedBorder implements Border {
        private int radius;
        public RoundedBorder(int r) { radius = r; }
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new CouncilMainFrame("council_soft", "소프트웨어융합학과")
        );
    }
}
