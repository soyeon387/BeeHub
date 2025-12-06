package beehub;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpaceInfoDAO {

    // is_active = 1 인 공간만 조회
    public List<SpaceInfo> getActiveSpaces() {
        List<SpaceInfo> list = new ArrayList<>();

        String sql =
            "SELECT space_id, building_name, room_name, " +
            "       min_people, max_people, oper_time, room_type, is_active " +
            "FROM space_info " +
            "WHERE is_active = 1 " +
            "ORDER BY room_type, building_name, room_name";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                SpaceInfo s = new SpaceInfo(
                        rs.getInt("space_id"),
                        rs.getString("building_name"),
                        rs.getString("room_name"),
                        rs.getInt("min_people"),
                        rs.getInt("max_people"),
                        rs.getString("oper_time"),
                        rs.getString("room_type"),
                        rs.getInt("is_active") == 1
                );
                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
