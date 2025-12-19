package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityFrame extends JFrame {

    // 🎨 컬러 테마
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205); 

    private static Font uiFont;
    private ImageIcon heartIcon; 
    
    private String userName = "게스트";
    private String userId = "";
    private int userPoint = 0;

    // 폰트 로딩
    static {
        try {
            InputStream is = CommunityFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            } else {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }
    
    // UI 컴포넌트
    private JTextField searchField;
    private JTable postTable;
    private DefaultTableModel tableModel;
    private JPanel pagePanel; 

    // 데이터 및 페이지네이션 변수
    private List<Post> allPosts = new ArrayList<>(); 
    private List<Post> filteredPosts = new ArrayList<>(); 
    private CommunityDAO communityDAO = new CommunityDAO();
    private int currentPage = 1;
    
    // ✅ 한 페이지에 보여줄 개수 (8개 고정)
    private final int itemsPerPage = 8; 

    public CommunityFrame() {
        setTitle("서울여대 꿀단지 - 커뮤니티");
        setSize(850, 670);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 사용자 정보 로드
        User currentUser = UserManager.getCurrentUser();
        if(currentUser != null) {
            userName = currentUser.getName();
            userId = currentUser.getId();
            userPoint = currentUser.getPoints();
        }
        
        loadImages(); 
        loadPostsFromDB();
        initHeader();
        initNav();
        initContent();

        setVisible(true);    
    }
    
    private void loadPostsFromDB() {
        allPosts.clear();
        java.util.List<CommunityDAO.PostDTO> list = communityDAO.getAllPostsOrderByNewest();
        for (CommunityDAO.PostDTO dto : list) {
            Post p = new Post(
                dto.postId,
                dto.title,
                dto.writerNickname,
                dto.createdDate,
                dto.likeCount,
                dto.commentCount,
                dto.content
            );
            allPosts.add(p);
        }
        searchPosts();
    }

    public void addPost(Post newPost) {
        allPosts.add(0, newPost);
        searchPosts();
    }
    
    public void deletePost(Post postToDelete) {
        if (postToDelete != null) {
            communityDAO.deletePost(postToDelete.no);
        }
        allPosts.remove(postToDelete);
        searchPosts(false);
    }

    private void loadImages() {
        try {
            URL heartUrl = getClass().getResource("/img/heart.png");
            if (heartUrl != null) {
                ImageIcon origin = new ImageIcon(heartUrl);
                if (origin.getIconWidth() > 0) {
                    Image img = origin.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                    heartIcon = new ImageIcon(img);
                }
            }
        } catch (Exception e) {}
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 850, 80);
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
        userInfoPanel.setBounds(450, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfoText = new JLabel("[" + userName + "]님" +  " | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
           
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                showLogoutPopup(); 
            }
        });
        
        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);
    }

    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 850, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("커뮤니티"));
            navPanel.add(menuBtn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 850, 520);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 1. 상단 컨트롤 영역 (검색 & 글쓰기)
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBounds(50, 20, 750, 60);
        topContainer.setBackground(BG_MAIN);
        topContainer.setOpaque(false);

        // 검색 패널
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        
        searchField = new JTextField(20);
        searchField.setFont(uiFont.deriveFont(14f));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR, 1), 
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        searchField.setPreferredSize(new Dimension(220, 35));
        searchField.addActionListener(e -> searchPosts());

        JButton searchBtn = createStyledButton("검색", 70, 35);
        searchBtn.setBackground(Color.WHITE);
        searchBtn.setForeground(BROWN);
        searchBtn.addActionListener(e -> searchPosts());

        searchPanel.add(createLabel("검색 :"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // 글쓰기 버튼 패널
        JPanel writePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        writePanel.setOpaque(false);
        
        JButton writeBtn = createStyledButton("글쓰기", 90, 40);
        writeBtn.setBackground(Color.WHITE); 
        writeBtn.setForeground(BROWN);
        writeBtn.addActionListener(e -> {
            new CommunityWriteFrame(userName);
            dispose();
        });
        
        writePanel.add(writeBtn);

        topContainer.add(searchPanel, BorderLayout.WEST);
        topContainer.add(writePanel, BorderLayout.EAST);
        contentPanel.add(topContainer);

        // 2. 게시글 목록 테이블
        String[] headers = {"제목", "작성자", "작성일", "좋아요"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        postTable = new JTable(tableModel);
        styleTable(postTable);
        
        // 컬럼 너비 설정
        postTable.getColumnModel().getColumn(0).setPreferredWidth(450); 
        postTable.getColumnModel().getColumn(1).setPreferredWidth(100); 
        postTable.getColumnModel().getColumn(2).setPreferredWidth(120); 
        postTable.getColumnModel().getColumn(3).setPreferredWidth(80);  

        // 렌더러 설정
        postTable.getColumnModel().getColumn(0).setCellRenderer(new TitleCommentRenderer()); 
        if (heartIcon != null) {
            postTable.getColumnModel().getColumn(3).setCellRenderer(new IconTextRenderer(heartIcon)); 
        }

        // 클릭 이벤트
        postTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = postTable.getSelectedRow();
                    if (row != -1) {
                        // 빈 행 클릭 시 무시
                        Object val = tableModel.getValueAt(row, 0);
                        if (val instanceof String && ((String)val).isEmpty()) return;

                        // 현재 페이지 기준 인덱스 계산
                        int index = (currentPage - 1) * itemsPerPage + row;
                        if (index < 0 || index >= filteredPosts.size()) return;

                        Post selectedPost = filteredPosts.get(index);
                        if (selectedPost != null) {
                            new CommunityDetailFrame(selectedPost, heartIcon, userName, CommunityFrame.this);
                            dispose();
                        }
                    }
                }
            }
        });

        // ✅ [수정] 스크롤 패널을 고정된 테이블 뷰어처럼 설정
        JScrollPane scrollPane = new JScrollPane(postTable);
        
        // 높이 계산: Header(40) + Row(40 * 8 = 320) + 여유(2) = 362
        scrollPane.setBounds(50, 90, 750, 362); 
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // 스크롤바 숨김 & 마우스 휠 비활성화 (완벽한 고정 뷰)
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(false); 

        contentPanel.add(scrollPane);

        // 3. 페이지네이션 패널
        pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        // 테이블 바로 아래 (90 + 362 = 452, 여유 줘서 460)
        pagePanel.setBounds(50, 460, 750, 40);
        pagePanel.setBackground(BG_MAIN);
        contentPanel.add(pagePanel);

        // 초기 데이터 로드
        searchPosts();
    }
    
    // --- 기능 로직 ---
    public void searchPosts() {
        searchPosts(true);
    }

    public void searchPosts(boolean resetPage) {
        if (searchField == null) {
            filteredPosts.clear();
            filteredPosts.addAll(allPosts);
            return;
        }

        String keyword = searchField.getText().trim();
        filteredPosts.clear();

        if (keyword.isEmpty()) {
            filteredPosts.addAll(allPosts);
        } else {
            filteredPosts = allPosts.stream()
                .filter(p -> p.title.contains(keyword) || p.writer.contains(keyword))
                .collect(Collectors.toList());
        }

        filteredPosts.sort((p1, p2) -> Integer.compare(p2.no, p1.no));

        if (resetPage) {
            currentPage = 1;
        } else {
            int totalPages = (int) Math.ceil((double) filteredPosts.size() / itemsPerPage);
            if (totalPages == 0) totalPages = 1;
            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;
        }

        renderTable();
    }

    private void renderTable() {
        tableModel.setRowCount(0);

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredPosts.size());

        for (int i = start; i < end; i++) {
            Post post = filteredPosts.get(i);
            tableModel.addRow(new Object[]{
                new TitleWithCommentCount(post.title, post.comments), 
                post.writer, 
                formatDate(post.date), 
                post.likes
            });
        }
        
        // ✅ 8칸을 꽉 채우지 못했을 때 빈 행 추가 (디자인 유지)
        int currentRows = tableModel.getRowCount();
        if (currentRows < itemsPerPage) {
            for (int i = 0; i < (itemsPerPage - currentRows); i++) {
                tableModel.addRow(new Object[]{"", "", "", ""});
            }
        }
        
        updatePaginationPanel();
    }

    private void updatePaginationPanel() {
        pagePanel.removeAll();
        
        int calcPages = (int) Math.ceil((double) filteredPosts.size() / itemsPerPage);
        if (calcPages == 0) calcPages = 1;
        final int totalPages = calcPages; 

        JButton prevBtn = createPageButton("<", false);
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                renderTable();
            }
        });
        pagePanel.add(prevBtn);

        for (int i = 1; i <= totalPages; i++) {
            JButton numBtn = createPageButton(String.valueOf(i), i == currentPage);
            final int pageNum = i;
            numBtn.addActionListener(e -> {
                currentPage = pageNum;
                renderTable();
            });
            pagePanel.add(numBtn);
        }

        JButton nextBtn = createPageButton(">", false);
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                renderTable();
            }
        });
        pagePanel.add(nextBtn);

        pagePanel.revalidate();
        pagePanel.repaint();
    }

    private String formatDate(String dateStr) {
        try {
            LocalDate postDate = LocalDate.parse(dateStr);
            LocalDate today = LocalDate.now();
            long daysDiff = ChronoUnit.DAYS.between(postDate, today);

            if (daysDiff == 0) return "오늘";
            else if (daysDiff <= 30) return daysDiff + "일 전";
            else if (postDate.getYear() == today.getYear()) 
                return postDate.getMonthValue() + "월 " + postDate.getDayOfMonth() + "일";
            else return postDate.getYear() + "." + postDate.getMonthValue() + "." + postDate.getDayOfMonth();
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            dispose();
            new LoginFrame();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
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
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
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

    // --- 데이터 클래스 ---
    public static class Post {
        int no;
        String title;
        String writer;
        String date;
        int likes;
        int comments;
        String content;

        public Post() { }

        public Post(int n, String t, String w, String d, int l, int c, String content) {
            this.no = n;
            this.title = t;
            this.writer = w;
            this.date = d;
            this.likes = l;
            this.comments = c;
            this.content = content;
        }
    }

    class TitleWithCommentCount {
        String title; int commentCount;
        public TitleWithCommentCount(String t, int c) { title = t; commentCount = c; }
        @Override public String toString() { return title; }
    }

    class TitleCommentRenderer extends JPanel implements TableCellRenderer {
        private JLabel titleLabel = new JLabel();
        private JLabel countLabel = new JLabel();

        public TitleCommentRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setOpaque(true);
            
            titleLabel.setFont(uiFont.deriveFont(16f)); 
            titleLabel.setForeground(BROWN);
            
            countLabel.setFont(uiFont.deriveFont(14f));
            countLabel.setForeground(Color.GRAY);
            
            add(titleLabel);
            add(countLabel);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                setBackground(HIGHLIGHT_YELLOW);
                titleLabel.setForeground(BROWN); 
                countLabel.setForeground(Color.GRAY);
            } else {
                setBackground(Color.WHITE);
                titleLabel.setForeground(BROWN);
                countLabel.setForeground(Color.GRAY);
            }

            // 빈 행 처리
            if (value instanceof String && ((String)value).isEmpty()) {
                titleLabel.setText("");
                countLabel.setText("");
                return this;
            }

            if (value instanceof TitleWithCommentCount) {
                TitleWithCommentCount tc = (TitleWithCommentCount) value;
                titleLabel.setText(tc.title);
                if (tc.commentCount > 0) {
                    countLabel.setText("[" + tc.commentCount + "]");
                } else {
                    countLabel.setText(""); 
                }
            }
            return this;
        }
    }

    class IconTextRenderer extends DefaultTableCellRenderer {
        private Icon icon;
        public IconTextRenderer(Icon icon) { this.icon = icon; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(uiFont.deriveFont(14f)); 
            
            if (value == null || value.toString().isEmpty()) {
                c.setIcon(null);
                c.setText("");
                return c;
            }
            
            c.setIcon(icon);
            c.setText(" " + value.toString());
            c.setHorizontalAlignment(CENTER);
            return c;
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont.deriveFont(16f));
        label.setForeground(BROWN);
        return label;
    }

    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPageButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (isActive) {
            btn.setBackground(BROWN);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new RoundedBorder(10, BROWN, 1));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(BROWN);
            btn.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        }
        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(uiFont.deriveFont(14f)); 
        table.setRowHeight(40);
        table.setSelectionBackground(HIGHLIGHT_YELLOW);
        table.setSelectionForeground(BROWN);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(uiFont.deriveFont(16f));
        header.setBackground(HEADER_YELLOW);
        header.setForeground(BROWN);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BROWN));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 1 || i == 2) { 
                table.getColumnModel().getColumn(i).setCellRenderer(center);
            }
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
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("커뮤니티")) return;
                    if (text.equals("빈 강의실")) { new EmptyClassFrame(); dispose(); }
                    else if (text.equals("공간대여")) { new SpaceRentFrame(); dispose(); }
                    else if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                    else if (text.equals("과행사")) { new EventListFrame(); dispose(); }
                    else if (text.equals("마이페이지")) {  new MyPageFrame(); dispose();  }
                    else JOptionPane.showMessageDialog(null, "준비중입니다.");
                }
            });
        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CommunityFrame::new);
    }
}