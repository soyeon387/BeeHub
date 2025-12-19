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

        setUndecorated(true);
        setSize(800, 450);
        setLocationRelativeTo(parent);

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

        JPanel topBtnArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topBtnArea.setOpaque(false);

        JButton excelBtn = new JButton("엑셀 저장");
        excelBtn.setFont(uiFont.deriveFont(14f));
        excelBtn.setBackground(new Color(60, 160, 60));
        excelBtn.setForeground(Color.WHITE);
        excelBtn.setFocusPainted(false);
        excelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        excelBtn.addActionListener(e -> saveToExcel());
        topBtnArea.add(excelBtn);

        JButton closeBtn = new JButton("X");
        closeBtn.setFont(uiFont.deriveFont(18f));
        closeBtn.setForeground(BROWN);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorder(null);
        closeBtn.addActionListener(e -> dispose());
        topBtnArea.add(closeBtn);

        header.add(topBtnArea, BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);
    }

    private void saveToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("명단 저장 경로 선택");
        fileChooser.setSelectedFile(new java.io.File(event.title + "_명단.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "MS949"))) {

                // 1) 헤더
                for (int i = 0; i < table.getColumnCount(); i++) {
                    bw.write(table.getColumnName(i));
                    if (i < table.getColumnCount() - 1) bw.write(",");
                }
                bw.newLine();

                // 2) 데이터
                for (int r = 0; r < table.getRowCount(); r++) {
                    for (int c = 0; c < table.getColumnCount(); c++) {
                        Object v = table.getValueAt(r, c);
                        bw.write(v == null ? "" : v.toString());
                        if (c < table.getColumnCount() - 1) bw.write(",");
                    }
                    bw.newLine();
                }

                // ✅ 기본 JOptionPane 대신: 우리 UI 팝업
                showStyledPopup("저장 완료",
                        "성공적으로 저장되었습니다!\n\n경로:\n" + file.getAbsolutePath(),
                        false);

            } catch (Exception ex) {
                ex.printStackTrace();

                // ✅ 기본 JOptionPane 대신: 우리 UI 팝업
                showStyledPopup("저장 실패",
                        "파일 저장 중 오류가 발생했습니다.\n\n(권한/경로/파일명 등을 확인해주세요.)",
                        true);
            }
        }
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
                    row.add(idx++);
                    row.add(rs.getString("name"));
                    row.add(rs.getString("participant_hakbun"));

                    String fee = rs.getString("is_fee_paid");
                    String feeLabel = "Y".equalsIgnoreCase(fee) ? "납부" :
                                      "N".equalsIgnoreCase(fee) ? "미납" : "-";
                    row.add(feeLabel);
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

    // =========================
    // ✅ 우리가 꾸민 스타일 팝업
    // =========================
    private void showStyledPopup(String titleText, String message, boolean isError) {
        JDialog popup = new JDialog(this, true);
        popup.setUndecorated(true);
        popup.setSize(460, 240);
        popup.setLocationRelativeTo(this);

        JPanel outer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
            }
        };
        outer.setOpaque(false);
        outer.setLayout(new BorderLayout());
        outer.setBorder(new EmptyBorder(16, 18, 14, 18));
        popup.setContentPane(outer);

        // 상단 타이틀
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel(titleText);
        title.setFont(uiFont.deriveFont(18f));
        title.setForeground(BROWN);
        top.add(title, BorderLayout.WEST);

        JButton xBtn = new JButton("X");
        xBtn.setFont(uiFont.deriveFont(16f));
        xBtn.setForeground(BROWN);
        xBtn.setContentAreaFilled(false);
        xBtn.setBorder(null);
        xBtn.setFocusPainted(false);
        xBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        xBtn.addActionListener(e -> popup.dispose());
        top.add(xBtn, BorderLayout.EAST);

        outer.add(top, BorderLayout.NORTH);

        // 내용
        JTextArea area = new JTextArea(message);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(uiFont.deriveFont(14f));
        area.setForeground(new Color(60, 40, 20));
        area.setBorder(new EmptyBorder(10, 4, 10, 4));
        outer.add(area, BorderLayout.CENTER);

        // 버튼
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottom.setOpaque(false);

        JButton ok = new JButton("확인");
        ok.setFont(uiFont.deriveFont(14f));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ok.setBackground(isError ? new Color(220, 80, 80) : new Color(60, 160, 60));
        ok.addActionListener(e -> popup.dispose());
        bottom.add(ok);

        outer.add(bottom, BorderLayout.SOUTH);

        popup.setVisible(true);
    }
}
