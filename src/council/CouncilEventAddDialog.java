package council;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CouncilEventAddDialog extends JDialog {

    private static final Color BG_MAIN = new Color(250, 250, 250);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            java.io.InputStream is = CouncilEventAddDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private final CouncilMainFrame parent;
    private final String councilId;
    private final String councilName;
    private EventManager.EventData event;   // 수정 시에는 기존 데이터, 신규는 새로 생성

    // 입력 컴포넌트들
    private JTextField titleField;
    private JComboBox<String> typeCombo;
    private JTextField dateField;
    private JTextField locationField;
    private JTextField applyStartField;
    private JTextField applyEndField;
    private JSpinner totalSpinner;
    private JTextField targetMajorField;
    private JTextField secretCodeField;
    private JComboBox<String> statusCombo;
    private JTextArea descArea;

    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CouncilEventAddDialog(CouncilMainFrame parent,
                                 EventManager.EventData event,
                                 String councilId,
                                 String councilName) {
        super(parent, true);
        this.parent = parent;
        this.event = event;
        this.councilId = councilId;
        this.councilName = councilName;

        setTitle(event == null ? "새 행사 등록" : "행사 수정");
        setSize(650, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        initUI();
        loadData();
        setVisible(true);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.CENTER);

        // 제목
        titleField = createTextRow(mainPanel, "행사 제목");

        // 타입
        typeCombo = new JComboBox<>(new String[]{"SNACK", "ACTIVITY"});
        addLabeled(mainPanel, "행사 유형", typeCombo);

        // 행사 일시
        dateField = createTextRow(mainPanel, "행사 일시 (yyyy-MM-dd HH:mm)");

        // 장소
        locationField = createTextRow(mainPanel, "장소");

        // 신청 기간
        applyStartField = createTextRow(mainPanel, "신청 시작 (yyyy-MM-dd HH:mm)");
        applyEndField = createTextRow(mainPanel, "신청 종료 (yyyy-MM-dd HH:mm)");

        // 전체 수량
        totalSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
        addLabeled(mainPanel, "전체 인원 / 수량", totalSpinner);

        // 대상 학과
        targetMajorField = createTextRow(mainPanel, "대상 학과 (예: " + councilName + ")");

        // 비밀코드
        secretCodeField = createTextRow(mainPanel, "비밀코드 (선택)");

        // 상태
        statusCombo = new JComboBox<>(new String[]{"SCHEDULED", "PROGRESS", "CLOSED"});
        addLabeled(mainPanel, "상태", statusCombo);

        // 설명
        JLabel descLabel = new JLabel("행사 설명");
        descLabel.setFont(uiFont);
        descLabel.setForeground(BROWN);
        mainPanel.add(descLabel);

        descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(uiFont.deriveFont(13f));
        JScrollPane sp = new JScrollPane(descArea);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(sp);

        mainPanel.add(Box.createVerticalStrut(10));

        // 버튼
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(BG_MAIN);

        JButton saveBtn = createButton("저장", this::onSave);
        JButton cancelBtn = createButton("취소", e -> dispose());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JTextField createTextRow(JPanel parent, String labelText) {
        JTextField field = new JTextField(25);
        addLabeled(parent, labelText, field);
        return field;
    }

    private void addLabeled(JPanel parent, String labelText, JComponent comp) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.setBackground(BG_MAIN);

        JLabel label = new JLabel(labelText);
        label.setFont(uiFont);
        label.setForeground(BROWN);
        row.add(label);

        comp.setFont(uiFont);
        row.add(comp);

        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(row);
    }

    private JButton createButton(String text, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont);
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(l);
        return btn;
    }

    // 기존 데이터가 있을 때 필드 채우기
    private void loadData() {
        if (event == null) return;

        titleField.setText(event.title != null ? event.title : "");
        typeCombo.setSelectedItem(event.eventType != null ? event.eventType : "SNACK");
        if (event.date != null) dateField.setText(event.date.format(INPUT_FMT));
        locationField.setText(event.location != null ? event.location : "");
        if (event.applyStart != null) applyStartField.setText(event.applyStart.format(INPUT_FMT));
        if (event.applyEnd != null) applyEndField.setText(event.applyEnd.format(INPUT_FMT));
        totalSpinner.setValue(event.totalCount > 0 ? event.totalCount : 100);
        targetMajorField.setText(event.targetDept != null ? event.targetDept : "");
        secretCodeField.setText(event.secretCode != null ? event.secretCode : "");
        descArea.setText(event.description != null ? event.description : "");

        // status는 DB 값 그대로 사용 (SCHEDULED / PROGRESS / CLOSED)
        if (event.status != null) {
            if ("종료".equals(event.status)) statusCombo.setSelectedItem("CLOSED");
            else statusCombo.setSelectedItem("SCHEDULED");
        }
    }

    private void onSave(ActionEvent e) {
        try {
            if (event == null) {
                event = new EventManager.EventData();
            }

            event.title = titleField.getText().trim();
            event.eventType = (String) typeCombo.getSelectedItem();
            event.location = locationField.getText().trim();
            event.targetDept = targetMajorField.getText().trim();
            event.secretCode = secretCodeField.getText().trim();
            event.description = descArea.getText().trim();
            event.totalCount = (Integer) totalSpinner.getValue();
            event.ownerHakbun = councilId; // 주최 학생회 ID

            String status = (String) statusCombo.getSelectedItem();
            event.status = "CLOSED".equals(status) ? "종료" : "진행중";

            // 날짜 파싱 (비어 있으면 null)
            event.date = parseDateTime(dateField.getText().trim());
            event.startDateTime = event.date; // 예전 필드와 동기화
            event.applyStart = parseDateTime(applyStartField.getText().trim());
            event.applyEnd = parseDateTime(applyEndField.getText().trim());

            // 현재 신청 인원은 0으로 두고, 남은 수량 = total 로 초기화하고 싶으면:
            if (event.eventId == 0) {
                event.currentCount = 0;
            }

            // DB 저장
            EventManager.addEvent(event);

            // 부모 화면 새로고침
            parent.refreshLists();

            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "저장 중 오류가 발생했습니다.\n" + ex.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDateTime parseDateTime(String text) {
        if (text == null || text.isEmpty()) return null;
        return LocalDateTime.parse(text, INPUT_FMT);
    }
}
