package admin;

import javax.swing.*;
import javax.swing.border.Border; // [추가] 테두리 사용을 위해 추가
import java.awt.*;
import java.io.InputStream;

public class AdminLotteryAddDialog extends JDialog {

    private static final Color BG_YELLOW = new Color(255, 250, 205);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    
    private static Font uiFont;
    static {
        try {
            InputStream is = AdminLotteryAddDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 12); }
    }

    private AdminLotteryFrame parent;
    
    private JTextField titleField;
    private JTextField prizeField;
    private JSpinner countSpinner;
    private JTextField annDateField;
    private JTextField appPeriodField;
    private JTextField locField;
    private JTextField pickPeriodField;

    public AdminLotteryAddDialog(AdminLotteryFrame parent) {
        super(parent, "경품 추첨 등록", true);
        this.parent = parent;

        setSize(450, 550);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(BG_YELLOW);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JLabel titleLabel = new JLabel("새로운 경품 행사 등록");
        titleLabel.setFont(uiFont.deriveFont(18f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 300, 30);
        add(titleLabel);

        int yPos = 70;
        int gap = 60; 

        addLabel(yPos, "이벤트 제목 (회차 자동)");
        titleField = createField(yPos + 25);
        add(titleField);
        yPos += gap;

        addLabel(yPos, "경품명");
        prizeField = new JTextField();
        prizeField.setBounds(30, yPos + 25, 250, 30);
        prizeField.setFont(uiFont.deriveFont(14f));
        add(prizeField);

        JLabel countLabel = new JLabel("인원");
        countLabel.setFont(uiFont.deriveFont(14f));
        countLabel.setForeground(BROWN);
        countLabel.setBounds(300, yPos, 50, 20);
        add(countLabel);

        countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        countSpinner.setBounds(300, yPos + 25, 100, 30);
        add(countSpinner);
        yPos += gap;

        addLabel(yPos, "당첨자 발표 일시 (예: 2024-05-20 14:00)");
        annDateField = createField(yPos + 25);
        add(annDateField);
        yPos += gap;

        addLabel(yPos, "응모 기간 (예: 05.01 ~ 05.15)");
        appPeriodField = createField(yPos + 25);
        add(appPeriodField);
        yPos += gap;

        addLabel(yPos, "수령 장소 (예: 학생회관 2층)");
        locField = createField(yPos + 25);
        add(locField);
        yPos += gap;

        addLabel(yPos, "수령 기간 (예: 05.21 ~ 05.25)");
        pickPeriodField = createField(yPos + 25);
        add(pickPeriodField);
        yPos += gap + 10;

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setBounds(100, yPos, 100, 40);
        cancelBtn.setBackground(new Color(200, 200, 200));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());
        add(cancelBtn);

        JButton okBtn = new JButton("등록");
        okBtn.setBounds(230, yPos, 100, 40);
        okBtn.setBackground(BROWN);
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> saveData());
        add(okBtn);
    }

    private void addLabel(int y, String text) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(BROWN);
        l.setBounds(30, y, 300, 20);
        add(l);
    }

    private JTextField createField(int y) {
        JTextField f = new JTextField();
        f.setBounds(30, y, 370, 30);
        f.setFont(uiFont.deriveFont(14f));
        return f;
    }

    private void saveData() {
        String title = titleField.getText().trim();
        String prize = prizeField.getText().trim();
        int count = (int) countSpinner.getValue();
        
        String annDate = annDateField.getText().trim();
        String appPeriod = appPeriodField.getText().trim();
        String loc = locField.getText().trim();
        String pickPeriod = pickPeriodField.getText().trim();

        if (title.isEmpty() || prize.isEmpty() || annDate.isEmpty() || 
            appPeriod.isEmpty() || loc.isEmpty() || pickPeriod.isEmpty()) {
            showMsgPopup("알림", "모든 정보를 입력해주세요.");
            return;
        }

        parent.addRound(title, prize, count, annDate, appPeriod, loc, pickPeriod);
        showMsgPopup("성공", "등록되었습니다.");
        dispose();
    }

    // 🎨 이쁜 팝업
    private void showMsgPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0,0,0,0));

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

        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setFont(uiFont.deriveFont(18f));
        l.setForeground(BROWN);
        l.setBounds(20, 80, 360, 30);
        panel.add(l);

        JButton okBtn = new JButton("확인");
        okBtn.setFont(uiFont.deriveFont(16f));
        okBtn.setBackground(BROWN);
        okBtn.setForeground(Color.WHITE);
        okBtn.setBounds(135, 170, 130, 45);
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }
}