package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// RentManager의 역할을 대체하는 데이터 접근 객체 (DAO)
public class RentDAO {

    // Singleton Pattern
    private static RentDAO instance = new RentDAO();
    private RentDAO() {}
    public static RentDAO getInstance() { return instance; }

    // ResultSet에서 Rent 객체로 데이터를 매핑
    private Rent getRentFromResultSet(ResultSet rs) throws SQLException {
        return new Rent(
            rs.getInt("rental_id"),
            rs.getInt("item_id"), // DB에서는 int item_id 사용
            rs.getString("item_name"),
            rs.getString("renter_id"),
            rs.getString("renter_name"),
            rs.getDate("rent_date").toLocalDate(),
            rs.getDate("due_date").toLocalDate(),
            // return_date는 NULL일 수 있으므로 처리
            rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
            rs.getBoolean("is_returned")
        );
    }

    // 1. 대여 기록 추가 (addRental)
    public boolean addRental(Rent data) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO RENTAL (item_id, item_name, renter_id, renter_name, rent_date, due_date, is_returned) "
                   + "VALUES (?, ?, ?, ?, ?, ?, FALSE)"; // is_returned는 기본 FALSE
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, data.getItemId());
            pstmt.setString(2, data.getItemName());
            pstmt.setString(3, data.getRenterId());
            pstmt.setString(4, data.getRenterName());
            pstmt.setDate(5, java.sql.Date.valueOf(data.getRentDate()));
            pstmt.setDate(6, java.sql.Date.valueOf(data.getDueDate()));
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("addRental DB 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt);
        }
    }

    // 2. 모든 대여 기록 조회 (getAllRentals)
    public List<Rent> getAllRentals() {
        List<Rent> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM RENTAL ORDER BY rental_id DESC"; 

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                list.add(getRentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("getAllRentals DB 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    // 3. 사용자의 미반납 대여 건수 조회 (getCurrentRentalCount)
    // ItemDetailFrame에서 PenaltyManager가 호출하던 로직을 DB에서 가져옴
    public int getCurrentRentalCount(String renterId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM RENTAL WHERE renter_id = ? AND is_returned = FALSE";
        int count = 0;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, renterId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("getCurrentRentalCount DB 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return count;
    }

    // 4. 물품 반납 처리 (returnItem)
    public boolean returnItem(int rentalId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE RENTAL SET is_returned = TRUE, return_date = ? WHERE rental_id = ? AND is_returned = FALSE";
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now())); // 현재 날짜로 반납일 기록
            pstmt.setInt(2, rentalId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("returnItem DB 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt);
        }
    }
    
    // 5. [핵심] 해당 물품 ID로 현재 대여 중(미반납)인 기록이 있는지 확인 (isItemCurrentlyRented)
    // 이 메서드는 기존 RentManager에 있었지만, DB 기반에서는 거의 사용되지 않고 재고(ItemDAO)로 대체됩니다.
    // 하지만 Admin 기능에서 필요할 수 있으므로 유지합니다.
    public boolean isItemCurrentlyRented(int itemId) {
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         String sql = "SELECT 1 FROM RENTAL WHERE item_id = ? AND is_returned = FALSE LIMIT 1";

         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setInt(1, itemId);
             rs = pstmt.executeQuery();
             
             return rs.next(); // 결과가 있으면 true (대여 중)
         } catch (SQLException e) {
             System.err.println("isItemCurrentlyRented DB 오류: " + e.getMessage());
             e.printStackTrace();
             return false;
         } finally {
             DBUtil.close(conn, pstmt, rs);
         }
     }
}