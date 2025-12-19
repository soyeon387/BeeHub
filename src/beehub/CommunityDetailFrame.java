package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import beehub.CommunityFrame.Post;

public class CommunityDetailFrame extends JFrame {

    // 🎨 꿀단지 컬러 테마
    private static final Color HEADER_YELLOW    = new Color(255, 238, 140);
    private static final Color BG_MAIN          = new Color(255, 255, 255);
    private static final Color BROWN            = new Color(89, 60, 28);
    private static final Color BORDER_COLOR     = new Color(220, 220, 220);
    private static final Color POPUP_BG         = new Color(255, 250, 205);
    private static final Color AUTHOR_HIGHLIGHT = new Color(255, 180, 0);

    private static Font uiFont;

    static {
        try {
            File fontFile = new File("resource/fonts/DNFBitBitv2.ttf");
            if (fontFile.exists()) {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);
            } else {
                InputStream is = CommunityDetailFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
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

    private Post currentPost;
    private DefaultListModel<String> commentModel;
    private ImageIcon heartIcon;

    private Member loginMember;
    private String currentUser;    
    private String currentHakbun;  
    private boolean isLiked = false;

    private JLabel commentTitle;
    private JButton likeBtn;
    private JLabel postTitle;
    private JLabel writerInfo;
    private JTextArea contentArea;
    private JLabel likeLabel;

    private CommunityDAO dao = new CommunityDAO();

    public CommunityDetailFrame(Post post, ImageIcon icon, String user, CommunityFrame parent) {
        this.currentPost = post;
        this.heartIcon = icon;
        
        this.loginMember = LoginSession.getUser();
        if (loginMember != null) {
            currentHakbun = loginMember.getHakbun();
            this.currentUser = resolveDisplayName(loginMember);
        } else {
            this.currentUser = user;
        }

        try {
            currentPost.likes = dao.getLikeCount(currentPost.no);
            if (currentHakbun != null) {
                isLiked = dao.hasUserLiked(currentPost.no, currentHakbun);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("서울여대 꿀단지 - 게시글 상세");
        setSize(800, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();

        setVisible(true);
    }

    private String resolveDisplayName(Member m) {
        if (m == null) return null;
        if (m.getNickname() != null && !m.getNickname().trim().isEmpty()) {
            return m.getNickname().trim();
        }
        return m.getName();
    }

    private void initUI() {
        // 1. 헤더 패널 (높이 60)
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 800, 60);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel title = new JLabel(" 게시글 상세", JLabel.LEFT);
        title.setFont(uiFont.deriveFont(24f));
        title.setForeground(BROWN);
        title.setBounds(40, 10, 300, 40);
        headerPanel.add(title);

        JButton backBtn = createStyledButton("이전으로", 100, 40);
        backBtn.setBackground(BROWN); 
        backBtn.setForeground(Color.WHITE);
        backBtn.setBounds(650, 10, 100, 40);
        backBtn.addActionListener(e -> {
            new CommunityFrame(); 
            dispose();            
        });
        headerPanel.add(backBtn);

        // 2. 바디 패널
        JPanel bodyPanel = new JPanel(null);
        bodyPanel.setBounds(0, 60, 800, 590);
        bodyPanel.setBackground(BG_MAIN);
        add(bodyPanel);

        // (1) 게시글 정보 박스 [위치 조정: Y 20 -> 10, 높이 80 -> 75]
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(40, 10, 710, 75); 
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        bodyPanel.add(infoPanel);

        // 제목
        postTitle = new JLabel(currentPost.title);
        postTitle.setFont(uiFont.deriveFont(Font.BOLD, 20f));
        postTitle.setForeground(BROWN);
        postTitle.setBounds(20, 12, 550, 30); // Y조정
        infoPanel.add(postTitle);

        // 작성자 및 날짜
        writerInfo = new JLabel("작성자: " + currentPost.writer + "  |  " + currentPost.date);
        writerInfo.setFont(uiFont.deriveFont(14f));
        writerInfo.setForeground(Color.GRAY);
        writerInfo.setBounds(20, 42, 400, 20); // Y조정
        infoPanel.add(writerInfo);

        // 좋아요 수
        likeLabel = new JLabel(" " + currentPost.likes);
        if (heartIcon != null) likeLabel.setIcon(heartIcon);
        likeLabel.setFont(uiFont.deriveFont(16f));
        likeLabel.setForeground(new Color(255, 100, 100));
        likeLabel.setBounds(600, 27, 100, 20); // Y조정
        infoPanel.add(likeLabel);

        // (2) 본문 영역 [위치 조정: Y 115 -> 95]
        contentArea = new JTextArea(currentPost.content);
        contentArea.setFont(uiFont.deriveFont(16f));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBounds(40, 95, 710, 200); // Y를 위로 당김
        contentScroll.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        bodyPanel.add(contentScroll);

        // (3) 컨트롤 바 [위치 조정: Y 325 -> 305]
        JPanel controlBar = new JPanel(null);
        controlBar.setBounds(40, 305, 710, 50); 
        controlBar.setOpaque(false);
        bodyPanel.add(controlBar);

        likeBtn = createStyledButton(" 좋아요", 120, 40);
        if (heartIcon != null) likeBtn.setIcon(heartIcon);
        likeBtn.setBackground(Color.WHITE);
        likeBtn.setForeground(new Color(255, 100, 100));
        likeBtn.setBounds(0, 5, 120, 40);
        likeBtn.addActionListener(e -> handleLikeAction(likeLabel));
        if (isLiked) likeBtn.setBackground(new Color(255, 240, 240));
        controlBar.add(likeBtn);
        
        if (isMyPost()) {
            JPanel editDeletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            editDeletePanel.setOpaque(false);
            editDeletePanel.setBounds(490, 5, 220, 40);

            JLabel editLink = createTextLink("수정");
            editLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new CommunityWriteFrame(currentUser, currentPost);
                    dispose();
                }
            });

            JLabel deleteLink = createTextLink("삭제");
            deleteLink.setForeground(new Color(200, 50, 50));
            deleteLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showCustomConfirmPopup("게시글을 삭제하시겠습니까?", () -> {
                        dao.deletePost(currentPost.no);
                        showCustomAlertPopup("삭제 완료", "게시글이 삭제되었습니다.", () -> {
                            new CommunityFrame(); 
                            dispose();
                        });
                    });
                }
            });

