package beehub;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpaceReservationDAO {

    // ==========================
    //  DTO (마이페이지용 요약)
    // ==========================
    public static class ReservationSummary {
        public int reservationId;
        public int spaceId;
        public String roomName;
        public LocalDate reserveDate;
        public String timeSlot;   // "09:00~10:00"
        public String status;     // RESERVED, CANCELED, NO_SHOW ...

        public ReservationSummary(int reservationId,
                                  int spaceId,
                                  String roomName,
                                  LocalDate reserveDate,
                                  String timeSlot,
                                  String status) {
            this.reservationId = reservationId;
            this.spaceId = spaceId;
            this.roomName = roomName;
            this.reserveDate = reserveDate;
            this.timeSlot = timeSlot;
            this.status = status;
        }
    }

    // ==========================
    //  생성자 (public) + 싱글톤도 유지
    // ==========================
    private static final SpaceReservationDAO instance = new SpaceReservationDAO();

    public SpaceReservationDAO() {
    }

    public static SpaceReservationDAO getInstance() {
        return instance;
    }

    // ======================================================
    // 1) 마이페이지 - 로그인한 사용자의 공간 대여 기록 조회
    //    👉 모든 room_type 다 가져오도록 필터 제거
    // ======================================================
    public List<ReservationSummary> getReservationsByUser(String hakbun) {
        List<ReservationSummary> list = new ArrayList<>();

        String sql =
            "SELECT r.reservation_id, " +
            "       r.space_id, " +
            "       s.room_name, " +
            "       r.reserve_date, " +
            "       r.time_slot, " +
            "       r.status " +
            "FROM space_reservation r " +
            "JOIN space_info s ON r.space_id = s.space_id " +
            "WHERE r.hakbun = ? " +
            "ORDER BY r.reserve_date DESC, r.time_slot";

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
                            reservationId,
                            spaceId,
                            roomName,
                            date,
                            timeSlot,
                            status
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ======================================================
    // 2) 특정 공간+날짜에 이미 예약된 time_slot 목록 (시간 선택 막기용)
    // ======================================================
    public List<String> getBookedTimeSlots(Integer spaceId, LocalDate date) {
        List<String> result = new ArrayList<>();

        String sql =
            "SELECT time_slot " +
            "FROM space_reservation " +
            "WHERE space_id = ? " +
            "  AND reserve_date = ? " +
            "  AND status = 'RESERVED'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, spaceId);
            pstmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("time_slot"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // ======================================================
    // 3) 예약 INSERT (SpaceRentFrame에서 사용)
    //    selectedHours: 예) [10,11,12]
    //    👉 각 시간마다 "10:00~11:00", "11:00~12:00", "12:00~13:00"
    //       이렇게 각각 한 줄씩 INSERT (더 이상 10~13으로 묶지 않음)
    // ======================================================
    public boolean insertReservation(Integer spaceId,
                                     LocalDate date,
                                     ArrayList<Integer> selectedHours,
                                     String hakbun,
                                     int peopleCount) {
        if (spaceId == null || selectedHours == null || selectedHours.isEmpty()) return false;

        // 시간 정렬
        Collections.sort(selectedHours);

        // 1시간 단위로 slot 생성
        List<String> timeSlots = new ArrayList<>();
        for (int hour : selectedHours) {
            String slot = String.format("%02d:00~%02d:00", hour, hour + 1);
            timeSlots.add(slot);
        }

        // ---------- ✅ 중복 예약 체크 ----------
        List<String> booked = getBookedTimeSlots(spaceId, date);
        Set<String> bookedSet = new HashSet<>(booked);

        for (String slot : timeSlots) {
            if (bookedSet.contains(slot)) {
                System.out.println("[공간예약] 이미 예약된 시간과 겹칩니다: " + slot);
                return false;
            }
        }
        // -----------------------------------

        String sql =
            "INSERT INTO space_reservation " +
            "(space_id, reserve_date, time_slot, hakbun, people_count, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, 'RESERVED', NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (String slot : timeSlots) {
                pstmt.setInt(1, spaceId);
                pstmt.setDate(2, Date.valueOf(date));
                pstmt.setString(3, slot);
                pstmt.setString(4, hakbun);
                pstmt.setInt(5, peopleCount);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            return results.length > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======================================================
    // 4) 예약 취소 (마이페이지에서 사용)
    // ======================================================
    public boolean cancelReservation(int reservationId, String hakbun) {

        String sql =
            "UPDATE space_reservation " +
            "SET status = 'CANCELED' " +
            "WHERE reservation_id = ? " +
            "  AND hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            pstmt.setString(2, hakbun);

            int updated = pstmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======================================================
    // 5) 해당 날짜에 사용자가 이미 예약한 시간(시간 수) 계산
    // ======================================================
    public int getUsedHoursForUser(String hakbun, LocalDate date) {
        int totalHours = 0;

        String sql =
            "SELECT time_slot " +
            "FROM space_reservation " +
            "WHERE hakbun = ? " +
            "  AND reserve_date = ? " +
            "  AND status = 'RESERVED'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hakbun);
            pstmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String slot = rs.getString("time_slot");
                    try {
                        String[] parts = slot.split("~");
                        LocalTime start = LocalTime.parse(parts[0].trim());
                        LocalTime end   = LocalTime.parse(parts[1].trim());
                        int diff = end.getHour() - start.getHour();
                        if (diff > 0) totalHours += diff;
                    } catch (Exception ignore) {}
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalHours;
    }
}
