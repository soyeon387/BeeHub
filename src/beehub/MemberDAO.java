package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MemberDAO {

    // public 생성자 (외부에서 new MemberDAO() 가능하도록)
    public MemberDAO() {}

    /** ✅ 로그인 : 학교/과 회비까지 모두 읽어오기 */
    public Member login(String hakbun, String pw) {

        String sql =
                "SELECT hakbun, pw, name, nickname, major, phone, " +
                "is_fee_paid, dept_fee_yn, point " +
                "FROM members WHERE hakbun = ? AND pw = ?";

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
                    m.setNickname(rs.getString("nickname"));
                    m.setMajor(rs.getString("major"));
                    m.setPhone(rs.getString("phone"));

                    // 🔹 회비 정보
                    m.setIsFeePaid(rs.getString("is_fee_paid"));   // 학교 회비 Y/N
                    m.setDeptFeeYn(rs.getString("dept_fee_yn"));   // 과 회비 Y/N

                    m.setPoint(rs.getInt("point"));
                    return m;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    /** ✅ 학번으로 회원 조회 (회비 조건 체크용) */
    public Member findByHakbun(String hakbun) {

        String sql =
                "SELECT hakbun, pw, name, nickname, major, phone, " +
                "is_fee_paid, dept_fee_yn, point " +
                "FROM members WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Member m = new Member();
                    m.setHakbun(rs.getString("hakbun"));
                    m.setPw(rs.getString("pw"));
                    m.setName(rs.getString("name"));
                    m.setNickname(rs.getString("nickname"));
                    m.setMajor(rs.getString("major"));
                    m.setPhone(rs.getString("phone"));

                    // 🔹 회비 정보 다시 읽기
                    m.setIsFeePaid(rs.getString("is_fee_paid"));
                    m.setDeptFeeYn(rs.getString("dept_fee_yn"));

                    m.setPoint(rs.getInt("point"));
                    return m;
                }
            }

        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    /** 닉네임 변경 */
    public boolean updateNickname(String hakbun, String nickname) {
        String sql = "UPDATE members SET nickname = ? WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, hakbun);
            return pstmt.executeUpdate() == 1;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    /** 비밀번호 변경 */
    public boolean updatePassword(String hakbun, String newPw) {
        String sql = "UPDATE members SET pw = ? WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPw);
            pstmt.setString(2, hakbun);
            return pstmt.executeUpdate() == 1;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    /** 포인트 변경 */
    public boolean updatePoint(String hakbun, int point) {
        String sql = "UPDATE members SET point = ? WHERE hakbun = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, point);
            pstmt.setString(2, hakbun);
            return pstmt.executeUpdate() == 1;

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }
}
