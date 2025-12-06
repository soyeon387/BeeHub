package council;

import beehub.DBUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class CouncilRecipientDialog extends JDialog {

    // 색상 (기존 UI 톤 비슷하게)
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN       = new Color(255, 250, 205);
    private static final Color BROWN         = new Color(139, 90, 43);

    private static Font uiFont;
    static {
        try {
            java.io.InputStream is = CouncilRecipientDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JTable table;
    private final EventManager.EventData event;

    public CouncilRecipientDialog(JFrame parent, EventManager.EventData event) {
        super(parent, true);
        this.event = event;

        setUndecorated(true); // 테두리 빼고 직접 꾸미기
        setSize(800, 450);
        setLocationRelativeTo(parent);

        // 바깥 패널(배경 + 둥근 테두리)
        JPanel outer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
        outer.setLayout(new BorderLayout());
        outer.setOpaque(false);
        setContentPane(outer);

        initHeader(outer);
        initTable(outer);

        setVisible(true);
    }

    private void initHeader(JPanel outer) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 25, 10, 25));

        JLabel title = new JLabel("[" + event.title + "] 명단");
        title.setFont(uiFont.deriveFont(22f));
        title.setForeground(BROWN);
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = new JButton("X");
        closeBtn.setFont(uiFont.deriveFont(18f));
        closeBtn.setForeground(BROWN);
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorder(null);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        header.add(closeBtn, BorderLayout.EAST);

        outer.add(header, BorderLayout.NORTH);
    }

    private void initTable(JPanel outer) {
        String[] columns = {"순번", "이름", "학번", "학생회비 납부"};

        Vector<String> colNames = new Vector<>();
        for (String c : columns) colNames.add(c);
        Vector<Vector<Object>> rows = new Vector<>();

        String sql =
                "SELECT ep.participation_id, ep.participant_hakbun, " +
                "       m.name, m.is_fee_paid " +
                "FROM event_participation ep " +
                "LEFT JOIN members m ON ep.participant_hakbun = m.hakbun " +
                "WHERE ep.event_id = ? " +
                "ORDER BY ep.participation_date ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, event.eventId);

            try (ResultSet rs = pstmt.executeQuery()) {
                int idx = 1;
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(idx++);                                     // 순번
                    row.add(rs.getString("name"));                      // 이름
                    row.add(rs.getString("participant_hakbun"));        // 학번

                    String fee = rs.getString("is_fee_paid");
                    String feeLabel = "Y".equalsIgnoreCase(fee) ? "납부" :
                                      "N".equalsIgnoreCase(fee) ? "미납" : "-";
                    row.add(feeLabel);                                  // 학생회비 납부
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        table = new JTable(new javax.swing.table.DefaultTableModel(rows, colNames) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });

        table.setFont(uiFont.deriveFont(14f));
        table.setRowHeight(26);
        table.getTableHeader().setFont(uiFont.deriveFont(Font.BOLD, 15f));
        table.getTableHeader().setBackground(HEADER_YELLOW);
        table.getTableHeader().setForeground(BROWN);

        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(new EmptyBorder(10, 25, 25, 25));

        outer.add(sp, BorderLayout.CENTER);
    }
}
