package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import beehub.Rent;
import beehub.RentDAO;
import beehub.ItemDAO;
import beehub.PointService;

public class AdminRentManageFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color RED_OVERDUE = new Color(255, 80, 80);
    private static final Color GREEN_DONE = new Color(100, 180, 100);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;

    static {
        try {
            InputStream is = AdminRentManageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JPanel rentListPanel;
    private List<Rent> rentList = new ArrayList<>();

    public AdminRentManageFrame() {
        setTitle("관리자 - 대여 관리");
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();

        try {
            refreshList();
        } catch (Exception ex) {
            ex.printStackTrace();
            showMsgPopup("오류", "대여 목록을 불러오는 중 오류가 발생했습니다.\n" + ex.getMessage());
        }

        setVisible(true);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel titleLabel = new JLabel("대여 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 200, 40);
        headerPanel.add(titleLabel);

        JButton homeBtn = new JButton("<-메인으로");
        homeBtn.setFont(uiFont.deriveFont(14f));
        homeBtn.setBackground(BROWN);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBounds(650, 25, 110, 35);
        homeBtn.setBorder(new RoundedBorder(15, BROWN));
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> {
            new AdminMainFrame();
            dispose();
        });
        headerPanel.add(homeBtn);

        rentListPanel = new JPanel();
        rentListPanel.setLayout(null);
        rentListPanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(rentListPanel);
        scrollPane.setBounds(30, 100, 730, 440);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }

    private void refreshList() {
        rentListPanel.removeAll();

        try {
            rentList = RentDAO.getInstance().getAllRentals();
        } catch (Exception ex) {
            ex.printStackTrace();
            showMsgPopup("오류", "대여 목록 조회 중 오류가 발생했습니다.\n" + ex.getMessage());
            rentList = new ArrayList<>();
        }

        int yPos = 10;
        for (Rent data : rentList) {
            JPanel card = createRentCard(data);
            card.setBounds(10, yPos, 690, 100);
            rentListPanel.add(card);
            yPos += 110;
        }

        rentListPanel.setPreferredSize(new Dimension(690, yPos));
        rentListPanel.revalidate();
        rentListPanel.repaint();
    }

    private JPanel createRentCard(Rent data) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(15, Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel(data.getItemName());
        nameLabel.setFont(uiFont.deriveFont(20f));
        nameLabel.setForeground(BROWN);
        nameLabel.setBounds(20, 15, 250, 30);
        panel.add(nameLabel);

        JLabel renterLabel =
                new JLabel("대여자: " + data.getRenterId() + " | " + data.getRenterName());
        renterLabel.setFont(uiFont.deriveFont(14f));
        renterLabel.setForeground(Color.GRAY);
        renterLabel.setBounds(20, 50, 250, 20);
        panel.add(renterLabel);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy.MM.dd");
        String dateStr = data.getRentDate().format(dtf) + " ~ " + data.getDueDate().format(dtf);
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(uiFont.deriveFont(14f));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setBounds(20, 70, 250, 20);
        panel.add(dateLabel);

        long daysDiff = ChronoUnit.DAYS.between(LocalDate.now(), data.getDueDate());
        String dDayStr;
        Color dDayColor;

        if (data.isReturned()) {
            dDayStr = "반납완료";
            dDayColor = GREEN_DONE;
        } else {
            if (daysDiff >= 0) {
                dDayStr = (daysDiff == 0) ? "D-Day" : "D-" + daysDiff;
                dDayColor = BROWN;
            } else {
                dDayStr = "D+" + Math.abs(daysDiff) + " (연체)";
                dDayColor = RED_OVERDUE;
            }
        }

        JLabel statusLabel = new JLabel(dDayStr, SwingConstants.RIGHT);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        statusLabel.setForeground(dDayColor);
        statusLabel.setBounds(300, 35, 200, 30);
        panel.add(statusLabel);

        JButton actionBtn = new JButton();
        if (data.isReturned()) {
            actionBtn.setText("완료됨");
            actionBtn.setBackground(new Color(230, 230, 230));
            actionBtn.setForeground(Color.GRAY);
            actionBtn.setEnabled(false);
            actionBtn.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        } else {
            actionBtn.setText("반납확인");
            actionBtn.setBackground(BROWN);
            actionBtn.setForeground(Color.WHITE);
            actionBtn.setBorder(new RoundedBorder(10, BROWN));

            actionBtn.addActionListener(e -> {
                boolean confirm = showConfirmPopup(
                        "반납 확인",
                        "[" + data.getItemName() + "] 반납 처리를\n하시겠습니까?"
                );

                if (!confirm) return;   // 아니오 누르면 바로 종료

                boolean rentSuccess =
                        RentDAO.getInstance().returnItem(data.getRentalId());
                boolean itemSuccess =
                        ItemDAO.getInstance().increaseAvailableStock(data.getItemId());

                if (rentSuccess && itemSuccess) {
                    long overdueDays =
                            ChronoUnit.DAYS.between(data.getDueDate(), LocalDate.now());

                    String renterId = data.getRenterId();  // 학번

                    if (overdueDays > 0) {
                        // 연체: -50꿀
                        PointService.addPoint(renterId, -50);

                        showMsgPopup("연체 확인",
                                "연체 반납 확인되었습니다.\n" +
                                        "50꿀 차감");
                    } else {
                        // 제때 반납: +10꿀
                        PointService.addPoint(renterId, +10);

                        showMsgPopup("반납 완료", "정상적으로 반납되었습니다.\n+10꿀이 지급되었습니다.");
                    }

                    refreshList();   // 리스트 갱신
                } else {
                    showMsgPopup("오류",
                            "DB 처리 중 오류가 발생했습니다.\n(대여 기록 또는 재고 업데이트 실패)");
                }
            });

        }

        actionBtn.setFont(uiFont.deriveFont(14f));
        actionBtn.setBounds(530, 30, 130, 40);
        actionBtn.setFocusPainted(false);
        panel.add(actionBtn);

        return panel;
    }

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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        String[] lines = msg.split("\n");
        int yPos = (lines.length == 1) ? 80 : 60;
        for (String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, yPos, 360, 30);
            panel.add(l);
            yPos += 30;
        }

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

    private boolean showConfirmPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));

        final boolean[] result = {false};

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        String[] lines = msg.split("\n");
        int yPos = (lines.length == 1) ? 80 : 60;
        for (String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, yPos, 360, 30);
            panel.add(l);
            yPos += 30;
        }

        JButton yesBtn = new JButton("네");
        yesBtn.setBounds(60, 160, 120, 45);
        yesBtn.setBackground(BROWN);
        yesBtn.setForeground(Color.WHITE);
        yesBtn.setFont(uiFont.deriveFont(16f));
        yesBtn.setBorder(new RoundedBorder(15, BROWN));
        yesBtn.setFocusPainted(false);
        yesBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setBounds(220, 160, 120, 45);
        noBtn.setBackground(BROWN);
        noBtn.setForeground(Color.WHITE);
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBorder(new RoundedBorder(15, BROWN));
        noBtn.setFocusPainted(false);
        noBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        panel.add(noBtn);

        dialog.setVisible(true);
        return result[0];
    }

    private static class RoundedBorder implements Border {
        private final int radius;
        private final Color color;

        public RoundedBorder(int r, Color c) {
            radius = r;
            color = c;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }
}
