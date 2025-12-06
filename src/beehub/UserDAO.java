package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate; 


public class UserDAO {

    // 학생회 정보 리턴용 클래스
    public static class CouncilInfo {
        public String id;
        public String name; 
        public CouncilInfo(String id, String name) { 
            this.id = id; 
            this.name = name; 
        }
    }
    
    // ====================================================================
    // 📦 물품 대여 패널티 관리 메소드
    // ====================================================================

    public boolean updateRentalBanEndDate(String userId, LocalDate releaseDate) {
        String sql = "UPDATE members SET rental_ban_end_date = ? WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(releaseDate));
            pstmt.setString(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public LocalDate getRentalBanEndDate(String userId) {
        String sql = "SELECT rental_ban_end_date FROM members WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("rental_ban_end_date");
                    return (sqlDate != null) ? sqlDate.toLocalDate() : null;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean updatePassword(String hakbun, String newPw) {
        String sql = "UPDATE members SET pw = ? WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPw);
            pstmt.setString(2, hakbun);
            return pstmt.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearRentalBan(String userId) {
        String sql = "UPDATE members SET rental_ban_end_date = NULL WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ====================================================================
    // 👤 일반 로그인 + User 객체 리턴
    // ====================================================================

    public User loginAndGetUser(String id, String pw) {

        // ⭐ 닉네임 + 포인트 + role 포함
        String sql =
                "SELECT hakbun, pw, name, major, phone, nickname, point, role " +
                "FROM members WHERE hakbun = ? AND pw = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, pw);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    User u = new User();

                    u.setId(rs.getString("hakbun"));
                    u.setPassword(rs.getString("pw"));
                    u.setName(rs.getString("name"));
                    u.setDept(rs.getString("major"));
                    u.setPhone(rs.getString("phone"));

                    // ⭐ 닉네임
                    u.setNickname(rs.getString("nickname"));

                    // ⭐ 꿀 포인트
                    u.setPoints(rs.getInt("point"));

                    // ⭐ 권한
                    u.setRole(rs.getString("role"));

                    return u;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean checkUserLogin(String id, String pw) {
        String sql = "SELECT * FROM members WHERE hakbun = ? AND pw = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, pw);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public String findPassword(String name, String hakbun, String phone) {
        String sql = "SELECT pw FROM members WHERE name=? AND hakbun=? AND phone=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, hakbun);
            pstmt.setString(3, phone);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("pw");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // ====================================================================
    // 🏫 관리자 & 학생회 로그인
    // ====================================================================

    public boolean checkAdminLogin(String id, String pw) {
        return id.equals("admin") && pw.equals("1234");
    }

    public CouncilInfo getCouncilInfo(String id, String pw) {

        if (id.equals("council_soft") && pw.equals("1234"))
            return new CouncilInfo("council_soft", "소프트웨어융합학과");

        if (id.equals("council_general") && pw.equals("1234"))
            return new CouncilInfo("council_general", "총학생회");

        return null;
    }


    public boolean checkUserMatch(String id, String name) {

        String sql = "SELECT * FROM members WHERE hakbun = ? AND name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
