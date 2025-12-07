package beehub;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassTimetableDAO {

    private static ClassTimetableDAO instance = new ClassTimetableDAO();
    private ClassTimetableDAO() {}
    public static ClassTimetableDAO getInstance() { return instance; }

    // 강의 일정 DTO
    public static class ClassSchedule {
        public String buildingName;
        public String roomName;
        public int startHour;
        public int endHour;

        public ClassSchedule(String buildingName, String roomName, int startHour, int endHour) {
            this.buildingName = buildingName;
            this.roomName = roomName;
            this.startHour = startHour;
            this.endHour = endHour;
        }
    
    }

    public List<ClassSchedule> getSchedulesByDate(LocalDate date) {
        List<ClassSchedule> list = new ArrayList<>();

        String sql =
            "SELECT si.building_name, si.room_name, ct.start_hour, ct.end_hour " +
            "FROM class_timetable ct " +
            "JOIN space_info si ON ct.space_id = si.space_id " +
            "WHERE si.is_active = 1 " +
            "  AND si.room_type = '강의실' " +
            "  AND ct.class_date = ? " +          // ← 날짜 기준으로 조회!
            "ORDER BY si.building_name, si.room_name, ct.start_hour";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ClassSchedule(
                        rs.getString("building_name"),
                        rs.getString("room_name"),
                        rs.getInt("start_hour"),
                        rs.getInt("end_hour")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}
