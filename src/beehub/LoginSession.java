package beehub;

public class LoginSession {

    // ------------------------------
    // 현재 로그인된 사용자 정보
    // ------------------------------
    private static Member user;

    public static void setUser(Member m) {
        user = m;
    }

    public static Member getUser() {
        return user;
    }

    // ------------------------------
    // 편의 메서드들 (UI에서 사용)
    // ------------------------------

    /** 로그인 사용자 이름(닉네임 우선) */
    public static String getDisplayName() {
        if (user == null) return "알 수 없음";

        String nick = user.getNickname();
        if (nick != null && !nick.trim().isEmpty()) return nick.trim();

        return user.getName();
    }

    /** 학교 학생회비 납부 여부 */
    public static boolean isSchoolFeePaid() {
        if (user == null) return false;
        return "Y".equalsIgnoreCase(user.getIsFeePaid());
    }

    /** 과 학생회비 납부 여부 */
    public static boolean isDeptFeePaid() {
        if (user == null) return false;
        return "Y".equalsIgnoreCase(user.getDeptFeeYn());
    }

    /** 학과 */
    public static String getUserMajor() {
        if (user == null) return "";
        return user.getMajor();
    }

    /** 학번 */
    public static String getHakbun() {
        if (user == null) return "";
        return user.getHakbun();
    }

    /** 역할(USER / COUNCIL 등) */
    public static String getRole() {
        if (user == null) return "";
        return user.getRole();
    }
}
