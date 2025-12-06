package beehub;

// 패널티(대여 정지) 관리용 매니저 - 일단 껍데기 버전
public class PenaltyManager {

    public PenaltyManager() {}

    // ✅ 대여 정지 여부 확인 (지금은 항상 false 리턴)
    public boolean isBanned(String hakbun) {
        // 나중에 DB 로직 연결해서 진짜로 구현하면 됨
        return false;
    }

    // ✅ 정지 해제일 / 정지 종료일 문자열로 리턴 (지금은 빈 문자열)
    public String getBanDate(String hakbun) {
        return "";
    }
}
