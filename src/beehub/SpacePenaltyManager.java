package beehub;

import java.sql.*;
import java.time.LocalDate;

/**
 * 공간대여 전용 패널티(경고/정지) 관리자 - DB 연동
 *
 * members:
 *  - space_warning_count INT
 *  - space_ban_end_date DATE
 */
public class SpacePenaltyManager {

    private SpacePenaltyManager() {}

    public static boolean isBanned(String hakbun) {
        LocalDate end = getBanEndDate(hakbun);
        if (end == null) return false;

        LocalDate today = LocalDate.now();
        if (today.isAfter(end)) {
            clearBan(hakbun);
            return false;
        }
        return true;
    }

    public static String getBanDate(String hakbun) {
        LocalDate end = getBanEndDate(hakbun);
        if (end == null) return "";
        if (LocalDate.now().isAfter(end)) {
            clearBan(hakbun);
            return "";
        }
        return end.toString(); // YYYY-MM-DD
    }

    public static int getWarningCount(String hakbun) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT space_warning_count FROM members WHERE hakbun = ?")) {
            ps.setString(1, hakbun);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 미입실 처리 시 호출: 경고 +1, 경고 2회면 7일 정지 + 경고 0으로 리셋
    public static void addWarning(String hakbun) {
        if (isBanned(hakbun)) return; // 이미 정지면 추가 누적 안 함(정책)

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int current = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT space_warning_count FROM members WHERE hakbun = ?")) {
                ps.setString(1, hakbun);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) current = rs.getInt(1);
                }
            }

            int next = current + 1;

            if (next >= 2) {
                LocalDate banUntil = LocalDate.now().plusDays(7);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE members SET space_ban_end_date = ?, space_warning_count = 0 WHERE hakbun = ?")) {
                    ps.setDate(1, Date.valueOf(banUntil));
                    ps.setString(2, hakbun);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE members SET space_warning_count = ? WHERE hakbun = ?")) {
                    ps.setInt(1, next);
                    ps.setString(2, hakbun);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    private static LocalDate getBanEndDate(String hakbun) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT space_ban_end_date FROM members WHERE hakbun = ?")) {
            ps.setString(1, hakbun);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate(1);
                    return d == null ? null : d.toLocalDate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearBan(String hakbun) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE members SET space_ban_end_date = NULL WHERE hakbun = ?")) {
            ps.setString(1, hakbun);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
