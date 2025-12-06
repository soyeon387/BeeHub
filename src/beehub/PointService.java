package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PointService {

    /**
     * 꿀 포인트 변경 (+/-)
     * @param hakbun 학번
     * @param amount 변경할 포인트 (예: +10, -50)
     */
    public static void addPoint(String hakbun, int amount) {
        String sql = "UPDATE members SET point = point + ? WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);   // 1번째 ? -> 변경할 포인트
            pstmt.setString(2, hakbun); // 2번째 ? -> 학번

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
