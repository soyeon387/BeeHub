package beehub;

import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 2L; 
    
    // DB 필드에 대응하는 필드 목록
    private int itemId; 
    private int totalStock;
    private int availableStock;
    private boolean isActive;

    // 기존 필드
    private String name;
    private int maxRentDays; 
    private String targetMajor;
    private String imagePath;

    public Item() {}

    public Item(String name, int totalStock, int maxRentDays,
                String targetMajor, String imagePath) {
        this.name = name;
        this.totalStock = totalStock;
        this.availableStock = totalStock;  // 처음에는 총 재고 = 대여 가능 재고
        this.maxRentDays = maxRentDays;
        this.targetMajor = targetMajor;
        this.imagePath = imagePath;
        this.isActive = true;              // 기본값: 활성
    }

    // ===================================
    // Getter 및 Setter
    // ===================================

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public int getMaxRentDays() { return maxRentDays; }
    public void setMaxRentDays(int maxRentDays) { this.maxRentDays = maxRentDays; }

    public String getTargetMajor() { return targetMajor; }
    public void setTargetMajor(String targetMajor) { this.targetMajor = targetMajor; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}
