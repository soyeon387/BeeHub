package beehub;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {

    // ============================================================
    // ✅ (중요) 같은 사람이 같은 물품을 이미 대여 중인지 체크
    // ============================================================
    private boolean hasActiveRental(String userId, int itemId) {
        String sql = "SELECT COUNT(*) " +
                     "FROM RENTAL " +
                     "WHERE renter_id = ? AND item_id = ? AND is_returned = 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setInt(2, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;  // 이미 대여 중이면 true
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 에러 발생 시 기본 false (대여 중이 아닌 것으로 간주)
        return false;
    }

    // ============================================================
    // ✅ 대여 생성 (재고 감소 → RENTAL INSERT)
    // ============================================================
    public boolean createRental(Item item, String userId,
                                LocalDate rentDate, LocalDate dueDate) throws SQLException {

        int itemId = item.getItemId();

        // ---------------------------------
        // 0️⃣ 동일 물품 중복 대여 방지 체크
        // ---------------------------------
        if (hasActiveRental(userId, itemId)) {
            return false;  // 이미 대여 중
        }

        // ---------------------------------
        // 1️⃣ 재고 감소 시도
        // ---------------------------------
        boolean stockUpdated = ItemDAO.getInstance().decreaseAvailableStock(itemId);
        if (!stockUpdated) {
            return false;  // 재고 없음 → 대여 불가
        }

        // ---------------------------------
        // 2️⃣ RENTAL INSERT
        // ---------------------------------
        String sql = "INSERT INTO RENTAL " +
                "(item_id, item_name, renter_id, renter_name, rent_date, due_date, return_date, is_returned) " +
                "VALUES (?, ?, ?, ?, ?, ?, NULL, 0)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            pstmt.setString(2, item.getName());
            pstmt.setString(3, userId);
            pstmt.setString(4, LoginSession.getUser().getName());  // 로그인한 사용자 이름
            pstmt.setDate(5, Date.valueOf(rentDate));
            pstmt.setDate(6, Date.valueOf(dueDate));

            pstmt.executeUpdate();
        }

        return true; // 성공!
    }
    
    

    // ============================================================
    // ✅ 특정 사용자 대여 내역 조회
    // ============================================================
    public List<Rental> getRentalsByUser(String userId) throws SQLException {
        List<Rental> list = new ArrayList<>();

        String sql = "SELECT * FROM RENTAL WHERE renter_id = ? ORDER BY rent_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Rental r = new Rental();
                    r.setRentalId(rs.getInt("rental_id"));
                    r.setItemId(rs.getInt("item_id"));
                    r.setItemName(rs.getString("item_name"));
                    r.setRenterId(rs.getString("renter_id"));
                    r.setRenterName(rs.getString("renter_name"));

                    Date rentDt = rs.getDate("rent_date");
                    if (rentDt != null) r.setRentDate(rentDt.toLocalDate());

                    Date dueDt = rs.getDate("due_date");
                    if (dueDt != null) r.setDueDate(dueDt.toLocalDate());

                    Date returnDt = rs.getDate("return_date");
                    if (returnDt != null) r.setReturnDate(returnDt.toLocalDate());

                    r.setReturned(rs.getBoolean("is_returned"));

                    list.add(r);
                }
            }
        }

        return list;
    }

}
