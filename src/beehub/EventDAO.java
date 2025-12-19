package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

	public List<MyActivityDTO> getMyActivityList(String hakbun) {
	    List<MyActivityDTO> list = new ArrayList<>();

	    String sql = "SELECT e.event_name, e.event_date, e.location " +
	                 "FROM event_participation ep " +
	                 "JOIN events e ON ep.event_id = e.event_id " +
	                 "WHERE ep.participant_hakbun = ? " +
	                 "ORDER BY e.event_date DESC";

	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, hakbun);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	            while (rs.next()) {
	                String dateStr = "-";
	                try { dateStr = sdf.format(rs.getTimestamp("event_date")); } catch (Exception ignore) {}

	                list.add(new MyActivityDTO(
	                        rs.getString("event_name"),
	                        dateStr,
	                        rs.getString("location")
	                ));
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return list;
	}

}