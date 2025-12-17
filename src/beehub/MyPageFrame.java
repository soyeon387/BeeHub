package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

//기존 import 아래에 추가
import beehub.CommunityFrame;       // 게시글 객체(Post) 사용을 위해
import beehub.CommunityDetailFrame; // 상세 화면 띄우기 위해

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import beehub.DBUtil;
import java.time.LocalTime;
import java.time.LocalDateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;  // 사용 안 해도 에러는 아님

// 커뮤니티 관련
import beehub.CommunityDAO;

// 로그인 정보
import beehub.LoginSession;
import beehub.Member;

// 관리자 데이터 매니저 임포트
import admin.LotteryManager;
import admin.LotteryManager.LotteryRound;
import admin.LotteryManager.Applicant;

// 공간 대여 DAO
import beehub.SpaceReservationDAO;
import beehub.SpaceReservationDAO.ReservationSummary;

public class MyPageFrame extends JFrame {

    // 🎨 컬러 테마
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    private static final Color LINK_COLOR = new Color(0, 102, 204);
    private static final Color OVERDUE_RED = new Color(200, 50, 50);
    private static final Color CANCEL_RED = new Color(200, 50, 50);
    private static final Color WINNER_GREEN = new Color(0, 150, 0);

    // 커뮤니티 DB 접근용 DAO
    private CommunityDAO communityDAO = new CommunityDAO();
    private ImageIcon heartIcon;
    private ImageIcon queenIcon;

    private static Font uiFont;

    static {
        try {
            InputStream is = MyPageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                File f = new File("resource/fonts/DNFBitBitv2.ttf");
                if (f.exists()) {
                    uiFont = Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(14f);
                } else {
                    uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
                }
            } else {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, is);
                uiFont = uiFont.deriveFont(14f);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    // ==========================================================
    // 📊 데이터 구조
    // ==========================================================

    public static class MyPagePost {
        int no;
        String title;
        String writer;
        String date;
        int likes;
        int comments;
        String content;

        public MyPagePost(int n, String t, String w, String d, int l, int c, String content) {
            this.no = n;
            this.title = t;
            this.writer = w;
            this.date = d;
            this.likes = l;
            this.comments = c;
            this.content = content;
        }
    }

    public static class RentalItem {
        String itemName;
        String returnDate;
        boolean isReturned;

        public RentalItem(String name, String date, boolean returned) {
            this.itemName = name;
            this.returnDate = date;
            this.isReturned = returned;
        }
    }

    public enum ReservationStatus {
        CANCELLABLE, COMPLETED, USER_CANCELLED, AUTO_CANCELLED
    }

    // 🔹 공간 대여 1건 정보 (마이페이지용)
    public static class SpaceRentalItem {
        public int reservationId;    // 예약 PK
        String roomName;            // 방 이름
        String reservationDate;     // yyyy-MM-dd
        String startTime;           // HH:mm
        String endTime;             // HH:mm
        int headcount;              // 인원
        ReservationStatus status;   // 상태

        public SpaceRentalItem(int reservationId,
                               String name,
                               String date,
                               String startTime,
                               String endTime,
                               int count,
                               ReservationStatus status) {
            this.reservationId = reservationId;
            this.roomName = name;
            this.reservationDate = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.headcount = count;
            this.status = status;
        }
    }

    public static class EventParticipationItem {
        String eventTitle;
        String eventDate;
        String eventTime;
        boolean requiresSecretCode;
        ReservationStatus status;

        public EventParticipationItem(String title, String date, String time, boolean requiresCode, ReservationStatus status) {
            this.eventTitle = title;
            this.eventDate = date;
            this.eventTime = time;
            this.requiresSecretCode = requiresCode;
            this.status = status;
        }
    }

    // [수정] 사용자 응모 기록 (내 응모함용)
    public static class UserApplication {
        LotteryRound round;
        String applicationDate;
        int entryCount; // 응모 횟수

        public UserApplication(LotteryRound round, String appDate, int count) {
            this.round = round;
            this.applicationDate = appDate;
            this.entryCount = count;
        }
    }

    // 로그인 사용자 정보
    private Member currentUser = LoginSession.getUser();

    private String userName = (currentUser != null) ? currentUser.getName() : "게스트";
    private String userDept = (currentUser != null) ? currentUser.getMajor() : "학과 정보 없음";
    private String userId = (currentUser != null) ? currentUser.getHakbun() : "";
    private String userNickname = (currentUser != null && currentUser.getNickname() != null && !currentUser.getNickname().isEmpty())
            ? currentUser.getNickname()
            : "닉네임 없음";
    private String userPassword = (currentUser != null) ? currentUser.getPw() : "";
    private int userPoint = (currentUser != null) ? currentUser.getPoint() : 0;

    // UI 컴포넌트
    private JList<String> menuList;
    private CardLayout cardLayout;
    private JPanel detailPanel;
    private JLabel nicknameLabel;
    private ImageIcon beeIcon;

    // 내 응모 기록 리스트
    private List<UserApplication> myApplications;

    private final int FRAME_WIDTH = 800;
    private final int FRAME_HEIGHT = 680;
    private final int CONTENT_Y = 130;
    private final int CONTENT_HEIGHT = FRAME_HEIGHT - CONTENT_Y - 30;
    private final int MENU_WIDTH = 170;
    private final int DETAIL_X = 20 + MENU_WIDTH + 10;
    private final int DETAIL_WIDTH = FRAME_WIDTH - DETAIL_X - 20;
    
    // ✅ 실제 DB에서 불러온 공간 대여 기록 리스트
    private List<SpaceRentalItem> spaceRentalItems = new ArrayList<>();

    public MyPageFrame() {
        setTitle("서울여대 꿀단지 - 마이페이지");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        loadImages();
        //initDummyData();
        initHeader();
        initNav();
        initContent();

        setVisible(true);
    }

    private void loadImages() {
        try {
            // 1. 꿀벌 아이콘
            ImageIcon originalBeeIcon = new ImageIcon("resource/img/login-bee.png");
            if (originalBeeIcon.getIconWidth() > 0) {
                Image img = originalBeeIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                beeIcon = new ImageIcon(img);
            }
            
            // 2. 하트 아이콘
            File heartFile = new File("resource/img/heart.png");
            if (heartFile.exists()) {
                ImageIcon origin = new ImageIcon(heartFile.getAbsolutePath());
                if (origin.getIconWidth() > 0) {
                    Image img = origin.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                    heartIcon = new ImageIcon(img);
                }
            } else {
                // (백업 경로 시도 생략 가능)
                java.net.URL heartUrl = getClass().getResource("/img/heart.png");
                if (heartUrl != null) {
                    ImageIcon origin = new ImageIcon(heartUrl);
                    if (origin.getIconWidth() > 0) {
                        Image img = origin.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                        heartIcon = new ImageIcon(img);
                    }
                }
            }

            // 3. 👑 [추가] 여왕벌 아이콘 로드
            File queenFile = new File("resource/img/queen.png");
            if (queenFile.exists()) {
                ImageIcon origin = new ImageIcon(queenFile.getAbsolutePath());
                if (origin.getIconWidth() > 0) {
                    // 여왕벌은 조금 더 크게 (22x22)
                    Image img = origin.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                    queenIcon = new ImageIcon(img);
                }
            } else {
                java.net.URL queenUrl = getClass().getResource("/img/queen.png");
                if (queenUrl != null) {
                    ImageIcon origin = new ImageIcon(queenUrl);
                    if (origin.getIconWidth() > 0) {
                        Image img = origin.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                        queenIcon = new ImageIcon(img);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 더미 데이터 생성
    /*private void initDummyData() {
        LocalDate today = LocalDate.of(2025, 12, 1);

        dummyPosts = new ArrayList<>();
        dummyPosts.add(new MyPagePost(1, "커뮤니티 기능 완성! (내 글)", userNickname, today.toString(), 15, 5, "완성해서 너무 기뻐요!"));
        dummyPosts.add(new MyPagePost(2, "Spring 강의 자료 요청해요", userNickname, today.minusDays(2).toString(), 8, 3, "혹시 자료 공유 가능하신 분?"));
        dummyPosts.add(new MyPagePost(3, "점심 메뉴 추천 받습니다", "다른학생1", today.minusDays(5).toString(), 20, 10, "오늘 뭐 먹지..."));
        dummyPosts.add(new MyPagePost(4, "시험 기간 힘내세요!", "다른학생2", today.minusDays(10).toString(), 50, 2, "모두 A+ 받기를 기원합니다."));

        dummyRentals = new ArrayList<>();
        dummyRentals.add(new RentalItem("노트북 3", "2025-12-04", false));
        dummyRentals.add(new RentalItem("보조배터리 5", "2025-11-28", false));
        dummyRentals.add(new RentalItem("빔 프로젝터", "2025-12-10", false));
        dummyRentals.add(new RentalItem("무선 마우스", "2025-11-20", true));
        dummyRentals.add(new RentalItem("삼각대", "2025-10-01", true));

        dummySpaceRentals = new ArrayList<>();
        dummySpaceRentals.add(new SpaceRentalItem(1, "세미나실 1", "2025-12-05", "14:00", "16:00", 8, ReservationStatus.CANCELLABLE));
        dummySpaceRentals.add(new SpaceRentalItem(2, "실습실 F", "2025-11-25", "18:00", "20:00", 12, ReservationStatus.COMPLETED));

        dummyEvents = new ArrayList<>();
        dummyEvents.add(new EventParticipationItem("SW 멘토링 특강", "2025-12-10", "15:00", false, ReservationStatus.CANCELLABLE));
        dummyEvents.add(new EventParticipationItem("개강총회", "2025-09-01", "18:00", false, ReservationStatus.COMPLETED));
        dummyEvents.add(new EventParticipationItem("총학생회 간식 배부", "2025-12-05", "12:00", true, ReservationStatus.COMPLETED));
        dummyEvents.add(new EventParticipationItem("캡스톤 디자인 발표회", "2025-12-20", "13:00", false, ReservationStatus.USER_CANCELLED));
    }*/

    private String getRank(int point) {
        if (point >= 200) return "여왕벌";
        if (point >= 100) return "꿀벌";
        return "일벌";
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, FRAME_WIDTH, 80);
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

        JLabel jarIcon = new JLabel("");
        jarIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        jarIcon.setBounds(310, 25, 40, 40);
        headerPanel.add(jarIcon);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel logoutText = new JLabel("| 로그아웃");
        logoutText.setFont(uiFont.deriveFont(14f));
        logoutText.setForeground(BROWN);
        logoutText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showLogoutPopup();
            }
        });
        userInfoPanel.add(logoutText);
        headerPanel.add(userInfoPanel);
    }

    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, FRAME_WIDTH, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("마이페이지"));
            navPanel.add(menuBtn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, CONTENT_Y, FRAME_WIDTH, CONTENT_HEIGHT);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(20, 20, MENU_WIDTH, CONTENT_HEIGHT - 40);
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new RoundedBorder(20, BORDER_COLOR, 1));
        contentPanel.add(leftPanel);

