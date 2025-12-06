package beehub;

import java.io.Serializable;
import java.time.LocalDate;

// 대여 기록 하나를 나타내는 Value Object (VO)
public class Rent implements Serializable {
    private static final long serialVersionUID = 3L; 

    private int rentalId;    // DB PK: 대여 기록 고유 ID
    private int itemId;      // 물품 고유 ID (DB ITEM 테이블의 FK)
    private String itemName; 
    private String renterId;
    private String renterName;
    private LocalDate rentDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // 실제 반납일
    private boolean isReturned;

    public Rent() {}

    // 9개 인자를 받는 생성자 (DB에서 불러올 때 사용)
    public Rent(int rentalId, int itemId, String itemName, String renterId, String renterName, LocalDate rentDate, LocalDate dueDate, LocalDate returnDate, boolean isReturned) {
        this.rentalId = rentalId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.renterId = renterId;
        this.renterName = renterName;
        this.rentDate = rentDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.isReturned = isReturned;
    }
    
    // Getter 및 Setter는 반드시 모두 추가되어야 합니다.
    public int getRentalId() { return rentalId; }
    public void setRentalId(int rentalId) { this.rentalId = rentalId; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getRenterId() { return renterId; }
    public void setRenterId(String renterId) { this.renterId = renterId; }
    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }
    public LocalDate getRentDate() { return rentDate; }
    public void setRentDate(LocalDate rentDate) { this.rentDate = rentDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public boolean isReturned() { return isReturned; }
    public void setReturned(boolean isReturned) { this.isReturned = isReturned; }
}