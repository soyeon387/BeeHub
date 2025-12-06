package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import beehub.CommunityFrame.Post;
import beehub.CommunityDAO;
import beehub.LoginSession;
import beehub.Member;

public class CommunityDetailFrame extends JFrame {

    private static final Color HEADER_YELLOW    = new Color(255, 238, 140);
    private static final Color BROWN            = new Color(89, 60, 28);
    private static final Color BG_MAIN          = new Color(255, 255, 255);
    private static final Color BORDER_COLOR     = new Color(220, 220, 220);
    private static final Color POPUP_BG         = new Color(255, 250, 205);
    private static final Color AUTHOR_HIGHLIGHT = new Color(255, 180, 0);

    private static Font uiFont;
    private static final String FONT_NAME_HTML = "던파 비트비트체 v2";

    static {
        try {
            File fontFile = new File("resource/fonts/DNFBitBitv2.ttf");
            if (fontFile.exists()) {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);
            } else {
                InputStream is =
                        CommunityDetailFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
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
            e.printStackTrace();
        }
    }

    // ====== 필드 ======
    private Post currentPost;
    private DefaultListModel<String> commentModel;
    private ImageIcon heartIcon;

    // 로그인 / 좋아요용
    private Member loginMember;
    private String currentUser;    // 현재 로그인한 유저 닉네임(또는 이름)
    private String currentHakbun;  // 좋아요 테이블용
    private boolean isLiked = false;

    private JLabel commentTitle;
    private JButton likeBtn;

    // 게시글 수정 후 업데이트용
    private JLabel postTitle;
    private JLabel writerInfo;
    private JTextArea contentArea;
    private JLabel likeLabel;

    // 수정 / 삭제용
    private CommunityFrame parentFrame;

    // 댓글·좋아요 DB 연동용 DAO
    private CommunityDAO dao = new CommunityDAO();

    // ====== 생성자 ======
    public CommunityDetailFrame(Post post, ImageIcon icon, String user, CommunityFrame parent) {
        this.currentPost = post;
        this.heartIcon = icon;
        this.parentFrame = parent;

        // 🔐 로그인 회원 정보
        this.loginMember = LoginSession.getUser();
        if (loginMember != null) {
            currentHakbun = loginMember.getHakbun();
            this.currentUser = resolveDisplayName(loginMember);
        } else {
            // 혹시 로그인 정보가 null이면 파라미터 값 사용
            this.currentUser = user;
        }

        // 🔗 DB에서 좋아요 수 & 내가 눌렀는지 미리 가져오기
        try {
            currentPost.likes = dao.getLikeCount(currentPost.no);           // 총 좋아요 수
            if (currentHakbun != null) {
                isLiked = dao.hasUserLiked(currentPost.no, currentHakbun);  // 내가 이미 눌렀는지
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 창 닫을 때 현재 댓글 수를 Post에 반영
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (commentModel != null) {
                    currentPost.comments = commentModel.getSize();
                }
            }
        });

