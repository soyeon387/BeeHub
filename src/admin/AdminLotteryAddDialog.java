package admin;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AdminLotteryAddDialog extends JDialog {

    private static final Color BG_YELLOW = new Color(255, 250, 205);
    private static final Color BROWN     = new Color(139, 90, 43);
    private static final Color POPUP_BG  = new Color(255, 250, 205);

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
    private JSpinner   countSpinner;

    private JTextField annDateField;      // 발표 날짜 (DATE)

    private JTextField appStartField;     // 응모 시작 일시
    private JTextField appEndField;       // 응모 마감 일시

    private JTextField locField;          // 수령 장소
    private JTextField pickStartField;    // 수령 시작 일시
    private JTextField pickEndField;      // 수령 마감 일시

    // 날짜/시간 포맷
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");              // announcement_date : DATE
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");   // DATETIME, 초 있어도/없어도 OK

    public AdminLotteryAddDialog(AdminLotteryFrame parent) {
        super(parent, "경품 추첨 등록", true);
        this.parent = parent;

        setSize(480, 650);  // 필드 더 많아졌으니까 살짝 키움
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
        int gap  = 55;

        // 이벤트 제목
        addLabel(yPos, "이벤트 제목 (회차는 자동 생성)");
        titleField = createField(yPos + 25);
        add(titleField);
        yPos += gap;

        // 경품 + 인원
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

        // 발표 날짜 (DATE만)
        addLabel(yPos, "당첨자 발표 날짜 (예: 2025-12-10)");
        annDateField = createField(yPos + 25);
        add(annDateField);
        yPos += gap;

        // 응모 시작/마감 일시
        addLabel(yPos, "응모 시작 일시 (예: 2025-12-10 00:00 또는 2025-12-10 00:00:00)");
        appStartField = createField(yPos + 25);
        add(appStartField);
        yPos += gap;

        addLabel(yPos, "응모 마감 일시 (예: 2025-12-10 23:59 또는 2025-12-10 23:59:59)");
        appEndField = createField(yPos + 25);
        add(appEndField);
        yPos += gap;

        // 수령 장소
        addLabel(yPos, "수령 장소 (예: 학생회관 2층)");
        locField = createField(yPos + 25);
        add(locField);
        yPos += gap;

        // 수령 시작/마감 일시
        addLabel(yPos, "수령 시작 일시 (예: 2025-12-16 00:00)");
        pickStartField = createField(yPos + 25);
        add(pickStartField);
        yPos += gap;

        addLabel(yPos, "수령 마감 일시 (예: 2025-12-20 23:59)");
        pickEndField = createField(yPos + 25);
        add(pickEndField);
        yPos += gap + 15;

        // 버튼들
        JButton cancelBtn = new JButton("취소");
        cancelBtn.setBounds(100, yPos, 100, 40);
        cancelBtn.setBackground(new Color(200, 200, 200));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());
        add(cancelBtn);

        JButton okBtn = new JButton("등록");
        okBtn.setBounds(250, yPos, 100, 40);
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
        l.setBounds(30, y, 420, 20);
        add(l);
    }

    private JTextField createField(int y) {
        JTextField f = new JTextField();
        f.setBounds(30, y, 400, 30);
        f.setFont(uiFont.deriveFont(14f));
        return f;
    }

    private void saveData() {
        try {
            String title      = titleField.getText().trim();
            String prize      = prizeField.getText().trim();
            int    count      = (int) countSpinner.getValue();
            String ann        = annDateField.getText().trim();
            String appStart   = appStartField.getText().trim();
            String appEnd     = appEndField.getText().trim();
            String loc        = locField.getText().trim();
            String pickStart  = pickStartField.getText().trim();
            String pickEnd    = pickEndField.getText().trim();

            if (title.isEmpty() || prize.isEmpty() || ann.isEmpty()
                    || appStart.isEmpty() || appEnd.isEmpty()
                    || loc.isEmpty() || pickStart.isEmpty() || pickEnd.isEmpty()) {
                showMsgPopup("알림", "모든 정보를 입력해주세요.");
                return;
            }

            // 1) 발표 날짜 (DATE)
            LocalDate announcementDate = LocalDate.parse(ann, DATE_FMT);

            // 2) 응모/수령 시작/마감 (DATETIME) - 초 있어도/없어도 OK
            LocalDateTime appStartDt  = LocalDateTime.parse(appStart, DT_FMT);
            LocalDateTime appEndDt    = LocalDateTime.parse(appEnd, DT_FMT);
            LocalDateTime pickStartDt = LocalDateTime.parse(pickStart, DT_FMT);
            LocalDateTime pickEndDt   = LocalDateTime.parse(pickEnd, DT_FMT);

            // 3) 부모 프레임으로 넘기기
            parent.addRound(
                    title,
                    prize,
                    count,
                    announcementDate,
                    appStartDt,
                    appEndDt,
                    loc,
                    pickStartDt,
                    pickEndDt
            );

            showMsgPopup("성공", "등록되었습니다.");
            dispose();

        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            showMsgPopup("오류", "날짜/시간 형식이 맞지 않습니다.\n예시 형식을 다시 확인해주세요.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showMsgPopup("오류", "저장 중 오류가 발생했습니다.\n콘솔 로그를 확인해주세요.");
        }
    }

    // 🎨 이쁜 팝업
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

        JLabel l = new JLabel("<html><center>" + msg.replace("\n", "<br>") + "</center></html>",
                SwingConstants.CENTER);
        l.setFont(uiFont.deriveFont(16f));
        l.setForeground(BROWN);
        l.setBounds(20, 60, 360, 70);
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
