package admin;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// 🔥 DB 기반으로 변경: beehub.Item / ItemDAO 사용
import beehub.Item;
import beehub.ItemDAO;

public class AdminItemAddDialog extends JDialog {

    private static final Color BG_YELLOW = new Color(255, 250, 205);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color GRAY = new Color(200, 200, 200);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;

    static {
        try {
            InputStream is = AdminItemAddDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
        }
    }

    // ====== 필드들 ======
    private AdminItemManageFrame parent;
    private Item currentItem;              // 🔥 수정 모드일 때 기존 아이템
    private String selectedImagePath = null;  // 🔥 DB에 저장할 image_path (예: "images/1733xxx_book.png")

    private JTextField nameField;
    private JSpinner stockSpinner, daySpinner;
    private JLabel imagePreview;           // 미리보기 라벨
    private List<JCheckBox> majorCheckBoxes = new ArrayList<>();

    // 🔥 실제로 사용하는 생성자
    public AdminItemAddDialog(AdminItemManageFrame parent, Item item) {
        super(parent, item == null ? "물품 등록" : "물품 수정", true);
        this.parent = parent;
        this.currentItem = item;

        setSize(500, 650);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(BG_YELLOW);

        initUI();
        if (item != null) loadData(item);

        setVisible(true);
    }

    // ======================== UI 구성 ========================
    private void initUI() {
        JLabel imgLabel = new JLabel("물품 사진");
        imgLabel.setFont(uiFont.deriveFont(16f));
        imgLabel.setForeground(BROWN);
        imgLabel.setBounds(30, 30, 100, 30);
        add(imgLabel);

        imagePreview = new JLabel("이미지 없음", SwingConstants.CENTER);
        imagePreview.setBorder(BorderFactory.createLineBorder(BROWN));
        imagePreview.setOpaque(true);
        imagePreview.setBackground(Color.WHITE);
        imagePreview.setBounds(30, 65, 100, 100);
        add(imagePreview);

        JButton uploadBtn = new JButton("사진 찾기");
        uploadBtn.setFont(uiFont.deriveFont(12f));
        uploadBtn.setBounds(140, 100, 100, 30);
        uploadBtn.setBackground(Color.WHITE);
        uploadBtn.setForeground(BROWN);
        // 🔥 사진 찾기 버튼은 이미지 선택만 한다
        uploadBtn.addActionListener(e -> selectImage());
        add(uploadBtn);

        addLabelAndField("물품명 :", 30, 200, 160);
        nameField = new JTextField();
        nameField.setBounds(140, 195, 300, 35);
        add(nameField);

        addLabelAndField("재고(개) :", 30, 250, 80);
        stockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
        stockSpinner.setBounds(140, 245, 80, 35);
        add(stockSpinner);

        addLabelAndField("대여기간(일):", 250, 250, 100);
        daySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        daySpinner.setBounds(360, 245, 80, 35);
        add(daySpinner);

        JLabel majorLabel = new JLabel("대상 학과 선택 (중복 가능)");
        majorLabel.setFont(uiFont.deriveFont(16f));
        majorLabel.setForeground(BROWN);
        majorLabel.setBounds(30, 300, 300, 30);
        add(majorLabel);

        JPanel majorPanel = new JPanel();
        majorPanel.setLayout(new BoxLayout(majorPanel, BoxLayout.Y_AXIS));
        majorPanel.setBackground(Color.WHITE);

        JCheckBox allCheck = new JCheckBox("전체 학과");
        allCheck.setFont(uiFont.deriveFont(Font.BOLD, 14f));
        allCheck.setBackground(Color.WHITE);
        allCheck.setForeground(BROWN);
        allCheck.addActionListener(e -> {
            boolean sel = allCheck.isSelected();
            for (JCheckBox cb : majorCheckBoxes) cb.setSelected(sel);
        });
        majorPanel.add(allCheck);
        majorPanel.add(Box.createVerticalStrut(10));

        addCollegeGroup(majorPanel, "인문대학",
                new String[]{"글로벌ICT인문융합학부", "국어국문학과", "영어영문학과", "중어중문학과", "일어일문학과", "사학과", "기독교학과"});
        addCollegeGroup(majorPanel, "사회과학대학",
                new String[]{"경제학과", "문헌정보학과", "사회복지학과", "아동학과", "행정학과", "언론영상학부", "심리.인지과학학부", "스포츠운동과학과"});
        addCollegeGroup(majorPanel, "과학기술융합대학",
                new String[]{"수학과", "화학과", "생명환경공학과", "바이오헬스융합학과", "원예생명조경학과", "식품공학과", "식품영양학과"});
        addCollegeGroup(majorPanel, "미래산업융합대학",
                new String[]{"경영학과", "패션산업학과", "디지털미디어학과", "지능정보보호학부", "소프트웨어융합학과", "데이터사이언스학과", "산업디자인학과"});

        JScrollPane scrollPane = new JScrollPane(majorPanel);
        scrollPane.setBounds(30, 335, 420, 180);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);

        JButton cancelBtn = createBtn("취소", GRAY);
        cancelBtn.setBounds(100, 540, 120, 50);
        cancelBtn.addActionListener(e -> dispose());
        add(cancelBtn);

