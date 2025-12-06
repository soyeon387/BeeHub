package beehub;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SpaceReservationDAO {

    // ==============================
    // 🔹 마이페이지용 요약 DTO
    // ==============================
    public static class ReservationSummary {
        public int reservationId;
        public int spaceId;
        public String roomName;
        public LocalDate reserveDate;
        public String timeSlot;
        public String status;

        public ReservationSummary(int reservationId, int spaceId,
                                  String roomName, LocalDate reserveDate,
                                  String timeSlot, String status) {
            this.reservationId = reservationId;
            this.spaceId = spaceId;
            this.roomName = roomName;
            this.reserveDate = reserveDate;
            this.timeSlot = timeSlot;
            this.status = status;
        }
    }

    // ==============================
    // 🔹 특정 사용자 예약 목록 조회 (마이페이지용)
    // ==============================
    public List<ReservationSummary> getReservationsByUser(String hakbun) {
        List<ReservationSummary> list = new ArrayList<>();

        // ⚠️ space_info / room_name / space_id 는
        // 실제 DB 스키마에 맞게 테이블/컬럼명을 맞춰줘야 함
        String sql =
                "SELECT r.reservation_id, r.space_id, " +
                "       s.room_name, r.reserve_date, r.time_slot, r.status " +
                "FROM space_reservation r " +
                "LEFT JOIN space_info s ON r.space_id = s.space_id " +
                "WHERE r.hakbun = ? " +
                "ORDER BY r.reserve_date DESC, r.time_slot DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reservationId = rs.getInt("reservation_id");
                    int spaceId       = rs.getInt("space_id");
                    String roomName   = rs.getString("room_name");
                    LocalDate date    = rs.getDate("reserve_date").toLocalDate();
                    String timeSlot   = rs.getString("time_slot");
                    String status     = rs.getString("status");

                    list.add(new ReservationSummary(
                            reservationId, spaceId, roomName, date, timeSlot, status
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ==============================
    // 🔹 특정 공간+날짜에 이미 예약된 슬롯 목록
    // ==============================
    public List<String> getBookedTimeSlots(Integer spaceId, LocalDate date) {
        List<String> list = new ArrayList<>();

        String sql =
            "SELECT time_slot " +
            "FROM space_reservation " +
            "WHERE space_id = ? AND reserve_date = ? " +
            "AND (status IS NULL OR status <> 'CANCEL')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, spaceId);
            pstmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("time_slot")); // "09:00" 형식
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ==============================
    // 🔹 특정 날짜에 해당 학번이 이미 예약한 슬롯 수
    // ==============================
    public int getUsedHoursForUser(String hakbun, LocalDate date) {
        String sql =
            "SELECT COUNT(*) " +
            "FROM space_reservation " +
            "WHERE hakbun = ? AND reserve_date = ? " +
            "AND (status IS NULL OR status <> 'CANCEL')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hakbun);
            pstmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ==============================
    // 🔹 시간 슬롯들 저장 (슬롯당 한 줄 INSERT)
    // ==============================
    public void insertReservation(Integer spaceId, LocalDate date,
                                  ArrayList<Integer> hours, String hakbun) throws SQLException {

        String sql =
            "INSERT INTO space_reservation " +
            "(space_id, reserve_date, time_slot, hakbun, status, created_at) " +
            "VALUES (?, ?, ?, ?, 'CONFIRMED', NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (int h : hours) {
                String timeSlot = String.format("%02d:00", h); // 9 -> "09:00"

                pstmt.setInt(1, spaceId);
                pstmt.setDate(2, Date.valueOf(date));
                pstmt.setString(3, timeSlot);
                pstmt.setString(4, hakbun);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();
        }
    }

    // ==============================
    // 🔹 예약 취소
    // ==============================
    public boolean cancelReservation(int reservationId, String hakbun) {
        String sql =
            "UPDATE space_reservation " +
            "SET status = 'CANCEL' /*, cancelled_at = NOW()*/ " +
            "WHERE reservation_id = ? AND hakbun = ? " +
            "AND (status IS NULL OR status <> 'CANCEL')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            pstmt.setString(2, hakbun);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
