package beehub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.io.InputStream;

public class LoadingFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color BG_YELLOW   = new Color(255, 250, 200); // 따뜻한 크림색
    private static final Color DOT_COLOR   = new Color(245, 230, 200); // 배경 격자 도트
    
    private static final Color BROWN       = new Color(90, 50, 20);    // 진한 갈색 (글씨/테두리)
    
    // 꿀 색상 (단색 적용)
    private static final Color HONEY_SOLID = new Color(255, 195, 40);  // 진한 황금색
    
    // 꿀단지 유리 (도트용)
    private static final Color JAR_HIGHLIGHT = new Color(255, 255, 255, 180); 
    private static final Color JAR_SHADOW    = new Color(230, 210, 190);

    // 라벨 색상
    private static final Color LABEL_BG      = new Color(255, 245, 230); 
    private static final Color LABEL_BORDER  = new Color(200, 170, 130); 

    // 꽃 색상 (파스텔 톤)
    private static final Color FLOWER_PINK   = new Color(255, 200, 200);
    private static final Color FLOWER_WHITE  = new Color(255, 255, 245);
    private static final Color FLOWER_BLUE   = new Color(210, 240, 250);
    private static final Color FLOWER_CENTER = new Color(255, 240, 100);

    private int progress = 0; 
    private Timer timer;
    private static Font uiFont;
    private static Font labelFont; 

    static {
        try {
            InputStream is = LoadingFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                uiFont = new Font("맑은 고딕", Font.BOLD, 12);
                labelFont = new Font("맑은 고딕", Font.BOLD, 16);
            } else {
                Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                uiFont = base.deriveFont(12f);
                labelFont = base.deriveFont(20f); 
            }
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.BOLD, 12);
        }
    }

    public LoadingFrame() {
        setUndecorated(true);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setBackground(BG_YELLOW);
        setLayout(new BorderLayout());

        AnimationPanel panel = new AnimationPanel();
        add(panel, BorderLayout.CENTER);

        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progress++;
                panel.repaint(); 

                if (progress >= 120) { 
                    timer.stop();
                    openLoginFrame();
                }
            }
        });
        timer.start();
        
        setVisible(true);
    }

    private void openLoginFrame() {
        dispose(); 
        new LoginFrame(); 
    }

    // ===============================
    // 🖌️ 렌더링 패널
    // ===============================
    class AnimationPanel extends JPanel {
        
        private final int PIXEL_SIZE = 5; 

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();

            // 1. 배경 (격자 무늬)
            drawPixelBackground(g2, w, h);

            // 2. 꿀단지 좌표 계산
            int rawJarW = 150; 
            int rawJarH = 170; 
            
            int jarX = ((w - rawJarW) / 2 / PIXEL_SIZE) * PIXEL_SIZE;
            int jarY = ((h - rawJarH) / 2 / PIXEL_SIZE) * PIXEL_SIZE - 10;
            int jarW = (rawJarW / PIXEL_SIZE) * PIXEL_SIZE;
            int jarH = (rawJarH / PIXEL_SIZE) * PIXEL_SIZE;

            // 3. 꿀단지 모양
            Path2D jarShape = createOriginalJarShape(jarX, jarY, jarW, jarH);
            
            // 4. 내부 꿀 (단색)
            drawSmoothHoney(g2, jarShape, jarX, jarY, jarW, jarH);

            // 5. 꿀단지 몸통 (도트 테두리)
            drawPixelatedJarContainer(g2, jarShape, jarX, jarY, jarW, jarH);
            
            // 6. 가운데 긴 라벨
            drawLongLabel(g2, jarX, jarY, jarW, jarH);

            // 7. 도트 꽃 배경 (넓게 퍼짐)
            long time = System.currentTimeMillis();
            
            // 네 모서리 부근
            drawDotFlower(g2, jarX - 90, jarY - 50, FLOWER_BLUE, time, 0);       
            drawDotFlower(g2, jarX + jarW + 80, jarY - 40, FLOWER_PINK, time, 200);    
            drawDotFlower(g2, jarX - 70, jarY + jarH + 50, FLOWER_WHITE, time, 400);   
            drawDotFlower(g2, jarX + jarW + 70, jarY + jarH + 40, FLOWER_BLUE, time, 600); 

            // 사이사이 빈 공간
            drawDotFlower(g2, jarX + 20, jarY - 80, FLOWER_WHITE, time, 100);     
            drawDotFlower(g2, jarX + jarW + 95, jarY + jarH / 2, FLOWER_WHITE, time, 300); 
            drawDotFlower(g2, jarX - 105, jarY + jarH / 3, FLOWER_PINK, time, 500);  
            drawDotFlower(g2, jarX + jarW / 2, jarY + jarH + 70, FLOWER_BLUE, time, 700);  

            // 8. 텍스트 & 외곽선
            drawText(g2, w, jarY + jarH);
            
            g2.setColor(BROWN);
            g2.setStroke(new BasicStroke(6f));
            g2.drawRect(0, 0, w, h);
        }

        // 도트 꽃 그리기
        private void drawDotFlower(Graphics2D g2, int x, int y, Color color, long time, int offset) {
            double anim = Math.sin((time + offset) / 400.0) * 5.0; 
            int dy = (int) anim;
            
            int dx = (x / PIXEL_SIZE) * PIXEL_SIZE;
            int finalY = ((y + dy) / PIXEL_SIZE) * PIXEL_SIZE;
            
            int p = 6; // 꽃 도트 크기 (7픽셀)

            g2.setColor(color);
            g2.fillRect(dx, finalY - p, p, p);      
            g2.fillRect(dx, finalY + p, p, p);      
            g2.fillRect(dx - p, finalY, p, p);      
            g2.fillRect(dx + p, finalY, p, p);      
            
            g2.setColor(FLOWER_CENTER);
            g2.fillRect(dx, finalY, p, p);
        }

        private void drawLongLabel(Graphics2D g2, int x, int y, int w, int h) {
            int labelW = w - 20; 
            int labelH = 35;     
            int lx = x + (w - labelW) / 2;
            int ly = y + (h - labelH) / 2;

            g2.setColor(LABEL_BG);
            g2.fillRect(lx, ly, labelW, labelH);
            
            g2.setColor(LABEL_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(lx, ly, labelW, labelH);

            g2.setColor(new Color(230, 200, 160));
            g2.drawRect(lx + 3, ly + 3, labelW - 6, labelH - 6);

            g2.setFont(labelFont);
            g2.setColor(BROWN);
            String labelText = "HONEY"; 
            FontMetrics fm = g2.getFontMetrics();
            int tx = lx + (labelW - fm.stringWidth(labelText)) / 2;
            int ty = ly + (labelH - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(labelText, tx, ty);
            
            g2.setColor(BROWN);
            g2.fillOval(lx + 5, ly + labelH/2 - 2, 4, 4);
            g2.fillOval(lx + labelW - 9, ly + labelH/2 - 2, 4, 4);
        }

        private void drawSmoothHoney(Graphics2D g2, Path2D shape, int x, int y, int w, int h) {
            int actualProgress = Math.min(progress, 100);
            double ratio = actualProgress / 100.0;
            int honeyHeight = (int) (h * ratio);
            int honeyLevelY = y + h - honeyHeight; 

            Shape originalClip = g2.getClip();
            g2.setClip(shape);

            if (honeyHeight > 0) {
                g2.setColor(HONEY_SOLID);
                g2.fillRect(x - 10, honeyLevelY, w + 20, honeyHeight + 20);

                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillOval(x + 10, honeyLevelY + 5, w - 20, 10);
                
                long time = System.currentTimeMillis();
                if (honeyHeight > 50) drawSparkle(g2, x + w/2 - 20, honeyLevelY + 30, time, 1.0);
                if (honeyHeight > 100) drawSparkle(g2, x + w/2 + 30, honeyLevelY + 50, time + 200, 0.8);
            }
            g2.setClip(originalClip);
        }

        private void drawSparkle(Graphics2D g2, int cx, int cy, long time, double scale) {
            g2.setColor(Color.WHITE);
            double anim = (Math.sin(time / 200.0) + 1) / 2.0; 
            double s = scale * (0.5 + 0.5 * anim); 
            int size = (int)(14 * s);
            int half = size / 2;
            
            GeneralPath star = new GeneralPath();
            star.moveTo(cx, cy - half);
            star.quadTo(cx, cy, cx + half, cy);
            star.quadTo(cx, cy, cx, cy + half);
            star.quadTo(cx, cy, cx - half, cy);
            star.quadTo(cx, cy, cx, cy - half);
            star.closePath();
        }

        private void drawPixelatedJarContainer(Graphics2D g2, Path2D shape, int x, int y, int w, int h) {
            int actualProgress = Math.min(progress, 100);
            int honeyHeight = (int) (h * (actualProgress / 100.0));
            int honeyLevelY = y + h - honeyHeight; 

            int padding = 40; 
            for (int py = y; py < y + h + PIXEL_SIZE; py += PIXEL_SIZE) {
                for (int px = x - padding; px < x + w + padding; px += PIXEL_SIZE) {
                    
                    int centerX = px + PIXEL_SIZE / 2;
                    int centerY = py + PIXEL_SIZE / 2;

                    if (shape.contains(centerX, centerY)) {
                        boolean isBorder = !shape.contains(centerX - PIXEL_SIZE, centerY) ||
                                           !shape.contains(centerX + PIXEL_SIZE, centerY) ||
                                           !shape.contains(centerX, centerY - PIXEL_SIZE) ||
                                           !shape.contains(centerX, centerY + PIXEL_SIZE);

                        if (isBorder) {
                            g2.setColor(BROWN);
                            g2.fillRect(px, py, PIXEL_SIZE, PIXEL_SIZE);
                        } else {
                            if (py < honeyLevelY) {
                                if (px < x + 40 && py > y + 40 && py < y + 90) {
                                    g2.setColor(JAR_HIGHLIGHT); 
                                    g2.fillRect(px, py, PIXEL_SIZE, PIXEL_SIZE);
                                } else if (px > x + w - 30) {
                                    g2.setColor(JAR_SHADOW); 
                                    g2.fillRect(px, py, PIXEL_SIZE, PIXEL_SIZE);
                                }
                            }
                        }
                    }
                }
            }
            
            int rimY = y - PIXEL_SIZE;
            int rimH = PIXEL_SIZE * 3;
            int rimX = (x + 15) / PIXEL_SIZE * PIXEL_SIZE;
            int rimW = (w - 30) / PIXEL_SIZE * PIXEL_SIZE;

            g2.setColor(BROWN);
            g2.fillRect(rimX, rimY, rimW, rimH); 
            g2.setColor(new Color(250, 240, 220));
            g2.fillRect(rimX + PIXEL_SIZE, rimY + PIXEL_SIZE, rimW - PIXEL_SIZE*2, PIXEL_SIZE);
        }

        private Path2D createOriginalJarShape(int x, int y, int w, int h) {
            Path2D s = new Path2D.Double();
            s.moveTo(x + 20, y); 
            s.lineTo(x + w - 20, y);
            s.curveTo(x + w + 20, y + 40, x + w + 20, y + h - 20, x + w - 30, y + h);    
            s.lineTo(x + 30, y + h);
            s.curveTo(x - 20, y + h - 20, x - 20, y + 40, x + 20, y);
            s.closePath();
            return s;
        }

        private void drawPixelBackground(Graphics2D g2, int w, int h) {
            g2.setColor(BG_YELLOW);
            g2.fillRect(0, 0, w, h);
            g2.setColor(DOT_COLOR);
            int dotSize = PIXEL_SIZE; 
            int gap = PIXEL_SIZE * 6; 
            for (int y = 0; y < h; y += gap) {
                for (int x = 0; x < w; x += gap) {
                    g2.fillRect(x, y, dotSize, dotSize); 
                }
            }
        }

        // [수정] 외곽선 제거 및 일반 텍스트 드로잉
        private void drawText(Graphics2D g2, int w, int basePath) {
            g2.setFont(uiFont.deriveFont(20f));
            g2.setColor(BROWN); // 깔끔한 갈색 글씨

            String text = "서울여대 꿀단지";
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(text)) / 2;
            int textY = basePath + 60;
            
            g2.drawString(text, textX, textY);

            // 로딩 텍스트
            g2.setFont(uiFont.deriveFont(14f));
            String perText = "Loading... " + Math.min(progress, 100) + "%";
            g2.drawString(perText, (w - g2.getFontMetrics().stringWidth(perText)) / 2, basePath + 90);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoadingFrame::new);
    }
}