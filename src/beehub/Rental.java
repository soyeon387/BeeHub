package beehub;

import java.time.LocalDate;

public class Rental {

    private int rentalId;
    private int itemId;
    private String itemName;
    private String renterId;
    private String renterName;

    private LocalDate rentDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    private boolean returned;

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

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }
}
