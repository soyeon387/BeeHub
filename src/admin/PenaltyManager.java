package admin;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import beehub.RentDAO;
import beehub.UserDAO;

/**
 * 패널티 관리 통합 클래스
 * - 공간 대여 경고 (메모리)
 * - 물품 대여 정지 (DB)
 */
public class PenaltyManager {

    // ================================
    // 🏢 공간 대여 패널티 (메모리 저장)
    // ================================
    // 아이디 : 경고 횟수
    private static Map<String, Integer> warningCounts = new HashMap<>();
    // 아이디 : 공간 대여 정지 해제 날짜
    private static Map<String, LocalDate> banEndDates = new HashMap<>();


    // ================================
    // 📦 물품 대여 패널티 (DB 저장)
    // ================================

    /**
     * 물품 연체 패널티 부여 → UserDAO를 통해 DB에 정지 해제 날짜 저장
     *
     * @param userId       학번
     * @param overdueDays  연체 일수 (연체 일수만큼 정지)
     */
    public static void setRentalBan(String userId, long overdueDays) {
        if (overdueDays <= 0) return;

        LocalDate releaseDate = LocalDate.now().plusDays(overdueDays);

        // UserDAO를 새로 생성해서 사용
        UserDAO userDAO = new UserDAO();
        userDAO.updateRentalBanEndDate(userId, releaseDate);

        System.out.println("[시스템] " + userId + "님 연체로 인해 " + releaseDate + "까지 대여 금지");
    }

    /**
     * 물품 대여 정지 남은 기간(일)을 조회
     */
    public static long getRentalBanDaysRemaining(String userId) {
        UserDAO userDAO = new UserDAO();
        LocalDate banUntil = userDAO.getRentalBanEndDate(userId);

        if (banUntil == null) return 0;

        LocalDate today = LocalDate.now();

        // 이미 정지 기간이 끝났으면 DB에서 해제
        if (today.isAfter(banUntil)) {
            userDAO.clearRentalBan(userId);
            return 0;
        }

        return ChronoUnit.DAYS.between(today, banUntil);
    }

    /**
     * 현재 DB 기준 물품 대여 중인 개수 조회
     */
    public static int getCurrentRentalCount(String userId) {
        // RentDAO는 너가 기존에 쓰던 싱글톤 패턴 그대로 사용
        return RentDAO.getInstance().getCurrentRentalCount(userId);
    }


    // ================================
    // 🏢 공간 대여 경고 시스템 (메모리)
    // ================================

    /**
     * 공간대여 미입실 → 경고 1회 부여
     *  - 경고 2회 이상이면 7일간 공간 예약 정지
     */
    public static void addWarning(String userId) {
        int count = warningCounts.getOrDefault(userId, 0) + 1;
        warningCounts.put(userId, count);

        System.out.println("[시스템] 경고 추가: " + userId + " (누적 " + count + "회)");

        // 경고 2회 이상 → 7일 정지
        if (count >= 2) {
            LocalDate banUntil = LocalDate.now().plusDays(7);
            banEndDates.put(userId, banUntil);

            System.out.println("⛔ " + userId + "님은 " + banUntil + "까지 공간 예약 불가");
        }
    }

    /**
     * 공간대여 정지 여부 확인
     */
    public static boolean isBanned(String userId) {
        if (!banEndDates.containsKey(userId)) {
            return false;
        }

        LocalDate banUntil = banEndDates.get(userId);
        LocalDate today = LocalDate.now();

        // 정지 기간이 끝났으면 자동 해제
        if (today.isAfter(banUntil)) {
            banEndDates.remove(userId);
            warningCounts.remove(userId);
            System.out.println("✅ " + userId + " 정지 해제됨");
            return false;
        }

        return true;
    }

    /**
     * 공간대여 정지 해제 날짜 조회
     */
    public static LocalDate getBanDate(String userId) {
        return banEndDates.get(userId);
    }

    /**
     * 공간대여 경고 횟수 조회
     */
    public static int getWarningCount(String userId) {
        return warningCounts.getOrDefault(userId, 0);
    }
}