        String[] menuItems = {
                "나의 활동", "회원 정보", "작성 게시글", "댓글 단 게시글", "좋아요 누른 게시글",
                "이용 기록", "물품 대여 기록", "공간 대여 기록",
                "나의 행사 목록",
                "--- 분리선 ---",
                "응모함"
        };

        menuList = new JList<>(menuItems);
        menuList.setFont(uiFont.deriveFont(16f));
        menuList.setForeground(BROWN);
        menuList.setSelectionBackground(HIGHLIGHT_YELLOW);
        menuList.setSelectionForeground(BROWN);
        menuList.setCellRenderer(new MyPageListRenderer());

        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setBounds(10, 10, MENU_WIDTH - 20, CONTENT_HEIGHT - 60);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getViewport().setBackground(Color.WHITE);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        menuScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(menuScroll);

        cardLayout = new CardLayout();
        detailPanel = new JPanel(cardLayout);
        detailPanel.setBounds(DETAIL_X, 20, DETAIL_WIDTH, CONTENT_HEIGHT - 40);
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(new RoundedBorder(20, BORDER_COLOR, 1));
        contentPanel.add(detailPanel);

        addDetailCards();

        menuList.setSelectedIndex(1);
        cardLayout.show(detailPanel, "회원 정보");

        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItem = menuList.getSelectedValue();
                if (selectedItem != null) {
                    if (!selectedItem.equals("나의 활동") &&
                        !selectedItem.equals("이용 기록") &&
                        !selectedItem.equals("--- 분리선 ---")) {
                        cardLayout.show(detailPanel, selectedItem);
                    }
                }
            }
        });
    }

    private void addDetailCards() {
        detailPanel.add(createUserInfoPanel(), "회원 정보");
        detailPanel.add(createActivityListPanel("작성 게시글"), "작성 게시글");
        detailPanel.add(createActivityListPanel("댓글 단 게시글"), "댓글 단 게시글");
        detailPanel.add(createActivityListPanel("좋아요 누른 게시글"), "좋아요 누른 게시글");
        detailPanel.add(createRentalListPanel(), "물품 대여 기록");
        detailPanel.add(createSpaceRentalListPanel(), "공간 대여 기록");
        detailPanel.add(createEventParticipationPanel(), "나의 행사 목록");
        detailPanel.add(createApplicationPanel(), "응모함");
    }

    // 🔹 DB에서 로그인 사용자의 응모 내역 불러오기
    private void loadMyApplicationsFromDB() {
        myApplications = new ArrayList<>();

        Member user = LoginSession.getUser();
        if (user == null) return;

        String hakbun = user.getHakbun();

        String sql =
                "SELECT e.round_id, r.round_name, " +
                        "       DATE(MIN(e.created_at)) AS first_date, " +
                        "       SUM(e.entry_count) AS total_count " +
                        "FROM lottery_entry e " +
                        "JOIN lottery_round r ON e.round_id = r.round_id " +
                        "WHERE e.hakbun = ? " +
                        "GROUP BY e.round_id, r.round_name " +
                        "ORDER BY e.round_id ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int roundId = rs.getInt("round_id");
                    String rawName = rs.getString("round_name");

                    String roundName = rawName;
                    if (rawName != null) {
                        int idx = rawName.indexOf(":");
                        if (idx > 0 && rawName.substring(0, idx).contains("회차")) {
                            roundName = rawName.substring(idx + 1).trim();
                        }
                    }

                    java.sql.Date firstDate = rs.getDate("first_date");
                    String dateStr = (firstDate != null) ? firstDate.toString() : "-";

                    int totalCount = rs.getInt("total_count");

                    LotteryRound round = new LotteryRound();
                    round.roundId = roundId;
                    round.name = roundName;

                    myApplications.add(new UserApplication(round, dateStr, totalCount));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showCustomAlertPopup("오류",
                    "응모 내역을 불러오는 중 오류가 발생했습니다.\n" + ex.getMessage());
        }
    }

    // ===================== 응모함 패널 =====================
    private JPanel createApplicationPanel() {
        JPanel panel = new JPanel(null);
        panel.setName("응모함");
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("꿀단지 응모함", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 10, 400, 30);
        panel.add(titleLabel);

        int y = 50;

        JPanel pointPanel = new JPanel(null);
        pointPanel.setOpaque(false);
        pointPanel.setBounds(20, y, DETAIL_WIDTH - 40, 45);

        JLabel pointTitle = createLabel("나의 보유 꿀:");
        pointTitle.setBounds(0, 0, 120, 25);
        pointPanel.add(pointTitle);

        int currentPoint = (LoginSession.getUser() != null)
                ? LoginSession.getUser().getPoint()
                : userPoint;
        JLabel pointValueLabel = createLabel(currentPoint + "꿀");
        pointValueLabel.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        pointValueLabel.setBounds(120, 0, 120, 25);
        pointPanel.add(pointValueLabel);

        JLabel guideLabel = createLabel("※ 100꿀 → 1회 응모");
        guideLabel.setFont(uiFont.deriveFont(14f));
        guideLabel.setBounds(0, 25, 250, 20);
        pointPanel.add(guideLabel);

        panel.add(pointPanel);
        y += 60;

        JButton applyBtn = createStyledButton("응모하기", 140, 45);
        applyBtn.setBounds(DETAIL_WIDTH - 340, y, 140, 45);
        applyBtn.addActionListener(e -> showApplyPopup(pointValueLabel));
        panel.add(applyBtn);

        JButton checkBtn = createStyledButton("당첨 확인", 140, 45);
        checkBtn.setBounds(DETAIL_WIDTH - 180, y, 140, 45);
        checkBtn.addActionListener(e -> showCheckWinningPopup());
        panel.add(checkBtn);

        y += 70;

        JLabel subTitle = new JLabel("나의 응모 내역", SwingConstants.LEFT);
        subTitle.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        subTitle.setForeground(BROWN);
        subTitle.setBounds(20, y, 400, 30);
        panel.add(subTitle);
        y += 40;

        String[] headers = {"회차/이름", "응모일", "응모 횟수"};
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        styleTable(table);
        table.setRowHeight(32);

        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, y, DETAIL_WIDTH - 40, CONTENT_HEIGHT - y - 40);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scroll);

        loadMyApplicationsFromDB();

        if (myApplications != null) {
            for (int i = 0; i < myApplications.size(); i++) {
                UserApplication ua = myApplications.get(i);
                String titleText = ua.round.roundId + "회차. " + ua.round.name;

                model.addRow(new Object[]{
                        titleText,
                        ua.applicationDate,
                        ua.entryCount
                });
            }
        }

        return panel;
    }

    // ===================== 응모 팝업 =====================
    private void showApplyPopup(JLabel currentPointLabel) {
        int costPoints = 100;

        Member user = LoginSession.getUser();
        if (user == null) {
            showCustomAlertPopup("알림", "로그인이 필요합니다.");
            return;
        }

        String hakbun = user.getHakbun();

        String role = user.getRole();
        if (!"USER".equalsIgnoreCase(role)) {
            showCustomAlertPopup("알림", "일반 학생만 응모할 수 있습니다.\n(학생회/관리자는 응모 불가)");
            return;
        }

        int currentPoint = user.getPoint();
        if (currentPoint < costPoints) {
            showCustomAlertPopup("알림", "보유 꿀이 부족합니다.\n응모는 " + costPoints + "꿀 이상부터 가능합니다.");
            return;
        }

        List<LotteryManager.LotteryRound> allRounds = LotteryManager.getAllRounds();

        List<LotteryManager.LotteryRound> activeRounds = new ArrayList<>();

        DateTimeFormatter periodFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        for (LotteryManager.LotteryRound r : allRounds) {
            if (r.isDrawn) continue;

            if (r.applicationPeriod != null && r.applicationPeriod.contains("~")) {
                try {
                    String[] periodParts = r.applicationPeriod.split("~");

                    LocalDateTime startDateTime = LocalDateTime.parse(periodParts[0].trim(), periodFormatter);
                    LocalDateTime endDateTime = LocalDateTime.parse(periodParts[1].trim(), periodFormatter);

                    if (!now.isBefore(startDateTime) && !now.isAfter(endDateTime)) {
                        activeRounds.add(r);
                    }
                } catch (Exception ex) {
                    System.err.println("경품 추첨 기간 파싱 오류: " + r.applicationPeriod + " - " + ex.getMessage());
                    continue;
                }
            }
        }

        if (activeRounds.isEmpty()) {
            showCustomAlertPopup("알림", "현재 진행중인 추첨이 없습니다.");
            return;
        }

        String[] options = new String[activeRounds.size()];
        for (int i = 0; i < activeRounds.size(); i++) {
            LotteryManager.LotteryRound r = activeRounds.get(i);
            options[i] = (i + 1) + ". " + r.name + " (" + r.prizeName + ")";
        }

        JDialog dialog = new JDialog(this, "경품 추첨 응모", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        int y = 30;

        JLabel title = new JLabel("경품 추첨 응모", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        title.setForeground(BROWN);
        title.setBounds(10, y, 430, 30);
        panel.add(title);
        y += 50;

        JTextPane pointInfoLabel = createCenteredTextPane("나의 보유 꿀: " + currentPoint + "꿀\n(1회 응모 시 " + costPoints + "꿀 차감)");
        pointInfoLabel.setBounds(20, y, 410, 50);
        pointInfoLabel.setFont(uiFont.deriveFont(16f));
        panel.add(pointInfoLabel);
        y += 80;

        JLabel roundSelectLabel = createLabel("응모할 회차 선택:");
        roundSelectLabel.setBounds(30, y, 150, 30);
        roundSelectLabel.setFont(uiFont.deriveFont(18f));
        panel.add(roundSelectLabel);

        JComboBox<String> roundCombo = new JComboBox<>(options);
        roundCombo.setFont(uiFont.deriveFont(16f));
        roundCombo.setBounds(180, y, 240, 35);
        panel.add(roundCombo);
        y += 70;

        JButton applyButton = createPopupBtn("응모하기");
        applyButton.setBounds(90, y + 80, 120, 45);
        applyButton.addActionListener(e -> {
            int selectedIndex = roundCombo.getSelectedIndex();
            if (selectedIndex < 0) {
                showCustomAlertPopup("알림", "응모할 회차를 선택해주세요.");
                return;
            }

            dialog.dispose();

            LotteryManager.LotteryRound chosen = activeRounds.get(selectedIndex);
            int roundId = chosen.roundId;
            String selectedOptionText = options[selectedIndex];

            String confirmMsg = selectedOptionText + "\n\n" + costPoints + "꿀을 사용하여 1회 응모하시겠습니까?";

            showCustomConfirmPopup(confirmMsg, () -> {
                boolean success = LotteryManager.applyUsingPoints(roundId, hakbun);
                if (success) {
                    int newPoint = currentPoint - costPoints;
                    user.setPoint(newPoint);
                    currentPointLabel.setText(newPoint + "꿀");

                    showCustomAlertPopup("성공", "응모가 완료되었습니다!\n(현재 보유 꿀: " + newPoint + "꿀)");

                    if (myApplications == null) {
                        myApplications = new ArrayList<>();
                    }
                    LocalDate appDate = LocalDate.now();

                    UserApplication existingApp = myApplications.stream()
                            .filter(app -> app.round.roundId == roundId)
                            .findFirst()
                            .orElse(null);

                    if (existingApp != null) {
                        existingApp.entryCount++;
                    } else {
                        myApplications.add(new UserApplication(chosen, appDate.toString(), 1));
                    }

                    refreshApplicationPanel();

                } else {
                    showCustomAlertPopup("실패", "응모에 실패했습니다.\n포인트가 부족하거나,\n서버 오류가 발생했습니다.");
                }
            });
        });
        panel.add(applyButton);

        JButton cancelButton = createPopupBtn("취소");
        cancelButton.setBounds(240, y + 80, 120, 45);
        cancelButton.addActionListener(e -> dialog.dispose());
        panel.add(cancelButton);

        dialog.setVisible(true);
    }

    // ===================== 당첨 확인 팝업 =====================
    private void showCheckWinningPopup() {
        JDialog dialog = new JDialog(this, "당첨 확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        int y = 30;
        JLabel title = new JLabel("경품 응모 당첨 확인", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        title.setForeground(BROWN);
        title.setBounds(10, y, 430, 30);
        panel.add(title);
        y += 50;

        JLabel roundSelectLabel = createLabel("회차 선택:");
        roundSelectLabel.setBounds(30, y, 100, 30);
        panel.add(roundSelectLabel);

        List<LotteryRound> allRounds = LotteryManager.getAllRounds();
        String[] roundTitles = allRounds.stream()
                .map(r -> r.name + ": " + r.prizeName)
                .toArray(String[]::new);

        JComboBox<String> roundCombo = new JComboBox<>(roundTitles);
        roundCombo.setFont(uiFont.deriveFont(16f));
        roundCombo.setBounds(140, y, 280, 30);
        panel.add(roundCombo);
        y += 50;

        JTextPane resultPane = new JTextPane();
        resultPane.setText("확인 버튼을 눌러주세요.");
        resultPane.setFont(uiFont.deriveFont(18f));
        resultPane.setForeground(BROWN);
        resultPane.setEditable(false);
        resultPane.setOpaque(false);

        StyledDocument doc = resultPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        resultPane.setBounds(30, y, 390, 200);
        panel.add(resultPane);
        y += 140;

        JButton confirmBtn = createPopupBtn("확인");
        confirmBtn.setBounds(100, y + 60, 110, 45);
        confirmBtn.addActionListener(e -> {
            int idx = roundCombo.getSelectedIndex();
            if (idx < 0) return;
            LotteryRound r = allRounds.get(idx);

            Applicant myRecord = null;
            for (Applicant app : r.applicants) {
                if (app.hakbun.equals(userId)) {
                    myRecord = app;
                    break;
                }
            }

            String resultText;
            Color color;

            LocalDate today = LocalDate.now();
            LocalDate annDate = null;
            try {
                if (r.announcementDate != null && !r.announcementDate.isEmpty()) {
                    annDate = LocalDate.parse(r.announcementDate);
                }
            } catch (Exception ignore) {
            }

            if (myRecord == null) {
                resultText = "응모 기록이 없습니다.";
                color = BROWN;
            } else if (!r.isDrawn) {
                resultText = "아직 추첨이 진행되지 않았습니다.\n"
                        + "(발표일: " + r.announcementDate + ")";
                color = BROWN;
            } else if (annDate != null && annDate.isAfter(today)) {
                resultText = "아직 발표일이 아닙니다.\n(" + r.announcementDate + " 발표)";
                color = BROWN;
            } else if ("당첨".equals(myRecord.status)) {
                resultText = "🎉 당첨 🎉\n"
                        + "\n수령 장소: " + r.pickupLocation + "\n\n"
                        + "수령 기간: " + r.pickupPeriod;
                color = WINNER_GREEN;
            } else {
                resultText = "아쉽게도 미당첨되었습니다.";
                color = OVERDUE_RED;
            }

            resultPane.setText(resultText);
            resultPane.setForeground(color);

            doc.setParagraphAttributes(0, doc.getLength(), center, false);
        });
        panel.add(confirmBtn);

        JButton closeBtn = createPopupBtn("닫기");
        closeBtn.setBounds(230, y + 60, 110, 45);
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn);

        dialog.setVisible(true);
    }

    // ✅ 응모 패널 새로고침 (항상 응모함 탭 유지)
    private void refreshApplicationPanel() {
        loadMyApplicationsFromDB();

        Component[] components = detailPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && "응모함".equals(comp.getName())) {
                detailPanel.remove(comp);
                break;
            }
        }

        JPanel newAppPanel = createApplicationPanel();
        detailPanel.add(newAppPanel, "응모함");

        menuList.setSelectedValue("응모함", true);
        cardLayout.show(detailPanel, "응모함");

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // ===================== 렌더러 =====================

    class CenterRenderer extends DefaultTableCellRenderer {
        public CenterRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(uiFont.deriveFont(16f));
            return c;
        }
    }

    private class SpaceDateTimeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.CENTER);

            if (value instanceof SpaceRentalItem) {
                SpaceRentalItem item = (SpaceRentalItem) value;

                StringBuilder sb = new StringBuilder();

                if (item.reservationDate != null && !item.reservationDate.isEmpty()) {
                    sb.append(item.reservationDate);
                }

                if (item.startTime != null && !item.startTime.isEmpty()
                        && item.endTime != null && !item.endTime.isEmpty()) {
                    sb.append("  ")
                            .append(item.startTime)
                            .append(" ~ ")
                            .append(item.endTime);
                }

                setText(sb.toString());
            }

            return this;
        }
    }

    class EventScheduleRenderer extends DefaultTableCellRenderer {
        public EventScheduleRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            if (value instanceof EventParticipationItem) {
                EventParticipationItem item = (EventParticipationItem) value;
                label.setText(item.eventDate + " (" + item.eventTime + ")");
            }
            label.setFont(uiFont.deriveFont(16f));
            return label;
        }
    }

    private class SpaceActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof ReservationStatus) {
                ReservationStatus st = (ReservationStatus) value;

                setHorizontalAlignment(SwingConstants.CENTER);

                switch (st) {
                    case CANCELLABLE:
                        setText("취소");
                        setForeground(new Color(0, 120, 0));
                        break;
                    case USER_CANCELLED:
                        setText("취소완료");
                        setForeground(new Color(150, 150, 150));
                        break;
                    case AUTO_CANCELLED:
                        setText("미입실");
                        setForeground(new Color(220, 50, 50));
                        break;
                    case COMPLETED:
                    default:
                        setText("이용완료");
                        setForeground(new Color(80, 80, 80));
                        break;
                }
            }

            return this;
        }
    }

    class EventActionRenderer extends DefaultTableCellRenderer {
        public EventActionRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setFont(uiFont.deriveFont(16f));
            label.setForeground(BROWN);
            if (isSelected) label.setBackground(HIGHLIGHT_YELLOW);
            else label.setBackground(Color.WHITE);
            ReservationStatus status = (ReservationStatus) value;
            switch (status) {
                case CANCELLABLE:
                    label.setText("<html><u>참여 취소</u></html>");
                    label.setForeground(CANCEL_RED);
                    break;
                case COMPLETED:
                    label.setText("완료");
                    break;
                case USER_CANCELLED:
                    label.setText("취소 완료");
                    break;
                default:
                    label.setText("");
                    break;
            }
            return label;
        }
    }

    class RentalStatusRenderer extends DefaultTableCellRenderer {
        public RentalStatusRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setFont(uiFont.deriveFont(16f));
            if (isSelected) label.setBackground(HIGHLIGHT_YELLOW);
            else label.setBackground(Color.WHITE);
            String statusText = value.toString();
            if (statusText.equals("반납 완료")) {
                label.setText(statusText);
                label.setForeground(BROWN);
            } else {
                String dDayStatus = formatDDay(statusText);
                label.setText(statusText + " (" + dDayStatus + ")");
                if (dDayStatus.startsWith("D+")) {
                    label.setForeground(OVERDUE_RED);
                    label.setFont(uiFont.deriveFont(Font.BOLD, 16f));
                } else {
                    label.setForeground(BROWN);
                }
            }
            return label;
        }
    }

    private String formatDDay(String dateStr) {
        try {
            LocalDate today = LocalDate.of(2025, 12, 1);
            LocalDate returnDate = LocalDate.parse(dateStr);
            long daysDiff = ChronoUnit.DAYS.between(today, returnDate);
            if (daysDiff == 0) return "D-DAY";
            else if (daysDiff > 0) return "D-" + daysDiff;
            else return "D+" + Math.abs(daysDiff);
        } catch (Exception e) {
            return "날짜 오류";
        }
    }

    // ===================== 회원 정보 패널 =====================

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("회원 정보", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 10, 200, 30);
        panel.add(titleLabel);

        JSeparator separator = new JSeparator();
        separator.setBounds(20, 45, 520, 1);
        panel.add(separator);

        int y = 70;
        y = addInfoRow(panel, y, "이름", userName, 400, false, null);
        y = addInfoRow(panel, y, "학과/학번", userDept + " / " + userId, 380, false, null);
        y = addInfoRow(panel, y, "닉네임", userNickname, 250, true, new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showNicknameEditPopup();
            }
        });
        y += 20;
        y = addInfoRow(panel, y, "보유 꿀", userPoint + "꿀", 400, false, null);

        JLabel rankTitleLabel = createLabel("등급");
        rankTitleLabel.setFont(uiFont.deriveFont(16f));
        rankTitleLabel.setBounds(20, y, 100, 30);
        panel.add(rankTitleLabel);

        JLabel rankValueLabel = createLabel("");
        String rank = getRank(userPoint);
        rankValueLabel.setText(rank + " (" + userPoint + "/200)");
        if (rank.startsWith("여왕벌") && queenIcon != null) {
            rankValueLabel.setIcon(queenIcon);
            rankValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            rankValueLabel.setIconTextGap(5);
        } else if (rank.startsWith("꿀벌") && beeIcon != null) {
            rankValueLabel.setIcon(beeIcon);
            rankValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            rankValueLabel.setIconTextGap(5);
        }
        
        
        rankValueLabel.setBounds(150, y, 400, 30);
        panel.add(rankValueLabel);

        y += 90;

        JButton passwordBtn = createStyledButton("비밀번호 수정", 150, 40);
        passwordBtn.setBounds(20, y, 150, 40);
        passwordBtn.addActionListener(e -> showPasswordChangePopup());
        panel.add(passwordBtn);

        return panel;
    }

    private int addInfoRow(JPanel panel, int y, String title, String value, int valueWidth, boolean isEditable, MouseAdapter adapter) {
        JLabel titleLabel = createLabel(title);
        titleLabel.setFont(uiFont.deriveFont(16f));
        titleLabel.setBounds(20, y, 100, 30);
        panel.add(titleLabel);

        JLabel valueLabel = createLabel(value);
        valueLabel.setFont(uiFont.deriveFont(16f));
        valueLabel.setBounds(150, y, valueWidth, 30);
        panel.add(valueLabel);

        if (title.equals("닉네임")) this.nicknameLabel = valueLabel;

        if (isEditable && adapter != null) {
            JLabel editLink = new JLabel("<html><u>[수정]</u></html>");
            editLink.setFont(uiFont.deriveFont(14f));
            editLink.setForeground(LINK_COLOR);
            editLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editLink.setBounds(150 + valueWidth + 10, y, 50, 30);
            editLink.addMouseListener(adapter);
            panel.add(editLink);
        }
        return y + 40;
    }

    // ===================== 활동 패널 =====================

    
 // ===================== 활동 패널 (수정됨: 더블클릭 이동 기능 추가) =====================

    private JPanel createActivityListPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 1. 컬럼에 '번호'(ID)를 추가합니다. (나중에 숨김)
        String[] headers = {"번호", "제목"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable activityTable = new JTable(tableModel);
        styleTable(activityTable);
        
        // 2. '번호' 컬럼 숨기기 (폭을 0으로 설정)
        activityTable.getColumnModel().getColumn(0).setMinWidth(0);
        activityTable.getColumnModel().getColumn(0).setMaxWidth(0);
        activityTable.getColumnModel().getColumn(0).setWidth(0);
        
        // 제목 컬럼 스타일
        activityTable.getColumnModel().getColumn(1).setPreferredWidth(550);
        activityTable.getColumnModel().getColumn(1).setCellRenderer(new CenterRenderer());

        // 3. 더블 클릭 리스너 추가
        activityTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 더블 클릭
                    int row = activityTable.getSelectedRow();
                    if (row != -1) {
                        // 숨겨진 0번 컬럼에서 글 번호(ID)를 가져옴
                        int postId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                        openPostDetail(postId); // 상세 화면 열기 메서드 호출
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        if (currentUser == null || userId == null || userId.isEmpty()) {
            return panel;
        }

        List<CommunityDAO.PostDTO> postList = new ArrayList<>();
        try {
            if ("작성 게시글".equals(title)) {
                postList = communityDAO.getPostsWrittenByUser(userId);
            } else if ("댓글 단 게시글".equals(title)) {
                postList = communityDAO.getPostsUserCommented(userId);
            } else if ("좋아요 누른 게시글".equals(title)) {
                postList = communityDAO.getPostsUserLiked(userId);
            }

            for (CommunityDAO.PostDTO dto : postList) {
                // dto.no (게시글 번호)를 첫 번째 컬럼에 넣습니다.
                tableModel.addRow(new Object[]{dto.postId, dto.title});
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showCustomAlertPopup("오류", "활동 내역을 불러오는 중 오류가 발생했습니다.\n" + ex.getMessage());
        }

        return panel;
    }
    
 // 🔹 게시글 상세 화면 열기 (Helper 메서드)
    private void openPostDetail(int postId) {
        String sql = "SELECT * FROM community_post WHERE post_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, postId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 1. DB 데이터를 CommunityFrame.Post 객체로 변환
                    CommunityFrame.Post post = new CommunityFrame.Post();
                    post.no = rs.getInt("post_id");
                    post.title = rs.getString("title");
                    post.content = rs.getString("content");
                    post.writer = rs.getString("writer_nickname"); // 닉네임 사용
                    post.likes = rs.getInt("like_count");
                    post.comments = rs.getInt("comment_count");
                    post.date = rs.getTimestamp("created_at").toString();
                    
                    // 2. 상세 화면 띄우기
                    // parentFrame은 null로 넘깁니다. (마이페이지에서 열었으므로 목록 갱신 연동은 안 됨)
                    // 하지만 상세 내용 확인 및 댓글 달기 등은 정상 작동합니다.
                    new CommunityDetailFrame(post, heartIcon, userNickname, null);
                } else {
                    showCustomAlertPopup("알림", "삭제되었거나 존재하지 않는 게시글입니다.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showCustomAlertPopup("오류", "게시글 정보를 불러오지 못했습니다.");
        }
    }

    // ===================== 물품 대여 기록 =====================

    private JPanel createRentalListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("물품 대여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] headers = {"물품 이름", "반납 기한/상태"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable rentalTable = new JTable(tableModel);
        styleTable(rentalTable);

        rentalTable.getColumnModel().getColumn(0).setPreferredWidth(320);
        rentalTable.getColumnModel().getColumn(1).setPreferredWidth(300);

        rentalTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        rentalTable.getColumnModel().getColumn(1).setCellRenderer(new CenterRenderer());
        JScrollPane scrollPane = new JScrollPane(rentalTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        Member current = LoginSession.getUser();
        if (current == null) {
            showCustomAlertPopup("안내", "로그인 정보가 없어 \n대여 기록을 불러올 수 없습니다.");
            return panel;
        }

        String userHakbun = current.getHakbun();

        try {
            RentalDAO rentalDAO = new RentalDAO();
            List<Rental> rentals = rentalDAO.getRentalsByUser(userHakbun);

            for (Rental r : rentals) {
                String status;

                if (r.isReturned()) {
                    status = "반납 완료";
                } else if (r.getDueDate() != null) {
                    LocalDate due = r.getDueDate();
                    LocalDate today = LocalDate.now();

                    long daysDiff = ChronoUnit.DAYS.between(today, due);
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String dateStr = due.format(fmt);

                    if (daysDiff < 0) {
                        status = dateStr + " (D+" + Math.abs(daysDiff) + ")";
                    } else if (daysDiff == 0) {
                        status = dateStr + " (D-Day)";
                    } else {
                        status = dateStr + " (D-" + daysDiff + ")";
                    }

                } else {
                    status = "날짜 없음";
                }

                tableModel.addRow(new Object[]{
                        r.getItemName(),
                        status
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            showCustomAlertPopup("오류", "대여 기록을 불러오는 중 오류가 발생했습니다.\n" + e.getMessage());
        }

        return panel;
    }

    // ===================== 공간 대여 기록 (DB 연동 + 필터) =====================

    private JPanel createSpaceRentalListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("공간 대여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] headers = {"빌린 방", "대여 일자", "상태/취소"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable spaceRentalTable = new JTable(tableModel);
        styleTable(spaceRentalTable);

        spaceRentalTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        spaceRentalTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        spaceRentalTable.getColumnModel().getColumn(2).setPreferredWidth(90);

        spaceRentalTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        spaceRentalTable.getColumnModel().getColumn(1).setCellRenderer(new SpaceDateTimeRenderer());
        spaceRentalTable.getColumnModel().getColumn(2).setCellRenderer(new SpaceActionRenderer());

        JScrollPane scrollPane = new JScrollPane(spaceRentalTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        Member current = LoginSession.getUser();
        if (current == null) {
            showCustomAlertPopup("안내", "로그인 정보가 없어\n 공간 대여 기록을 불러올 수 없습니다.");
            return panel;
        }

        String hakbun = current.getHakbun();
        spaceRentalItems.clear();

        try {
            SpaceReservationDAO dao = new SpaceReservationDAO();
            List<ReservationSummary> list = dao.getReservationsByUser(hakbun);

            LocalDate today = LocalDate.now();

            for (ReservationSummary rs : list) {

                String roomName = (rs.roomName != null) ? rs.roomName.trim() : "";

                String dateStr = (rs.reserveDate != null) ? rs.reserveDate.toString() : "";

                String startTime = "";
                String endTime = "";
                LocalTime startLocal = null;
                LocalTime endLocal = null;

                if (rs.timeSlot != null && rs.timeSlot.contains("~")) {
                    String[] parts = rs.timeSlot.split("~");
                    if (parts.length >= 2) {
                        startTime = parts[0].trim();
                        endTime = parts[1].trim();
                        try {
                            startLocal = LocalTime.parse(startTime);
                            endLocal = LocalTime.parse(endTime);
                        } catch (Exception ignore) {
                        }
                    }
                } else if (rs.timeSlot != null) {
                    String normalized = rs.timeSlot.replace("-", "~");
                    String[] parts = normalized.split("~");
                    if (parts.length >= 2) {
                        startTime = parts[0].trim();
                        endTime = parts[1].trim();
                        try {
                            startLocal = LocalTime.parse(startTime);
                            endLocal = LocalTime.parse(endTime);
                        } catch (Exception ignore) {
                        }
                    }
                }

                ReservationStatus statusEnum;
                String statusStr = (rs.status != null) ? rs.status.toUpperCase() : "";

                LocalDateTime now = LocalDateTime.now();

                switch (statusStr) {

                    case "CANCELED":
                    case "USER_CANCELLED":
                    case "CANCELLED_USER":
                        statusEnum = ReservationStatus.USER_CANCELLED;
                        break;

                    case "NO_SHOW":
                    case "AUTO_CANCELLED":
                    case "CANCELLED_AUTO":
                        statusEnum = ReservationStatus.AUTO_CANCELLED;
                        break;

                    case "COMPLETED":
                        statusEnum = ReservationStatus.COMPLETED;
                        break;

                    case "RESERVED":
                    default:
                        boolean finished = false;

                        if (rs.reserveDate != null) {
                            LocalDate reserveDate = rs.reserveDate;

                            if (reserveDate.isBefore(today)) {
                                finished = true;
                            } else if (reserveDate.isEqual(today) && endLocal != null) {
                                LocalDateTime endDateTime = LocalDateTime.of(reserveDate, endLocal);
                                if (!now.isBefore(endDateTime)) {
                                    finished = true;
                                }
                            }
                        }

                        if (finished) {
                            statusEnum = ReservationStatus.COMPLETED;
                        } else {
                            statusEnum = ReservationStatus.CANCELLABLE;
                        }
                        break;
                }

                int headcount = 0;

                SpaceRentalItem item = new SpaceRentalItem(
                        rs.reservationId,
                        roomName,
                        dateStr,
                        startTime,
                        endTime,
                        headcount,
                        statusEnum
                );

                spaceRentalItems.add(item);

                tableModel.addRow(new Object[]{
                        item.roomName,
                        item,
                        item.status
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            showCustomAlertPopup("오류", "공간 대여 기록을 불러오는 중 오류가 발생했습니다.\n" + e.getMessage());
        }

        headeraceRentalCancelListener(spaceRentalTable, tableModel);

        return panel;
    }

    // ===================== 리스너 =====================

    private void headeraceRentalCancelListener(JTable table, DefaultTableModel tableModel) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (col == 2 && row >= 0 && row < spaceRentalItems.size()) {

                    SpaceRentalItem item = spaceRentalItems.get(row);

                    if (item.status == ReservationStatus.CANCELLABLE) {

                        String confirmMsg = "'" + item.roomName + " (" + item.reservationDate + ")'\n예약을 취소하시겠습니까?";

                        showCustomConfirmPopup(confirmMsg, () -> {
                            try {
                                SpaceReservationDAO dao = new SpaceReservationDAO();
                                String hakbun = LoginSession.getUser().getHakbun();

                                boolean ok = dao.cancelReservation(item.reservationId, hakbun);

                                if (ok) {
                                    item.status = ReservationStatus.USER_CANCELLED;

                                    tableModel.setValueAt(item.status, row, 2);
                                    tableModel.fireTableRowsUpdated(row, row);

                                    showCustomAlertPopup("취소 완료",
                                            item.roomName + " 예약이\n취소 완료되었습니다.");
                                } else {
                                    showCustomAlertPopup("오류",
                                            "예약 취소 처리 중 오류가 발생했습니다.");
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showCustomAlertPopup("오류",
                                        "예약 취소 중 예외가 발생했습니다.\n" + ex.getMessage());
                            }
                        });
                    }
                }
            }
        });
    }

    // ===================== 기타 팝업 & 유틸 =====================

    private void showPasswordChangePopup() {
        JDialog dialog = new JDialog(this, "비밀번호 수정", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        int y = 30;

        JLabel title = new JLabel("비밀번호 수정", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(Font.BOLD, 20f));
        title.setForeground(BROWN);
        title.setBounds(10, y, 480, 30);
        panel.add(title);
        y += 50;

        JPasswordField currentPwdField = createPasswordField(panel, "현재 비밀번호:", y);
        y += 60;
        JPasswordField newPwdField = createPasswordField(panel, "수정할 비밀번호:", y);
        y += 60;
        JPasswordField confirmPwdField = createPasswordField(panel, "비밀번호 확인:", y);
        y += 80;

        JButton saveBtn = createPopupBtn("비밀번호 변경");
        saveBtn.setBounds(100, y, 150, 45);
        saveBtn.addActionListener(e -> {

            String current = new String(currentPwdField.getPassword());
            String newPwd = new String(newPwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());

            if (!current.equals(userPassword)) {
                showCustomAlertPopup("오류", "현재 비밀번호가 일치하지 않습니다.");
                return;
            }

            if (newPwd.isEmpty() || confirmPwd.isEmpty()) {
                showCustomAlertPopup("오류", "새 비밀번호를 모두 입력해주세요.");
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                showCustomAlertPopup("오류", "새 비밀번호와 확인이 일치하지 않습니다.");
                return;
            }

            if (newPwd.length() < 6) {
                showCustomAlertPopup("오류", "비밀번호는 6자 이상이어야 합니다.");
                return;
            }

            UserDAO dao = new UserDAO();
            boolean result = dao.updatePassword(userId, newPwd);

            if (!result) {
                showCustomAlertPopup("오류", "비밀번호 변경 중 문제가 발생했습니다.");
                return;
            }

            userPassword = newPwd;
            if (currentUser != null) currentUser.setPw(newPwd);

            dialog.dispose();
            showCustomAlertPopup("변경 완료", "비밀번호가 성공적으로 변경되었습니다.");
        });

        panel.add(saveBtn);

        JButton cancelBtn = createPopupBtn("취소");
        cancelBtn.setBounds(260, y, 120, 45);
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn);

        dialog.setVisible(true);
    }

    private JPasswordField createPasswordField(JPanel panel, String labelText, int y) {
        JLabel label = new JLabel(labelText, SwingConstants.LEFT);
        label.setFont(uiFont.deriveFont(16f));
        label.setForeground(BROWN);
        label.setBounds(50, y, 150, 30);
        panel.add(label);

        JPasswordField field = new JPasswordField(15);
        field.setEchoChar('*');
        field.setFont(uiFont.deriveFont(16f));
        field.setBounds(200, y, 200, 30);
        panel.add(field);

        return field;
    }

    private void showNicknameEditPopup() {
        JDialog dialog = new JDialog(this, "닉네임 수정", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("새 닉네임을 입력하세요.", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 60);
        panel.add(msgLabel);

        JTextField inputField = new JTextField(userNickname);
        inputField.setFont(uiFont.deriveFont(16f));
        inputField.setBounds(50, 140, 300, 40);
        panel.add(inputField);

        JButton saveBtn = createPopupBtn("저장");
        saveBtn.setBounds(60, 220, 120, 45);
        saveBtn.addActionListener(e -> {
            String newNickname = inputField.getText().trim();
            if (newNickname.isEmpty() || newNickname.length() > 10) {
                JOptionPane.showMessageDialog(dialog, "닉네임은 1자 이상 10자 이내로 입력해주세요.");
                return;
            }

            MemberDAO dao = new MemberDAO();
            boolean ok = dao.updateNickname(userId, newNickname);

            if (!ok) {
                showCustomAlertPopup("오류", "닉네임 변경 중 문제가 발생했습니다.");
                return;
            }

            userNickname = newNickname;
            if (currentUser != null) currentUser.setNickname(newNickname);
            nicknameLabel.setText(userNickname);

            dialog.dispose();
            showCustomAlertPopup("성공", "닉네임이 성공적으로 변경되었습니다.");
        });

        panel.add(saveBtn);

        JButton cancelBtn = createPopupBtn("취소");
        cancelBtn.setBounds(220, 220, 120, 45);
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn);

        dialog.setVisible(true);
    }

    private JPanel createPlaceholderPanel(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>",
                SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(Color.GRAY);
        panel.add(msgLabel, BorderLayout.CENTER);

        return panel;
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 60);
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 180, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            new LoginFrame();
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 180, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    private void showCustomAlertPopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JTextPane msgPane = createCenteredTextPane(message);
        msgPane.setBounds(20, 80, 360, 100);
        panel.add(msgPane);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 220, 130, 45);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private void showSimplePopup(String title, String message) {
        showCustomAlertPopup(title, message);
    }

    private JTextPane createCenteredTextPane(String text) {
        JTextPane textPane = new JTextPane();
        textPane.setText(text);
        textPane.setFont(uiFont.deriveFont(18f));
        textPane.setForeground(BROWN);
        textPane.setOpaque(false);
        textPane.setEditable(false);

        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        return textPane;
    }

    private void showCustomConfirmPopup(String message, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, "확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JTextPane msgPane = createCenteredTextPane(message);
        msgPane.setBounds(20, 80, 360, 100);
        panel.add(msgPane);

        JButton yesBtn = createPopupBtn("확인");
        yesBtn.setBounds(60, 220, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("취소");
        noBtn.setBounds(220, 220, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    private JPanel createPopupPanel() {
        return new JPanel() {
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
    }

    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont.deriveFont(16f));
        label.setForeground(BROWN);
        return label;
    }

    class MyPageListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = (String) value;
            label.setFont(uiFont.deriveFont(16f));
            label.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            if (text.equals("나의 활동") || text.equals("이용 기록")) {
                label.setFont(uiFont.deriveFont(Font.BOLD, 18f));
                label.setBackground(new Color(240, 240, 240));
                label.setForeground(BROWN);
                label.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR));
            } else if (text.equals("--- 분리선 ---")) {
                label.setText("");
                label.setBackground(Color.WHITE);
                label.setBorder(BorderFactory.createMatteBorder(5, 0, 0, 0, BG_MAIN));
                label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                label.setForeground(BROWN);
                if (isSelected) label.setBackground(HIGHLIGHT_YELLOW);
                else label.setBackground(Color.WHITE);
            }
            return label;
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g;
            if (!c.isEnabled()) return;
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

    private void styleTable(JTable table) {
        table.setFont(uiFont.deriveFont(16f));
        table.setRowHeight(30);
        table.setSelectionBackground(HIGHLIGHT_YELLOW);
        table.setSelectionForeground(BROWN);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(uiFont.deriveFont(18f));
        header.setBackground(HEADER_YELLOW);
        header.setForeground(BROWN);
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BROWN));

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
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
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(HIGHLIGHT_YELLOW);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(NAV_BG);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("마이페이지")) return;

                    if (text.equals("과행사")) {
                        new EventListFrame();
                        dispose();
                    } else if (text.equals("물품대여")) {
                        new ItemListFrame();
                        dispose();
                    } else if (text.equals("공간대여")) {
                        new SpaceRentFrame();
                        dispose();
                    } else if (text.equals("빈 강의실")) {
                        new EmptyClassFrame();
                        dispose();
                    } else if (text.equals("커뮤니티")) {
                        new CommunityFrame();
                        dispose();
                    } else {
                        showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다.");
                    }
                }
            });
        }
        return btn;
    }

    // ===================== 행사 참여 기록 패널 =====================
    private JPanel createEventParticipationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("나의 행사 목록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] headers = {"행사명", "일시", "장소"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable eventTable = new JTable(tableModel);
        styleTable(eventTable);

        eventTable.getColumnModel().getColumn(0).setPreferredWidth(230);
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(160);

        eventTable.getColumnModel().getColumn(1).setCellRenderer(new CenterRenderer());
        eventTable.getColumnModel().getColumn(2).setCellRenderer(new CenterRenderer());

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        Member current = LoginSession.getUser();
        if (current == null) {
            return panel;
        }

        try {
            EventDAO eventDAO = new EventDAO();
            List<MyActivityDTO> list = eventDAO.getMyActivityList(current.getHakbun());

            for (MyActivityDTO dto : list) {
                tableModel.addRow(new Object[]{
                        dto.getEventName(),
                        dto.getEventDate(),
                        dto.getLocation()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showCustomAlertPopup("오류", "행사 기록을 불러오는 중 오류가 발생했습니다.\n" + e.getMessage());
        }

        return panel;
    }

    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(int r, Color c, int t) {
            radius = r;
            color = c;
            thickness = t;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyPageFrame::new);
    }
}