            editDeletePanel.add(editLink);
            editDeletePanel.add(new JLabel("|"));
            editDeletePanel.add(deleteLink);
            controlBar.add(editDeletePanel);
        }

        // (4) 댓글 제목 [위치 조정: Y 375 -> 355]
        commentTitle = new JLabel(" 댓글 (0)");
        commentTitle.setFont(uiFont.deriveFont(16f));
        commentTitle.setForeground(BROWN);
        commentTitle.setBounds(40, 355, 150, 25);
        bodyPanel.add(commentTitle);

        // (5) 댓글 리스트 [위치 조정: Y 405 -> 385]
        commentModel = new DefaultListModel<>();
        loadCommentsFromDB();

        JList<String> commentList = new JList<>(commentModel);
        commentList.setFont(uiFont.deriveFont(14f));
        commentList.setCellRenderer(new CommentListRenderer(currentPost.writer));

        JScrollPane commentScroll = new JScrollPane(commentList);
        commentScroll.setBounds(40, 385, 710, 100); 
        commentScroll.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        bodyPanel.add(commentScroll);

        // (6) 댓글 입력창 [위치 조정: Y 515 -> 495]
        // 전체 높이가 650(바디 590)이므로, Y=495 + H=40 = 535 (여유 공간 충분)
        JTextField commentInput = new JTextField();
        commentInput.setBounds(40, 495, 600, 40);
        commentInput.setFont(uiFont.deriveFont(14f));
        commentInput.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        bodyPanel.add(commentInput);

        // 등록 버튼 [위치 조정]
        JButton addCommentBtn = createStyledButton("등록", 100, 40);
        addCommentBtn.setBackground(BROWN);
        addCommentBtn.setForeground(Color.WHITE);
        addCommentBtn.setBounds(650, 495, 100, 40);
        
        ActionListener submitAction = e -> {
            String text = commentInput.getText().trim();
            if (text.isEmpty()) return;

            Member m = LoginSession.getUser();
            if (m == null) {
                showCustomAlertPopup("오류", "로그인 후 댓글을 작성할 수 있습니다.");
                return;
            }

            String writerNickname = (m.getNickname() != null && !m.getNickname().isBlank()) ? m.getNickname() : m.getName();
            dao.insertComment(currentPost.no, m.getHakbun(), writerNickname, text);

            loadCommentsFromDB();
            commentInput.setText("");
        };
        
        addCommentBtn.addActionListener(submitAction);
        
        commentInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) addCommentBtn.doClick();
            }
        });
        
        bodyPanel.add(addCommentBtn);
    }

    private boolean isMyPost() {
        if (currentPost == null || currentPost.writer == null) return false;
        if (currentUser == null) currentUser = resolveDisplayName(loginMember);
        if (currentUser == null) return false;
        return currentPost.writer.trim().equals(currentUser.trim());
    }

    private void loadCommentsFromDB() {
        commentModel.clear();
        try {
            List<CommunityDAO.CommentDTO> list = dao.getCommentsByPostId(currentPost.no);
            for (CommunityDAO.CommentDTO c : list) {
                commentModel.addElement(c.writerNickname + ":" + c.content);
            }
            currentPost.comments = list.size();
        } catch (Exception e) {}

        if (commentTitle != null) {
            commentTitle.setText(" 댓글 (" + commentModel.getSize() + ")");
        }
    }

    private void handleLikeAction(JLabel likeLabel) {
        if (loginMember == null) {
            showCustomAlertPopup("알림", "로그인 후 이용할 수 있습니다.");
            return;
        }
        if (isLiked) {
            showCustomAlertPopup("알림", "이미 좋아요를 누른 게시글입니다.");
            return;
        }
        try {
            dao.addLike(currentPost.no, currentHakbun);
            int newCount = dao.getLikeCount(currentPost.no);
            currentPost.likes = newCount;
            likeLabel.setText(" " + newCount);
            isLiked = true;
            likeBtn.setBackground(new Color(255, 240, 240));
            showCustomAlertPopup("좋아요", "이 글을 좋아합니다!");
        } catch (Exception e) {
            showCustomAlertPopup("오류", "좋아요 처리 중 문제가 발생했습니다.");
        }
    }
    
    public void updatePostContent(Post updatedPost) {
        this.currentPost = updatedPost;
        postTitle.setText(updatedPost.title);
        writerInfo.setText("작성자: " + updatedPost.writer + "  |  " + updatedPost.date);
        contentArea.setText(updatedPost.content);
        revalidate(); repaint();
    }

    private JLabel createTextLink(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont.deriveFont(14f));
        label.setForeground(BROWN);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        label.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { label.setText("<html><u>" + text + "</u></html>"); }
            public void mouseExited(MouseEvent e) { label.setText(text); }
        });
        return label;
    }

    class CommentListRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel nameLabel = new JLabel();
        private JLabel contentLabel = new JLabel();

        public CommentListRenderer(String writer) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setOpaque(true);
            nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 14f));
            contentLabel.setFont(uiFont.deriveFont(14f));
            add(nameLabel);
            add(contentLabel);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
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
        showCustomAlertPopup(title, message, null);
    }
    
    private void showCustomAlertPopup(String title, String message, Runnable onOk) {
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
        msgLabel.setBounds(20, 60, 360, 80);
        panel.add(msgLabel);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.addActionListener(e -> {
            dialog.dispose();
            if(onOk != null) onOk.run();
        });
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

    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }
}