        setTitle("게시글 상세 - " + post.title);
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        setVisible(true);
    }

    // 현재 로그인한 사람의 표시 이름(닉네임 > 이름)
    private String resolveDisplayName(Member m) {
        if (m == null) return null;
        if (m.getNickname() != null && !m.getNickname().trim().isEmpty()) {
            return m.getNickname().trim();
        }
        return m.getName();
    }

    // ====== UI 구성 ======
    private void initUI() {

        // 1. 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setBounds(0, 0, 600, 50);
        header.setBackground(HEADER_YELLOW);

        JLabel title = new JLabel(" 커뮤니티 > 게시글 상세", JLabel.LEFT);
        title.setFont(uiFont.deriveFont(18f));
        title.setForeground(BROWN);
        header.add(title, BorderLayout.WEST);
        add(header);

        // 2. 게시글 정보
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(20, 70, 545, 100);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));

        postTitle = new JLabel(currentPost.title);
        postTitle.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        postTitle.setBounds(20, 15, 500, 30);
        infoPanel.add(postTitle);

        writerInfo = new JLabel("작성자: " + currentPost.writer + "  |  " + currentPost.date);
        writerInfo.setFont(uiFont.deriveFont(14f));
        writerInfo.setForeground(Color.GRAY);
        writerInfo.setBounds(20, 55, 350, 20);
        infoPanel.add(writerInfo);

        likeLabel = new JLabel(" " + currentPost.likes);
        if (heartIcon != null) likeLabel.setIcon(heartIcon);
        likeLabel.setFont(uiFont.deriveFont(16f));
        likeLabel.setForeground(new Color(255, 100, 100));
        likeLabel.setBounds(450, 55, 80, 20);
        infoPanel.add(likeLabel);

        add(infoPanel);

        // 3. 본문
        contentArea = new JTextArea(currentPost.content);
        contentArea.setFont(uiFont.deriveFont(16f));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBounds(20, 180, 545, 200);
        contentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(contentScroll);

        // 4. 컨트롤 바 (좋아요 + 수정/삭제)
        JPanel controlBar = new JPanel(null);
        controlBar.setBounds(20, 395, 545, 55);
        controlBar.setOpaque(false);

        int likeBtnWidth = 140;
        int likeBtnHeight = 50;
        int likeBtnX = (545 - likeBtnWidth) / 2;
        int likeBtnY = (55 - likeBtnHeight) / 2;

        likeBtn = createStyledButton(" 좋아요", likeBtnWidth, likeBtnHeight);
        if (heartIcon != null) likeBtn.setIcon(heartIcon);
        likeBtn.setBackground(Color.WHITE);
        likeBtn.setForeground(new Color(255, 100, 100));
        likeBtn.setBounds(likeBtnX, likeBtnY, likeBtnWidth, likeBtnHeight);
        likeBtn.addActionListener(e -> handleLikeAction(likeLabel));

        // 이미 좋아요한 글이면 버튼 색만 살짝 바꿔줌
        if (isLiked) {
            likeBtn.setBackground(new Color(255, 240, 240));
        }

        controlBar.add(likeBtn);

        // ✏️ 작성자일 때만 수정/삭제 링크
        if (isMyPost()) {
            JPanel editDeletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            editDeletePanel.setOpaque(false);

            JLabel editLink = createTextLink("수정");
            editLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (parentFrame != null) {
                        new CommunityWriteFrame(currentUser, parentFrame,
                                currentPost, CommunityDetailFrame.this);
                    } else {
                        showCustomAlertPopup("오류", "부모 프레임 참조가 없어 수정할 수 없습니다.");
                    }
                }
            });

            JLabel deleteLink = createTextLink("삭제");
            deleteLink.setForeground(new Color(200, 50, 50));
            deleteLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showCustomConfirmPopup("게시글을 삭제하시겠습니까?", () -> {
                        if (parentFrame != null) {
                            parentFrame.deletePost(currentPost);
                            dispose();
                        } else {
                            showCustomAlertPopup("오류", "부모 프레임 참조가 없어 삭제할 수 없습니다.");
                        }
                    });
                }
            });

            editDeletePanel.add(editLink);
            editDeletePanel.add(new JLabel(" "));
            editDeletePanel.add(deleteLink);

            editDeletePanel.setBounds(400, 15, 145, 30);
            controlBar.add(editDeletePanel);
        }

        add(controlBar);

        // 5. 댓글 영역 타이틀
        commentTitle = new JLabel(" 댓글 (0)");
        commentTitle.setFont(uiFont.deriveFont(16f));
        commentTitle.setForeground(BROWN);
        commentTitle.setBounds(25, 460, 150, 25);
        add(commentTitle);

        // 6. 댓글 목록 (DB에서 로드)
        commentModel = new DefaultListModel<>();
        loadCommentsFromDB();   // DB 연동

        JList<String> commentList = new JList<>(commentModel);
        commentList.setFont(uiFont.deriveFont(14f));
        commentList.setCellRenderer(new CommentListRenderer(currentPost.writer));

        JScrollPane commentScroll = new JScrollPane(commentList);
        commentScroll.setBounds(20, 490, 545, 100);
        commentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(commentScroll);

        // 7. 댓글 입력
        JTextField commentInput = new JTextField();
        commentInput.setBounds(20, 600, 430, 40);
        commentInput.setFont(uiFont.deriveFont(14f));
        add(commentInput);

        JButton addCommentBtn = createStyledButton("등록", 100, 40);
        addCommentBtn.setBounds(465, 600, 100, 40);
        addCommentBtn.addActionListener(e -> {
            String text = commentInput.getText().trim();
            if (text.isEmpty()) return;

            Member m = LoginSession.getUser();
            if (m == null) {
                showCustomAlertPopup("오류", "로그인 정보가 없어 댓글을 달 수 없습니다.");
                return;
            }

            // ✅ 닉네임 있으면 닉네임, 없으면 실명
            String writerNickname;
            if (m.getNickname() != null && !m.getNickname().trim().isEmpty()) {
                writerNickname = m.getNickname().trim();
            } else {
                writerNickname = m.getName();
            }

            // DB 저장
            dao.insertComment(
                    currentPost.no,
                    m.getHakbun(),
                    writerNickname,
                    text
            );

            // 다시 로드해서 갱신
            loadCommentsFromDB();
            commentInput.setText("");
        });
        add(addCommentBtn);
    }

    // 내가 쓴 글인지 확인
    private boolean isMyPost() {
        if (currentPost == null || currentPost.writer == null) return false;
        if (currentUser == null) currentUser = resolveDisplayName(loginMember);
        if (currentUser == null) return false;
        return currentPost.writer.trim().equals(currentUser.trim());
    }

    // ====== 댓글 DB에서 로드 ======
    private void loadCommentsFromDB() {
        commentModel.clear();
        try {
            List<CommunityDAO.CommentDTO> list = dao.getCommentsByPostId(currentPost.no);
            for (CommunityDAO.CommentDTO c : list) {
                // "닉네임:내용" 형태로 저장 → 렌더러에서 분리해서 씀
                commentModel.addElement(c.writerNickname + ":" + c.content);
            }
            currentPost.comments = list.size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (commentTitle != null) {
            commentTitle.setText(" 댓글 (" + commentModel.getSize() + ")");
        }
    }

    // ====== 좋아요 처리 (DB 연동) ======
    private void handleLikeAction(JLabel likeLabel) {
        // 로그인 체크
        if (loginMember == null) {
            showCustomAlertPopup("알림", "로그인 후 이용할 수 있습니다.");
            return;
        }

        // 이미 눌렀으면 막기
        if (isLiked) {
            showCustomAlertPopup("알림", "이미 좋아요를 누른 게시글입니다.");
            return;
        }

        try {
            // DB에 좋아요 기록 추가
            dao.addLike(currentPost.no, currentHakbun);

            // 다시 DB에서 카운트 가져와서 반영
            int newCount = dao.getLikeCount(currentPost.no);
            currentPost.likes = newCount;
            likeLabel.setText(" " + newCount);

            isLiked = true;
            likeBtn.setBackground(new Color(255, 240, 240));

            showCustomAlertPopup("좋아요", "이 글을 좋아합니다!");
        } catch (Exception e) {
            e.printStackTrace();
            showCustomAlertPopup("오류", "좋아요 처리 중 문제가 발생했습니다.");
        }
    }

    // ====== 수정 완료 후 내용 업데이트 ======
    public void updatePostContent(Post updatedPost) {
        this.currentPost = updatedPost;
        postTitle.setText(updatedPost.title);
        writerInfo.setText("작성자: " + updatedPost.writer + "  |  " + updatedPost.date);
        contentArea.setText(updatedPost.content);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    // ====== 텍스트 링크 ======
    private JLabel createTextLink(String text) {
        String underlineColor = toHexString(BROWN);

        JLabel label = new JLabel(
                "<html><body style='color:" + toHexString(BROWN) + ";'>"
                        + "<font face='" + FONT_NAME_HTML + "'>"
                        + "<u style='text-decoration-color: " + underlineColor + ";'>"
                        + text + "</u>"
                        + "</font></body></html>"
        );
        label.setFont(uiFont.deriveFont(14f));
        label.setForeground(BROWN);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return label;
    }

    private String toHexString(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ====== 댓글 렌더러 ======
    class CommentListRenderer extends JPanel implements ListCellRenderer<String> {
        String postWriter;
        private JLabel nameLabel = new JLabel();
        private JLabel contentLabel = new JLabel();

        public CommentListRenderer(String writer) {
            this.postWriter = writer;
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setOpaque(true);

            nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 14f));
            contentLabel.setFont(uiFont.deriveFont(14f));

            add(nameLabel);
            add(contentLabel);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {

            Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
            setBackground(bg);
            nameLabel.setBackground(bg);
            contentLabel.setBackground(bg);
            nameLabel.setOpaque(true);
            contentLabel.setOpaque(true);

            String[] parts = value.split(":", 2);
            String name = parts[0].trim();
            String content = (parts.length > 1) ? parts[1].trim() : "";

            if (name.equals(currentPost.writer)) {
                nameLabel.setText("작성자");
                nameLabel.setForeground(AUTHOR_HIGHLIGHT);
            } else {
                nameLabel.setText(name);
                nameLabel.setForeground(BROWN);
            }

            contentLabel.setText(" : " + content);
            contentLabel.setForeground(BROWN);

            return this;
        }
    }

    // ====== 공통 버튼 / 팝업 / 테두리 ======
    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setPreferredSize(new Dimension(w, h));
        return btn;
    }

    private JPanel createPopupPanel() {
        return new JPanel() {
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
    }

    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
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

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }
}
