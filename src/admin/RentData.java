package admin;

public class RentData {
    public String itemName;
    public String renterId;
    public String dueDate;
    public boolean isReturned;

    public RentData() {}

    public RentData(String itemName, String renterId, String dueDate, boolean isReturned) {
        this.itemName = itemName;
        this.renterId = renterId;
        this.dueDate = dueDate;
        this.isReturned = isReturned;
    }
}