        JButton okBtn = createBtn(currentItem == null ? "등록" : "수정", BROWN);
        okBtn.setBounds(260, 540, 120, 50);
        okBtn.addActionListener(e -> saveData());
        add(okBtn);
    }

    private void addCollegeGroup(JPanel p, String collegeName, String[] depts) {
        JLabel cLabel = new JLabel("■ " + collegeName);
        cLabel.setFont(uiFont.deriveFont(Font.BOLD, 14f));
        cLabel.setForeground(BROWN);
        cLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
        p.add(cLabel);

        for (String dept : depts) {
            JCheckBox cb = new JCheckBox(dept);
            cb.setFont(uiFont.deriveFont(13f));
            cb.setBackground(Color.WHITE);
            cb.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 0));
            majorCheckBoxes.add(cb);
            p.add(cb);
        }
    }

    private void addLabelAndField(String text, int x, int y, int w) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(16f));
        l.setForeground(BROWN);
        l.setBounds(x, y, w, 30);
        add(l);
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(16f));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    // ======================== 기존 아이템 수정 모드일 때 데이터 채우기 ========================
    private void loadData(Item item) {
        nameField.setText(item.getName());
        stockSpinner.setValue(item.getTotalStock());
        daySpinner.setValue(item.getMaxRentDays());

        String targetMajor = item.getTargetMajor();
        if (targetMajor != null && !targetMajor.isBlank() && !"전체 학과".equals(targetMajor)) {
            for (JCheckBox cb : majorCheckBoxes) {
                if (targetMajor.contains(cb.getText())) {
                    cb.setSelected(true);
                }
            }
        } else if ("전체 학과".equals(targetMajor)) {
            for (JCheckBox cb : majorCheckBoxes) cb.setSelected(true);
        }

        if (item.getImagePath() != null) {
            selectedImagePath = item.getImagePath();   // 🔥 기존 이미지 경로 저장
            ImageIcon icon = new ImageIcon(selectedImagePath);
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(img));
            imagePreview.setText("");
        }
    }

    // ======================== 저장 (등록 / 수정 공통) ========================
    private void saveData() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showMsgPopup("알림", "물품명을 입력해주세요.");
            return;
        }
        int stock = (int) stockSpinner.getValue();
        int days = (int) daySpinner.getValue();

        // 대상 학과 문자열 구성
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (JCheckBox cb : majorCheckBoxes) {
            if (cb.isSelected()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(cb.getText());
                count++;
            }
        }
        String majors;
        if (count == majorCheckBoxes.size()) {
            majors = "전체 학과";
        } else if (sb.length() == 0) {
            majors = "대상 없음";
        } else {
            majors = sb.toString();
        }

        boolean ok;
        if (currentItem == null) {
            // 🔥 신규 등록: 이미지 경로 포함해서 Item 생성
            Item newItem = new Item(name, stock, days, majors, selectedImagePath);
            ok = ItemDAO.getInstance().addItem(newItem);
            if (!ok) {
                showMsgPopup("오류", "물품 등록에 실패했습니다.");
                return;
            }
        } else {
            // 🔥 기존 아이템 수정
            currentItem.setName(name);
            currentItem.setTotalStock(stock);
            currentItem.setMaxRentDays(days);
            currentItem.setTargetMajor(majors);
            if (selectedImagePath != null) {  // 새로 선택했을 때만 덮어쓰기
                currentItem.setImagePath(selectedImagePath);
            }

            ok = ItemDAO.getInstance().updateItem(currentItem);
            if (!ok) {
                showMsgPopup("오류", "물품 수정에 실패했습니다.");
                return;
            }
        }

        parent.refreshList();
        dispose();
    }

    // ======================== 메시지 팝업 ========================
    private void showMsgPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
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
        panel.setLayout(null);
        dialog.add(panel);

        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setFont(uiFont.deriveFont(18f));
        l.setForeground(BROWN);
        l.setBounds(20, 80, 360, 30);
        panel.add(l);

        JButton ok = new JButton("확인");
        ok.setBounds(135, 170, 130, 45);
        ok.setBackground(BROWN);
        ok.setForeground(Color.WHITE);
        ok.addActionListener(e -> dialog.dispose());
        panel.add(ok);

        dialog.setVisible(true);
    }

    // ======================== 이미지 선택 ========================
    private void selectImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("물품 이미지 선택");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // 이미지 파일만 보이게 필터
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "이미지 파일 (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"
                )
        );

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();

        try {
            // 1) 저장할 디렉토리 (프로젝트 기준)
            java.nio.file.Path imagesDir = java.nio.file.Paths.get("images");
            if (!java.nio.file.Files.exists(imagesDir)) {
                java.nio.file.Files.createDirectories(imagesDir);
            }

            // 2) 파일명: 충돌 방지용으로 타임스탬프 붙이기
            String originalName = selectedFile.getName();
            String newFileName = System.currentTimeMillis() + "_" + originalName;

            java.nio.file.Path destPath = imagesDir.resolve(newFileName);

            // 3) 실제 파일 복사
            java.nio.file.Files.copy(
                    selectedFile.toPath(),
                    destPath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // 4) DB에 넣을 경로 (상대 경로만)
            selectedImagePath = "images/" + newFileName;

            // 5) 미리보기 세팅
            ImageIcon icon = new ImageIcon(destPath.toString());
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(img));
            imagePreview.setText("");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "이미지 파일을 저장하는 중 오류가 발생했습니다.\n" + ex.getMessage(),
                    "에러",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
