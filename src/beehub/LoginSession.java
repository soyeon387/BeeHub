package beehub;

public class LoginSession {

    private static Member user;

    public static void setUser(Member m) {
        user = m;
    }

    public static Member getUser() {
        return user;
    }
	private String getDisplayName(Member m) {
		if (m == null) return "알 수 없음";
		String nick = m.getNickname();
		if (nick != null && !nick.trim().isEmpty()) {
			return nick.trim();          // 닉네임 설정돼 있으면 닉네임
		}
		return m.getName();              // 아니면 실명
	}
}
