package beehub;

import beehub.DBUtil;   // ✅ 수정
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class ItemDAO {

    // Singleton Pattern 적용
    private static ItemDAO instance = new ItemDAO();
    private ItemDAO() {}
    public static ItemDAO getInstance() { return instance; }

    private Item getItemFromResultSet(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setName(rs.getString("name"));
        item.setTotalStock(rs.getInt("total_stock"));
        item.setAvailableStock(rs.getInt("available_stock"));
        item.setMaxRentDays(rs.getInt("max_rent_days"));
        item.setTargetMajor(rs.getString("target_major"));
        item.setImagePath(rs.getString("image_path"));
        item.setActive(rs.getBoolean("is_active"));
        return item;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM ITEM WHERE is_active = TRUE ORDER BY name"; 

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                items.add(getItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);   // ✅ beehub.DBUtil.close
        }
        return items;
    }
    
    public boolean decreaseAvailableStock(int itemId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE ITEM " +
                "SET available_stock = available_stock - 1 " +
                "WHERE item_id = ? AND available_stock > 0";
        int rowsAffected = 0;
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itemId);
            rowsAffected = pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);   // ✅
        }
        return rowsAffected > 0;
    }

    // 🚨 [추가] 물품 반납 시 재고 증가 메서드
    public boolean increaseAvailableStock(int itemId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE ITEM " +
                "SET available_stock = available_stock + 1 " +
                "WHERE item_id = ? AND available_stock < total_stock";        
        try {
            conn = DBUtil.getConnection();   // ✅
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itemId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt);       // ✅
        }
    }
 // ✅ 단일 물품 조회 (관리 화면에서 수정할 때 필요)
    public Item getItemById(int itemId) {
        String sql = "SELECT * FROM ITEM WHERE item_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getItemFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ 모든 물품 조회 (비활성 포함, 관리자 화면용)
    public List<Item> getAllItemsAdmin() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM ITEM ORDER BY name";  // is_active 조건 X

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                items.add(getItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    // ✅ 물품 추가 (INSERT)
    //  - total_stock, max_rent_days, target_major, image_path, is_active 사용
    //  - available_stock 은 처음에 total_stock 과 동일하게 세팅
    public boolean addItem(Item item) {
        String sql = "INSERT INTO ITEM " +
                     "(name, total_stock, available_stock, max_rent_days, target_major, image_path, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getTotalStock());
            pstmt.setInt(3, item.getTotalStock()); // 처음엔 available = total
            pstmt.setInt(4, item.getMaxRentDays());
            pstmt.setString(5, item.getTargetMajor());
            pstmt.setString(6, item.getImagePath());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 생성된 item_id 를 DTO에도 넣어주기
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setItemId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ 물품 정보 수정 (UPDATE)
    //  - 이름, 총 재고, 대여일수, 대상학과, 이미지, 활성 여부 수정
    //  - total_stock 을 줄였을 때 available_stock 이 너무 큰 경우 등은
//        나중에 별도 로직으로 다듬어도 됨(지금은 그대로 둠)
    public boolean updateItem(Item item) {
        String sql = "UPDATE ITEM SET " +
                     "name = ?, " +
                     "total_stock = ?, " +
                     "max_rent_days = ?, " +
                     "target_major = ?, " +
                     "image_path = ?, " +
                     "is_active = ? " +
                     "WHERE item_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getTotalStock());
            pstmt.setInt(3, item.getMaxRentDays());
            pstmt.setString(4, item.getTargetMajor());
            pstmt.setString(5, item.getImagePath());
            pstmt.setBoolean(6, item.isActive());
            pstmt.setInt(7, item.getItemId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ 물품 비활성화 (삭제처럼 쓰기)
    //  - 실제 DELETE 대신 is_active = 0
    public boolean deactivateItem(int itemId) {
        String sql = "UPDATE ITEM SET is_active = 0 WHERE item_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ 다시 활성화
    public boolean activateItem(int itemId) {
        String sql = "UPDATE ITEM SET is_active = 1 WHERE item_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
