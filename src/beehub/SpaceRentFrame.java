package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpaceRentFrame extends JFrame {

    // ===============================
    // 🎨 컬러 및 폰트 설정
    // ===============================
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color LIGHT_BROWN = new Color(160, 120, 80);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);

    // 버튼 색
    private static final Color BTN_OFF_BG = new Color(250, 250, 250);
    private static final Color BTN_ON_BG  = BROWN;
    private static final Color BTN_ON_FG  = Color.WHITE;
    private static final Color BTN_OFF_FG = new Color(100, 100, 100);
    private static final Color BTN_DISABLED_BG = new Color(230, 230, 230);
    private static final Color BTN_DISABLED_FG = new Color(180, 180, 180);

    // 시간 라벨
    private String[] timeLabels = {
        "09:00", "10:00", "11:00", "12:00",
        "13:00", "14:00", "15:00", "16:00",
        "17:00", "18:00", "19:00", "20:00"
    };

    private static Font uiFont;
    static {
        try {
            InputStream is = SpaceRentFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    // 사용자 정보
    private String userName = "게스트";
    private int userPoint = 0;
    private String myHakbun =
            (LoginSession.getUser() != null) ?
                    LoginSession.getUser().getHakbun() : "20231234";

    // UI 컴포넌트
    private JComboBox<String> spaceCombo;
    private JComboBox<Integer> yearCombo, monthCombo, dayCombo;
    private JPanel partnerContainer;
    private JTextField myNameField;

    // 동반인 관리
    private List<PartnerEntry> partnerEntries = new ArrayList<>();

    // 시간 버튼들
    private ArrayList<JToggleButton> timeButtons = new ArrayList<>();
    private int selectedTimeCount = 0;

    // 날짜 제한
    private LocalDate today;
    private LocalDate maxDate;

    // DAO
    private SpaceInfoDAO spaceInfoDAO = new SpaceInfoDAO();
    private SpaceReservationDAO reservationDAO = new SpaceReservationDAO();

    // 공간 목록
    private List<SpaceInfo> spaceList = new ArrayList<>();
    private Map<String, Integer> spaceNameToId = new HashMap<>();

    // ===============================
    // 생성자
    // ===============================

    public SpaceRentFrame() {
        Member loginUser = LoginSession.getUser();
        if (loginUser != null) {
            this.userName = loginUser.getName();
            this.userPoint = loginUser.getPoint();
            this.myHakbun = loginUser.getHakbun();
        }
        initFrame();
    }

    public SpaceRentFrame(String userName, int userPoint) {
        this.userName = userName;
        this.userPoint = userPoint;
        Member loginUser = LoginSession.getUser();
        if (loginUser != null) {
            this.myHakbun = loginUser.getHakbun();
        }
        initFrame();
    }

    // 공통 초기화
    private void initFrame() {
        setTitle("서울여대 꿀단지 - 공간대여");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initHeaderAndNav();
        initContent();

        setVisible(true);
    }

    // ===============================
    // 헤더 & 네비게이션
    // ===============================
    private void initHeaderAndNav() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 850, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(logoLabel);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(450, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfoText = new JLabel("[" + userName + "]님" +  " | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });

        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);

        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 850, 50);
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 2);
            navPanel.add(menuBtn);
        }
    }

    // ===============================
    // 메인 콘텐츠
    // ===============================
    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 850, 520);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // === LEFT PANEL (일시 선택) ===
        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(30, 30, 380, 430);
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        contentPanel.add(leftPanel);

        JLabel leftTitle = new JLabel("1. 예약 일시 선택");
        leftTitle.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        leftTitle.setForeground(BROWN);
        leftTitle.setBounds(25, 25, 200, 25);
        leftPanel.add(leftTitle);

        // 공간 선택
        addLabel(leftPanel, "공간 선택", 65);
        spaceCombo = new JComboBox<>();
        spaceCombo.setRenderer(new SpaceListRenderer());
        styleComboBox(spaceCombo);
        spaceCombo.setBounds(25, 90, 330, 40);
        spaceCombo.addActionListener(e -> updateTimeSlotAvailability());
        leftPanel.add(spaceCombo);

        loadSpacesIntoCombo();

        // 날짜 선택
        addLabel(leftPanel, "날짜 선택", 145);
        JPanel datePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        datePanel.setOpaque(false);
        datePanel.setBounds(25, 170, 330, 40);

        yearCombo = new JComboBox<>();
        monthCombo = new JComboBox<>();
        dayCombo = new JComboBox<>();
        styleComboBox(yearCombo);
        styleComboBox(monthCombo);
        styleComboBox(dayCombo);

        initDateLogic();

        datePanel.add(yearCombo);
        datePanel.add(monthCombo);
        datePanel.add(dayCombo);
        leftPanel.add(datePanel);

        // 시간 선택
        addLabel(leftPanel, "시간 선택", 225);
        JPanel timeGridPanel = new JPanel(new GridLayout(3, 4, 6, 6));
        timeGridPanel.setBounds(25, 255, 330, 120);
        timeGridPanel.setOpaque(false);

        for (String time : timeLabels) {
            JToggleButton btn = createTimeButton(time);
            timeButtons.add(btn);
            timeGridPanel.add(btn);
        }
        leftPanel.add(timeGridPanel);

        // === RIGHT PANEL (예약자 정보) ===
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBounds(430, 30, 390, 430);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        contentPanel.add(rightPanel);

        JLabel rightTitle = new JLabel("2. 예약자 정보");
        rightTitle.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        rightTitle.setForeground(BROWN);
        rightTitle.setBounds(25, 25, 200, 25);
        rightPanel.add(rightTitle);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBounds(25, 60, 340, 30);
        JLabel info1 = new JLabel("※ "); info1.setForeground(Color.GRAY);
        JLabel info2 = new JLabel("하루 최대 3시간"); info2.setForeground(new Color(220, 50, 50));
        JLabel info3 = new JLabel("까지 이용 가능합니다."); info3.setForeground(Color.GRAY);
        infoPanel.add(info1); infoPanel.add(info2); infoPanel.add(info3);
        rightPanel.add(infoPanel);

        // 신청자 이름
        addLabel(rightPanel, "신청자 이름 (자동 입력)", 100);
        myNameField = new JTextField(userName);
        myNameField.setFont(uiFont.deriveFont(16f));
        myNameField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        myNameField.setBounds(25, 125, 340, 40);
        myNameField.setBackground(new Color(245, 245, 245));
        myNameField.setEditable(false);
        rightPanel.add(myNameField);

        JLabel partnerLabel = new JLabel("동반인 정보 (최대 5명)");
        partnerLabel.setFont(uiFont.deriveFont(14f));
        partnerLabel.setForeground(LIGHT_BROWN);
        partnerLabel.setBounds(25, 185, 200, 20);
        rightPanel.add(partnerLabel);

        JButton addPartnerBtn = new JButton("+ 추가");
        addPartnerBtn.setFont(uiFont.deriveFont(12f));
        addPartnerBtn.setForeground(BROWN);
        addPartnerBtn.setBackground(Color.WHITE);
        addPartnerBtn.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        addPartnerBtn.setBounds(305, 180, 60, 25);
        addPartnerBtn.setFocusPainted(false);
        addPartnerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addPartnerBtn.addActionListener(e -> addPartnerRow());
        rightPanel.add(addPartnerBtn);

        partnerContainer = new JPanel();
        partnerContainer.setLayout(new BoxLayout(partnerContainer, BoxLayout.Y_AXIS));
        partnerContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(partnerContainer);
        scrollPane.setBounds(25, 215, 340, 140);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(scrollPane);

        JButton rentBtn = new JButton("예약 완료");
        rentBtn.setFont(uiFont.deriveFont(20f));
        rentBtn.setBackground(BROWN);
        rentBtn.setForeground(Color.WHITE);
        rentBtn.setBounds(25, 370, 340, 45);
        rentBtn.setFocusPainted(false);
        rentBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        rentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rentBtn.addActionListener(e -> handleRentAction());
        rightPanel.add(rentBtn);

        updateTimeSlotAvailability();
    }

    // ===============================
    // 공간 콤보박스 채우기
    // ===============================
    private void loadSpacesIntoCombo() {
        spaceCombo.removeAllItems();
        spaceNameToId.clear();

        spaceCombo.addItem("-- 공간을 선택해주세요 --");

        // 전체 활성 공간 가져오기
        spaceList = spaceInfoDAO.getActiveSpaces();

        String currentType = "";

        for (SpaceInfo s : spaceList) {

            // ✅ 세미나실 / 실습실만 예약 대상
            String type = s.getRoomType();
            if (!"세미나실".equals(type) && !"실습실".equals(type)) {
                continue;
            }

            // 타입 헤더(=== 세미나실 ===, === 실습실 ===) 한 번씩만 추가
            if (!type.equals(currentType)) {
                currentType = type;
                spaceCombo.addItem("=== " + currentType + " ===");
            }

            String label = s.getBuildingName() + " " + s.getRoomName();
            spaceCombo.addItem(label);
            spaceNameToId.put(label, s.getSpaceId());
        }
    }

    // ===============================
    // 동반인 추가
    // ===============================
    private void addPartnerRow() {
        if (partnerEntries.size() >= 5) {
            showSimplePopup("알림", "동반인은 최대 5명까지만 가능합니다.");
            return;
        }

        JPanel row = new JPanel(new GridLayout(1, 2, 5, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(340, 40));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JTextField nameField = new JTextField("이름");
        styleTextField(nameField);
        addPlaceholderEffect(nameField, "이름");

        JTextField idField = new JTextField("학번");
        styleTextField(idField);
        addPlaceholderEffect(idField, "학번");

        row.add(nameField);
        row.add(idField);

        partnerEntries.add(new PartnerEntry(nameField, idField, row));

        partnerContainer.add(row);
        partnerContainer.revalidate();
        partnerContainer.repaint();
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(uiFont.deriveFont(14f));
        tf.setForeground(Color.GRAY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void addPlaceholderEffect(JTextField tf, String placeholder) {
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }

    // ===============================
    // 예약 처리
    // ===============================
    private void handleRentAction() {
        int selectedIndex = spaceCombo.getSelectedIndex();
        String spaceLabel = (String) spaceCombo.getSelectedItem();

        if (selectedIndex == 0 || spaceLabel == null || spaceLabel.startsWith("===")) {
            showSimplePopup("알림", "유효한 공간을 선택해주세요.");
            return;
        }

        // 날짜
        int y = (Integer) yearCombo.getSelectedItem();
        int m = (Integer) monthCombo.getSelectedItem();
        int d = (Integer) dayCombo.getSelectedItem();
        LocalDate date = LocalDate.of(y, m, d);

        // 하루 3시간 제한
        int usedHours = reservationDAO.getUsedHoursForUser(myHakbun, date);
        if (usedHours + selectedTimeCount > 3) {
            showSimplePopup("이용 한도 초과",
                    "선택하신 날짜에 이미 " + usedHours + "시간을 예약하셨습니다.\n" +
                    "※하루 최대 3시간 규정");
            return;
        }

        // 최소 1시간 이상
        if (selectedTimeCount == 0) {
            showSimplePopup("알림", "최소 1시간 이상 선택해주세요.");
            return;
        }

        // 동반인 체크
        boolean hasPartner = false;
        int partnerCount = 0;
        StringBuilder partners = new StringBuilder();
        for (PartnerEntry pe : partnerEntries) {
            String name = pe.nameField.getText().trim();
            if (!name.isEmpty() && !name.equals("이름")) {
                hasPartner = true;
                partnerCount++;
                partners.append(name).append(", ");
            }
        }
        if (!hasPartner) {
            showSimplePopup("예약 불가", "최소 2인 이상(동반인 필수)\n부터 예약 가능합니다.");
            return;
        }
        if (partners.length() > 0) partners.setLength(partners.length() - 2);

        // 선택된 시간 → 정수 리스트
        ArrayList<Integer> selectedHours = new ArrayList<>();
        for (JToggleButton btn : timeButtons) {
            if (btn.isSelected()) {
                String t = btn.getText().split(":")[0];
                selectedHours.add(Integer.parseInt(t));
            }
        }
        Collections.sort(selectedHours);

        // 표시용 시간 문자열
        StringBuilder timeStrBuilder = new StringBuilder();
        if (!selectedHours.isEmpty()) {
            int startH = selectedHours.get(0);
            int prevH = startH;
            for (int i = 1; i < selectedHours.size(); i++) {
                int currentH = selectedHours.get(i);
                if (currentH > prevH + 1) {
                    timeStrBuilder.append(formatTime(startH))
                            .append(" ~ ")
                            .append(formatTime(prevH + 1))
                            .append(" / ");
                    startH = currentH;
                }
                prevH = currentH;
            }
            timeStrBuilder.append(formatTime(startH))
                          .append(" ~ ")
                          .append(formatTime(prevH + 1));
        }
        String timeStr = timeStrBuilder.toString();
        String dateStr = y + "년 " + m + "월 " + d + "일";

        Integer spaceId = spaceNameToId.get(spaceLabel);
        if (spaceId == null) {
            showSimplePopup("오류", "공간 ID를 찾을 수 없습니다.");
            return;
        }

        try {
            reservationDAO.insertReservation(spaceId, date, selectedHours, myHakbun);
        } catch (Exception ex) {
            ex.printStackTrace();
            showSimplePopup("오류", "예약 저장 중 오류가 발생했습니다.");
            return;
        }

        showSuccessPopup(spaceLabel, dateStr, timeStr, partnerCount + 1);
        selectedTimeCount = 0;
        updateTimeSlotAvailability();
    }

    private void showSuccessPopup(String space, String date, String timeRange, int totalPeople) {
        JDialog dialog = new JDialog(this, "예약 완료", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = createPopupPanel();
        dialog.add(panel);
        panel.setLayout(null);

        JLabel label1 = new JLabel("예약 일자 : " + date, SwingConstants.CENTER);
        label1.setFont(uiFont.deriveFont(15f));
        label1.setForeground(BROWN);
        label1.setBounds(20, 40, 380, 25);
        panel.add(label1);

        JLabel labelTime = new JLabel(timeRange, SwingConstants.CENTER);
        labelTime.setFont(uiFont.deriveFont(15f));
        labelTime.setForeground(BROWN);
        labelTime.setBounds(20, 65, 380, 25);
        panel.add(labelTime);

        JLabel label2 = new JLabel("[ " + space + " ], 인원 " + totalPeople + "명 예약되었습니다.",
                                   SwingConstants.CENTER);
        label2.setFont(uiFont.deriveFont(15f));
        label2.setForeground(BROWN);
        label2.setBounds(20, 115, 380, 20);
        panel.add(label2);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 200, 150, 50);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    // 동반인 엔트리
    private static class PartnerEntry {
        JTextField nameField;
        JTextField idField;
        JPanel panel;
        public PartnerEntry(JTextField n, JTextField i, JPanel p) {
            this.nameField = n; this.idField = i; this.panel = p;
        }
    }

    // ===============================
    // 날짜 로직 (오늘 ~ 3개월 후)
    // ===============================
    private void initDateLogic() {
        today = LocalDate.now();
        maxDate = today.plusMonths(3);

        yearCombo.addItem(today.getYear());
        if (maxDate.getYear() > today.getYear()) {
            yearCombo.addItem(maxDate.getYear());
        }

        updateMonths();
        updateDays();

        yearCombo.addActionListener(e -> {
            updateMonths();
            updateTimeSlotAvailability();
        });
        monthCombo.addActionListener(e -> {
            updateDays();
            updateTimeSlotAvailability();
        });
        dayCombo.addActionListener(e -> updateTimeSlotAvailability());
    }

    private void updateMonths() {
        if (yearCombo.getSelectedItem() == null) return;

        int selectedYear = (Integer) yearCombo.getSelectedItem();
        monthCombo.removeAllItems();

        int startMonth = 1;
        int endMonth = 12;

        if (selectedYear == today.getYear()) {
            startMonth = today.getMonthValue();
        }
        if (selectedYear == maxDate.getYear()) {
            endMonth = maxDate.getMonthValue();
        }

        for (int i = startMonth; i <= endMonth; i++) {
            monthCombo.addItem(i);
        }
    }

    private void updateDays() {
        if (yearCombo.getSelectedItem() == null || monthCombo.getSelectedItem() == null) return;

        int selectedYear = (Integer) yearCombo.getSelectedItem();
        int selectedMonth = (Integer) monthCombo.getSelectedItem();

        dayCombo.removeAllItems();

        int startDay = 1;
        int lastDayOfThisMonth = LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth();
        int endDay = lastDayOfThisMonth;

        if (selectedYear == today.getYear() && selectedMonth == today.getMonthValue()) {
            startDay = today.getDayOfMonth();
        }
        if (selectedYear == maxDate.getYear() && selectedMonth == maxDate.getMonthValue()) {
            endDay = maxDate.getDayOfMonth();
        }

        for (int i = startDay; i <= endDay; i++) {
            dayCombo.addItem(i);
        }
    }

    // ===============================
    // 시간 버튼 활성/비활성 (DB 기반)
    // ===============================
    private void updateTimeSlotAvailability() {

        // ✅ 아직 날짜 콤보박스가 만들어지기 전이면 그냥 리턴
        if (yearCombo == null || monthCombo == null || dayCombo == null) {
            return;
        }

        String selectedSpace = (String) spaceCombo.getSelectedItem();
        Object y = yearCombo.getSelectedItem();
        Object m = monthCombo.getSelectedItem();
        Object d = dayCombo.getSelectedItem();

        if (selectedSpace == null || y == null || m == null || d == null) return;
        if (selectedSpace.startsWith("--") || selectedSpace.startsWith("===")) return;

        Integer spaceId = spaceNameToId.get(selectedSpace);
        if (spaceId == null) return;

        LocalDate date = LocalDate.of((Integer) y, (Integer) m, (Integer) d);

        List<String> bookedTimes = reservationDAO.getBookedTimeSlots(spaceId, date);

        for (JToggleButton btn : timeButtons) {
            String time = btn.getText();
            if (bookedTimes.contains(time)) {
                btn.setEnabled(false);
                btn.setBackground(BTN_DISABLED_BG);
                btn.setForeground(BTN_DISABLED_FG);
                btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                if (btn.isSelected()) {
                    btn.setSelected(false);
                    if (selectedTimeCount > 0) selectedTimeCount--;
                }
            } else {
                btn.setEnabled(true);
                if (!btn.isSelected()) {
                    btn.setBackground(BTN_OFF_BG);
                    btn.setForeground(BTN_OFF_FG);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                }
            }
        }
    }

    private JToggleButton createTimeButton(String time) {
        JToggleButton btn = new JToggleButton(time);
        btn.setFont(uiFont.deriveFont(12f));
        btn.setBackground(BTN_OFF_BG);
        btn.setForeground(BTN_OFF_FG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btn.addActionListener(e -> {
            if (btn.isSelected()) {
                if (selectedTimeCount >= 3) {
                    btn.setSelected(false);
                    showSimplePopup("알림", "하루 최대 3시간까지 선택할 수 있습니다.");
                } else {
                    selectedTimeCount++;
                    btn.setBackground(BTN_ON_BG);
                    btn.setForeground(BTN_ON_FG);
                }
            } else {
                selectedTimeCount--;
                btn.setBackground(BTN_OFF_BG);
                btn.setForeground(BTN_OFF_FG);
            }
        });
        return btn;
    }

    private void addLabel(JPanel p, String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(LIGHT_BROWN);
        l.setBounds(25, y, 250, 20);
        p.add(l);
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(uiFont.deriveFont(14f));
        box.setBackground(Color.WHITE);
        box.setForeground(BROWN);
    }

    private String formatTime(int hour) {
        String ampm = (hour < 12) ? "오전" : "오후";
        int display = hour % 12;
        if (display == 0) display = 12;
        return ampm + " " + display + "시";
    }

    // 팝업 공통
    private JPanel createPopupPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 250, 205));
        panel.setBorder(new RoundedBorder(20, BROWN, 2));
        return panel;
    }

    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(15f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ✅ 간단 팝업 (텍스트 세로로 안 깨지게 수정)
    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
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
                g2.setColor(new Color(139, 90, 43));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        // 텍스트 영역 폭을 넉넉히 지정
        JTextPane msgPane = new JTextPane();
        msgPane.setText(message);
        msgPane.setFont(uiFont.deriveFont(18f));
        msgPane.setForeground(new Color(139, 90, 43));
        msgPane.setOpaque(false);
        msgPane.setEditable(false);
        msgPane.setFocusable(false);
        msgPane.setBounds(30, 40, 340, 100);   // 🔹 폭을 340으로 고정

        StyledDocument doc = msgPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        panel.add(msgPane);

        JButton okBtn = new JButton("확인");
        okBtn.setFont(uiFont.deriveFont(16f));
        okBtn.setBackground(new Color(139, 90, 43));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBounds(130, 160, 140, 45);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.setBorder(BorderFactory.createLineBorder(new Color(139, 90, 43), 2));
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    // 로그아웃 팝업
    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
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
                g2.setColor(new Color(139, 90, 43));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(new Color(139, 90, 43));
        msgLabel.setBounds(20, 50, 360, 50);
        panel.add(msgLabel);

        JButton yesBtn = new JButton("네");
        yesBtn.setFont(uiFont.deriveFont(16f));
        yesBtn.setBackground(new Color(139, 90, 43));
        yesBtn.setForeground(Color.WHITE);
        yesBtn.setFocusPainted(false);
        yesBtn.setBounds(70, 140, 110, 45);
        yesBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yesBtn.setBorder(BorderFactory.createLineBorder(new Color(139, 90, 43), 2));
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            UserManager.logout();
            new LoginFrame();
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBackground(new Color(139, 90, 43));
        noBtn.setForeground(Color.WHITE);
        noBtn.setFocusPainted(false);
        noBtn.setBounds(210, 140, 110, 45);
        noBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noBtn.setBorder(BorderFactory.createLineBorder(new Color(139, 90, 43), 2));
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(isActive ? HIGHLIGHT_YELLOW : NAV_BG);
        btn.setForeground(BROWN);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        if (!isActive) {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("마이페이지")) { new MyPageFrame(); dispose(); }
                    else if (text.equals("공간대여")) return;
                    else if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                    else if (text.equals("간식행사") || text.equals("과행사")) { new EventListFrame(); dispose(); }
                    else if (text.equals("커뮤니티")) { new CommunityFrame(); dispose(); }
                    else if (text.equals("빈 강의실")) { new EmptyClassFrame(); dispose(); }
                    else if (text.equals("서울여대 꿀단지")) { new MainFrame(); dispose(); }
                    else { showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다."); }
                }
            });
        }
        return btn;
    }

    class SpaceListRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null && (value.toString().startsWith("--") || value.toString().startsWith("==="))) {
                setBackground(Color.LIGHT_GRAY);
            }
            return this;
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
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceRentFrame::new);
    }
}
