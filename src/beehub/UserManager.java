package beehub;

public class UserManager {

    // 현재 로그인한 사용자 객체 (세션)
    private static User currentUser = null;

    // 로그인 성공 시 User 객체 저장
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // 현재 로그인한 사용자 가져오기
    public static User getCurrentUser() {
        return currentUser;
    }

    // 로그아웃
    public static void logout() {
        currentUser = null;
    }
}
