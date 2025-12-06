package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MemberDAO {
    public Member login(String hakbun, String pw) {
        String sql = "SELECT hakbun, pw, name, nickname, major, phone, point " +
                     "FROM members " +
                     "WHERE hakbun = ? AND pw = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hakbun);
            pstmt.setString(2, pw);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Member m = new Member();
                    m.setHakbun(rs.getString("hakbun"));
                    m.setPw(rs.getString("pw"));
                    m.setName(rs.getString("name"));
                    m.setNickname(rs.getString("nickname"));  // ✅ 닉네임
                    m.setMajor(rs.getString("major"));
                    m.setPhone(rs.getString("phone"));
                    m.setPoint(rs.getInt("point"));           // ✅ 포인트

                    return m;
                } else {
                    return null; // 로그인 실패
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean updateNickname(String hakbun, String nickname) {
        String sql = "UPDATE members SET nickname = ? WHERE hakbun = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, hakbun);
            return pstmt.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public boolean updatePoint(String hakbun, int point) {
        String sql = "UPDATE member SET point = ? WHERE hakbun = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, point);
            pstmt.setString(2, hakbun);
            return pstmt.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
