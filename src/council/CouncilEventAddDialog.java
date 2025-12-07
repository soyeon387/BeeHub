package council;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*; // [중요] 텍스트 스타일링을 위해 추가
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import council.EventManager.FeeType;

public class CouncilEventAddDialog extends JDialog {

    private static final Color BG_WHITE = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);

    // ✅ 사용자가 입력/보는 날짜 포맷 (라벨 설명이랑 맞춤)
    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private static Font uiFont;
    static {
        try {
            InputStream is = CouncilEventAddDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 12); }
    }

    private CouncilMainFrame parent;
    private EventManager.EventData currentEvent;
    
    // 주최자 정보
    private String councilId;
    private String councilName; // 지금은 따로 저장 안 하지만, 필요하면 EventManager에서 사용 가능

    // 입력 필드들
    private JTextField titleField, dateField, locField, startField, endField, totalField, codeField;
    private JComboBox<String> feeCombo; 
    private JCheckBox codeCheck;
    private JTextArea descArea;

    // 행사 유형 / 대상 학과
    private JComboBox<String> eventTypeCombo;   // 간식 / 활동
    private JTextField targetDeptField;         // 대상 학과 (전체 / 수학과 ...)

    public CouncilEventAddDialog(CouncilMainFrame parent, EventManager.EventData event, String id, String name) {
        super(parent, event == null ? "새 행사 등록" : "행사 수정", true);
        this.parent = parent;
        this.currentEvent = event;
        this.councilId = id;
        this.councilName = name;

        setSize(500, 800);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_WHITE);

        initUI();
        if (event != null) loadData();
        setVisible(true);
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 15));
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        formPanel.setBackground(BG_WHITE);

        // 1) 행사명
        titleField = addInput(formPanel, "행사명");

        // 2) 행사 유형 (간식 / 활동)
        JPanel typePanel = new JPanel(new BorderLayout(0, 5));
        typePanel.setBackground(BG_WHITE);
        JLabel typeLabel = new JLabel("행사 유형");
        typeLabel.setFont(uiFont.deriveFont(14f));
        typeLabel.setForeground(BROWN);

        String[] typeOptions = { "간식 행사", "활동 행사" };
        eventTypeCombo = new JComboBox<>(typeOptions);
        eventTypeCombo.setFont(uiFont.deriveFont(14f));
        eventTypeCombo.setBackground(Color.WHITE);

        typePanel.add(typeLabel, BorderLayout.NORTH);
        typePanel.add(eventTypeCombo, BorderLayout.CENTER);
        formPanel.add(typePanel);

        // 3) 대상 학과
        targetDeptField = addInput(formPanel, "대상 학과 (예: 전체 / 수학과)");

        // 4) 날짜/장소/신청 기간/인원
        dateField = addInput(formPanel, "행사 일시 (yyyy-MM-dd HH:mm)");
        locField = addInput(formPanel, "장소 (예: 50주년 기념관)");
        startField = addInput(formPanel, "신청 시작 일시 (yyyy-MM-dd HH:mm)");
        endField = addInput(formPanel, "신청 종료 일시 (yyyy-MM-dd HH:mm)");
        totalField = addInput(formPanel, "총 모집 인원 (숫자만)");

        // 5) 납부 대상 선택 (회비 조건)
        JPanel feePanel = new JPanel(new BorderLayout(0, 5));
        feePanel.setBackground(BG_WHITE);
        JLabel feeLabel = new JLabel("납부 대상 (참여 조건)");
        feeLabel.setFont(uiFont.deriveFont(14f));
        feeLabel.setForeground(BROWN);
        
        String[] feeOptions = {
            FeeType.NONE.getLabel(),
            FeeType.SCHOOL.getLabel(),
            FeeType.DEPT.getLabel()
        };
        feeCombo = new JComboBox<>(feeOptions);
        feeCombo.setFont(uiFont.deriveFont(14f));
        feeCombo.setBackground(Color.WHITE);
        
        feePanel.add(feeLabel, BorderLayout.NORTH);
        feePanel.add(feeCombo, BorderLayout.CENTER);
        formPanel.add(feePanel);

        // 6) 비밀코드 섹션
        JPanel codePanel = new JPanel(new BorderLayout(10, 0));
        codePanel.setBackground(BG_WHITE);
        codeCheck = new JCheckBox("비밀코드 사용");
        codeCheck.setFont(uiFont.deriveFont(14f));
        codeCheck.setBackground(BG_WHITE);
        codeCheck.setForeground(BROWN);
        codePanel.add(codeCheck, BorderLayout.WEST);
        
        codeField = new JTextField();
        codeField.setFont(uiFont.deriveFont(14f));
        codeField.setEnabled(false);
        codePanel.add(codeField, BorderLayout.CENTER);
        
        codeCheck.addActionListener(e -> codeField.setEnabled(codeCheck.isSelected()));
        formPanel.add(codePanel);

        // 7) 상세설명
        JLabel descLabel = new JLabel("상세 설명");
        descLabel.setFont(uiFont.deriveFont(14f));
        descLabel.setForeground(BROWN);
        formPanel.add(descLabel);
        
        descArea = new JTextArea(4, 20);
        descArea.setFont(uiFont.deriveFont(14f));
        descArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        descArea.setLineWrap(true);
        formPanel.add(new JScrollPane(descArea));

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(BG_WHITE);
        
        JButton cancelBtn = createBtn("취소", Color.LIGHT_GRAY);
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = createBtn("저장", BROWN);
        saveBtn.addActionListener(e -> saveData());
        btnPanel.add(saveBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JTextField addInput(JPanel p, String labelText) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setBackground(BG_WHITE);
        JLabel l = new JLabel(labelText);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(BROWN);
        JTextField tf = new JTextField();
        tf.setFont(uiFont.deriveFont(14f));
        tf.setPreferredSize(new Dimension(0, 35));
        row.add(l, BorderLayout.NORTH);
        row.add(tf, BorderLayout.CENTER);
        p.add(row);
        return tf;
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(16f));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(120, 45));
        b.setFocusPainted(false);
        return b;
    }

    private void loadData() {
        titleField.setText(currentEvent.title);

        // 날짜들 표시: INPUT_FMT 사용
        if (currentEvent.date != null) {
            dateField.setText(currentEvent.date.format(INPUT_FMT));
        }

        locField.setText(currentEvent.location != null ? currentEvent.location : "");

        if (currentEvent.applyStart != null) {
            startField.setText(currentEvent.applyStart.format(INPUT_FMT));
        }
        if (currentEvent.applyEnd != null) {
            endField.setText(currentEvent.applyEnd.format(INPUT_FMT));
        }

        totalField.setText(String.valueOf(currentEvent.totalCount));

        // 행사 유형 (String: "SNACK"/"ACTIVITY")
        if ("SNACK".equalsIgnoreCase(currentEvent.eventType)) {
            eventTypeCombo.setSelectedItem("간식 행사");
        } else {
            eventTypeCombo.setSelectedItem("활동 행사");
        }

        // 대상 학과
        if (currentEvent.targetDept != null) {
            targetDeptField.setText(currentEvent.targetDept);
        }

        if (currentEvent.requiredFee != null) {
            feeCombo.setSelectedItem(currentEvent.requiredFee.getLabel());
        }

        if (currentEvent.secretCode != null && !currentEvent.secretCode.isEmpty()) {
            codeCheck.setSelected(true);
            codeField.setEnabled(true);
            codeField.setText(currentEvent.secretCode);
        }

        descArea.setText(currentEvent.description != null ? currentEvent.description : "");
    }

    private void saveData() {
        try {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                showCustomAlertPopup("행사명을 입력하세요.");
                return;
            }

            // 대상 학과 처리
            String targetDept = targetDeptField.getText().trim();
            if (targetDept.isEmpty()) {
                targetDept = "전체";   // 기본값: 전체 학생 대상
            }
            
            // 날짜 변환 (행사일, 신청 시작/종료) - INPUT_FMT 사용
            LocalDateTime eventDate, applyStart, applyEnd;
            try {
                eventDate  = LocalDateTime.parse(dateField.getText().trim(),  INPUT_FMT);
                applyStart = LocalDateTime.parse(startField.getText().trim(), INPUT_FMT);
                applyEnd   = LocalDateTime.parse(endField.getText().trim(),   INPUT_FMT);
                
                if (applyEnd.isBefore(applyStart)) {
                    showCustomAlertPopup("신청 종료일이 시작일보다\n빠를 수 없습니다.");
                    return;
                }
                
                if (applyStart.isAfter(eventDate) || applyEnd.isAfter(eventDate)) {
                    showCustomAlertPopup("신청 기간은 행사 일시보다\n이전이어야 합니다.");
                    return;
                }

            } catch (DateTimeParseException e) {
                showCustomAlertPopup("날짜 형식이 올바르지 않습니다.\n(2023-12-05 12:00)\n형식으로 다시 입력해주세요.");
                return;
            }

            String code = codeCheck.isSelected() ? codeField.getText().trim() : null;
            int total = Integer.parseInt(totalField.getText().trim());

            // 회비 조건
            String selectedFee = (String) feeCombo.getSelectedItem();
            FeeType feeType = FeeType.NONE;
            if (selectedFee.equals(FeeType.SCHOOL.getLabel())) feeType = FeeType.SCHOOL;
            else if (selectedFee.equals(FeeType.DEPT.getLabel())) feeType = FeeType.DEPT;

            // 행사 유형 매핑 (String)
            String typeLabel = (String) eventTypeCombo.getSelectedItem();
            String eventTypeStr = "ACTIVITY";
            if ("간식 행사".equals(typeLabel)) {
                eventTypeStr = "SNACK";
            }

            if (currentEvent == null) {
                // 새 EventData 생성
                EventManager.EventData newEvent = new EventManager.EventData();

                newEvent.ownerHakbun = councilId;    // 주최 학생회 학번/아이디
                newEvent.title = title;
                newEvent.date = eventDate;
                newEvent.location = locField.getText().trim();
                newEvent.applyStart = applyStart;
                newEvent.applyEnd = applyEnd;
                newEvent.totalCount = total;
                newEvent.currentCount = 0;
                newEvent.secretCode = code;
                newEvent.description = descArea.getText();
                newEvent.status = "진행중";
                newEvent.requiredFee = feeType;

                newEvent.eventType = eventTypeStr;
                newEvent.targetDept = targetDept;

                // 행사 시작/끝 일시를 따로 쓰는 로직이 있다면 여기서 설정
                newEvent.startDateTime = eventDate;
                newEvent.endDateTime   = eventDate;

                EventManager.addEvent(newEvent);
            } else {
                // 수정 모드
                currentEvent.title = title;
                currentEvent.date = eventDate; 
                currentEvent.location = locField.getText().trim();
                currentEvent.applyStart = applyStart;
                currentEvent.applyEnd = applyEnd;
                currentEvent.totalCount = total;
                currentEvent.secretCode = code;
                currentEvent.description = descArea.getText();
                currentEvent.requiredFee = feeType; 

                currentEvent.eventType = eventTypeStr;
                currentEvent.targetDept = targetDept;

                currentEvent.startDateTime = eventDate;
                currentEvent.endDateTime   = eventDate;
            }
            
            parent.refreshLists();
            dispose();
            showCustomAlertPopup("저장되었습니다.");
            
        } catch (NumberFormatException ex) {
            showCustomAlertPopup("인원은 숫자만 입력하세요.");
        }
    }

    // 예쁜 팝업
    private void showCustomAlertPopup(String message) {
        JDialog dialog = new JDialog(this, "알림", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 250, 205)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setOpaque(false);
        textPanel.setBounds(20, 40, 360, 110); 
        panel.add(textPanel);

        JTextPane msgPane = new JTextPane();
        msgPane.setText(message);
        msgPane.setFont(uiFont.deriveFont(18f));
        msgPane.setForeground(BROWN);
        msgPane.setOpaque(false);
        msgPane.setEditable(false);
        msgPane.setFocusable(false);
        
        StyledDocument doc = msgPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        textPanel.add(msgPane);

        JButton okBtn = createBtn("확인", BROWN);
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }
}
