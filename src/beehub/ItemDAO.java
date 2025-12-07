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

    // ✅ 활성화된 물품만 (사용자용)
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
            DBUtil.close(conn, pstmt, rs);
        }
        return items;
    }

    // ✅ 대여 시 재고 -1
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
            DBUtil.close(conn, pstmt);
        }
        return rowsAffected > 0;
    }

    // ✅ 물품 반납 시 재고 +1
    public boolean increaseAvailableStock(int itemId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE ITEM " +
                "SET available_stock = available_stock + 1 " +
                "WHERE item_id = ? AND available_stock < total_stock";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itemId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt);
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

    // ✅ 물품 비활성화 (이전 방식 - 지금은 안 써도 됨)
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

    // ✅ 다시 활성화 (이전 방식 - 지금은 안 써도 됨)
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

    // ✅ [추가] 현재 누가 빌리고 있는지 체크
    //  rental 테이블에서 is_returned = 0 이 하나라도 있으면 "대여 중"
    public boolean isItemRented(int itemId) {
        String sql = "SELECT COUNT(*) FROM rental WHERE item_id = ? AND is_returned = 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 에러가 나면 안전하게 "대여 중" 으로 간주해서 삭제 막기
        return true;
    }

    // ✅ [추가] 실제 물품 삭제 (DELETE)
    //  - AdminItemManageFrame 에서 isItemRented() 검사 후 호출
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM ITEM WHERE item_id = ?";

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
