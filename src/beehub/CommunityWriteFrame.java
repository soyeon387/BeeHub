package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;

import beehub.CommunityFrame;
import beehub.CommunityFrame.Post;
import beehub.CommunityDAO;
import beehub.LoginSession;
import beehub.Member;

public class CommunityWriteFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BROWN         = new Color(89, 60, 28);
    private static final Color BG_MAIN       = new Color(255, 255, 255);
    private static final Color BORDER_COLOR  = new Color(220, 220, 220);
    private static final Color POPUP_BG      = new Color(255, 250, 205);

    private static Font uiFont;
    private static final String FONT_NAME_HTML = "던파 비트비트체 v2";

    static {
        try {
            File fontFile = new File("resource/fonts/DNFBitBitv2.ttf");
            if (fontFile.exists()) {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);
            } else {
                InputStream is = CommunityWriteFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
                if (is != null) {
                    uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                } else {
                    uiFont = new Font("SansSerif", Font.PLAIN, 14);
                }
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
            System.err.println("ERROR: Failed to load DNFBitBitv2 font. Using fallback 'SansSerif'.");
            e.printStackTrace();
        }
    }

    private String currentUser;                // 화면에 표시용 이름 (닉네임 또는 실명)
    private CommunityFrame parentFrame;        // 목록 화면
    private CommunityFrame.Post postToEdit;    // null 이면 새 글, 아니면 수정
    private CommunityDetailFrame detailParent; // 디테일 화면에서 수정 눌렀을 때

    private JTextField titleField;
    private JTextArea  contentArea;

    private CommunityDAO communityDAO = new CommunityDAO();

    /**
     * 게시글 작성 프레임 (새 글 작성)
     */
    public CommunityWriteFrame(String user, CommunityFrame parent) {
        this(user, parent, null, null);
    }

    /**
     * 게시글 수정 프레임
     */
    public CommunityWriteFrame(String user,
                               CommunityFrame parent,
                               CommunityFrame.Post postToEdit,
                               CommunityDetailFrame detailParent) {
        this.currentUser   = user;
        this.parentFrame   = parent;
        this.postToEdit    = postToEdit;
        this.detailParent  = detailParent;

        setTitle(postToEdit != null ? "게시글 수정" : "게시글 작성");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        setVisible(true);
    }

    // ----------------------------------
    //  공통: 닉네임 있으면 닉네임, 없으면 실명
    // ----------------------------------
    private String getDisplayName(Member m) {
        if (m == null) return "알 수 없음";
        String nick = m.getNickname();
        if (nick != null && !nick.trim().isEmpty()) {
            return nick.trim();
        }
        return m.getName();
    }

    private void initUI() {
        // 1. 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setBounds(0, 0, 600, 50);
        header.setBackground(HEADER_YELLOW);

        JLabel title = new JLabel(" 커뮤니티 > 게시글 " + (postToEdit != null ? "수정" : "작성"), JLabel.LEFT);
        title.setFont(uiFont.deriveFont(18f));
        title.setForeground(BROWN);
        header.add(title, BorderLayout.WEST);
        add(header);

        // 2. 제목 입력
        JLabel titleLabel = new JLabel("제목 (1자 ~ 20자)");
        titleLabel.setFont(uiFont.deriveFont(16f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 70, 200, 25);
        add(titleLabel);

        titleField = new JTextField();
        titleField.setFont(uiFont.deriveFont(16f));
        titleField.setBounds(20, 100, 545, 40);
        titleField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(titleField);

        // 3. 내용 입력
        JLabel contentLabel = new JLabel("내용 (1자 ~ 500자)");
        contentLabel.setFont(uiFont.deriveFont(16f));
        contentLabel.setForeground(BROWN);
        contentLabel.setBounds(20, 160, 200, 25);
        add(contentLabel);

        contentArea = new JTextArea();
        contentArea.setFont(uiFont.deriveFont(16f));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBounds(20, 190, 545, 380);
        contentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(contentScroll);

        // 수정 모드라면 기존 내용 채우기
        if (postToEdit != null) {
            titleField.setText(postToEdit.title);
            contentArea.setText(postToEdit.content);
        }

        // 4. 버튼 영역
        JButton submitBtn = createStyledButton(postToEdit != null ? "수정 완료" : "등록", 150, 50);
        submitBtn.setBounds(415, 600, 150, 50);
        submitBtn.addActionListener(e -> handleSubmit());
        add(submitBtn);

        JButton cancelBtn = createStyledButton("취소", 100, 50);
        Color CANCEL_COLOR = new Color(150, 150, 150);
        cancelBtn.setBackground(CANCEL_COLOR);
        cancelBtn.setBorder(new RoundedBorder(15, CANCEL_COLOR, 1));
        cancelBtn.setBounds(295, 600, 100, 50);
        cancelBtn.addActionListener(e ->
                showCustomConfirmPopup("작성을 취소하고 돌아가시겠습니까?", this::dispose));
        add(cancelBtn);
    }

    // ----------------------------------
    //  등록 / 수정 공통 처리
    // ----------------------------------
    private void handleSubmit() {
        String title   = titleField.getText().trim();
        String content = contentArea.getText().trim();

        // 1) 입력 검증
        if (title.isEmpty() || title.length() > 20) {
            showCustomAlertPopup("경고", "제목은 1자 이상 20자 이내로 작성해야 합니다.");
            return;
        }

        if (content.isEmpty() || content.length() > 500) {
            showCustomAlertPopup("경고", "내용은 1자 이상 500자 이내로 작성해야 합니다.");
            return;
        }

        // 2) 로그인 사용자 확인
        Member loginUser = LoginSession.getUser();
        if (loginUser == null) {
            showCustomAlertPopup("오류", "로그인 정보가 없어 게시글을 저장할 수 없습니다.\n다시 로그인 후 이용해주세요.");
            return;
        }

        String writerHakbun   = loginUser.getHakbun();
        String writerNickname = getDisplayName(loginUser);

        if (writerHakbun == null || writerHakbun.isBlank()) {
            showCustomAlertPopup("오류", "학번 정보가 없어 게시글을 저장할 수 없습니다.");
            return;
        }

        // 3) 확인 팝업 후 실제 DB 처리
        String confirmMessage = (postToEdit != null)
                ? "게시글을 수정하시겠습니까?"
                : "게시글을 등록하시겠습니까?";

        showCustomConfirmPopup(confirmMessage, () -> {

            if (postToEdit != null) {
                // ===== 수정 모드 =====
                communityDAO.updatePost(postToEdit.no, title, content);

                postToEdit.title   = title;
                postToEdit.content = content;

                if (detailParent != null) {
                    detailParent.updatePostContent(postToEdit);
                }
                if (parentFrame != null) {
                    parentFrame.searchPosts();  // 목록 새로고침
                }

                showCustomAlertPopup("수정 완료", "게시글이 성공적으로 수정되었습니다.");

            } else {
                // ===== 새 글 작성 모드 =====
                int newPostId = communityDAO.insertPost(writerHakbun, writerNickname, title, content);
                if (newPostId <= 0) {
                    showCustomAlertPopup("오류", "게시글 저장 중 문제가 발생했습니다.");
                    return;
                }

                String date = LocalDate.now().toString();
                Post newPost = new Post(newPostId, title, writerNickname, date, 0, 0, content);

                if (parentFrame != null) {
                    parentFrame.addPost(newPost);   // 목록에 추가
                }

                showCustomAlertPopup("등록 완료", "게시글이 성공적으로 등록되었습니다.");
            }

            dispose(); // 창 닫기
        });
    }

    // ===============================
    // 헬퍼 메소드
    // ===============================

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

    private void showCustomAlertPopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 80, 360, 30);
        panel.add(msgLabel);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private void showCustomConfirmPopup(String message, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, "확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
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

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    // 테스트용 main (원하면 삭제해도 됨)
    public static void main(String[] args) {
        CommunityFrame dummyParent = new CommunityFrame();
        SwingUtilities.invokeLater(() -> new CommunityWriteFrame("테스트사용자", dummyParent));
    }
}
