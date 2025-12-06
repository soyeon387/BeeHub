package beehub;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SpaceReservation {
    private int reservationId;
    private int spaceId;
    private LocalDate reserveDate;
    private String timeSlot;     // "09:00~10:00"
    private String hakbun;
    private String status;       // RESERVED, CANCELLED ...
    private LocalDateTime createdAt;

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getSpaceId() { return spaceId; }
    public void setSpaceId(int spaceId) { this.spaceId = spaceId; }

    public LocalDate getReserveDate() { return reserveDate; }
    public void setReserveDate(LocalDate reserveDate) { this.reserveDate = reserveDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getHakbun() { return hakbun; }
    public void setHakbun(String hakbun) { this.hakbun = hakbun; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
