// 파일명: LotteryManager.java
package admin;

import beehub.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class LotteryManager {

    // 한 번 응모할 때 기본 차감 꿀
    public static final int DEFAULT_COST_POINTS = 100;

    // 🔹 MyPageFrame에서 부르는 2개짜리 버전
    public static boolean applyUsingPoints(int roundId, String hakbun) {
        return applyUsingPoints(roundId, hakbun, DEFAULT_COST_POINTS);
    }

    // ===================== DTO =====================

    public static class LotteryRound {
        public int roundId;
        public String name;               // 회차 이름 (화면용)
        public String prizeName;          // 경품 이름
        public int winnerCount;           // 당첨 인원 수
        public String announcementDate;   // 발표일 (yyyy-MM-dd)
        public String applicationPeriod;  // 응모기간 텍스트
        public String pickupLocation;     // 수령 장소
        public String pickupPeriod;       // 수령 기간 텍스트
        public boolean isDrawn;           // 추첨 완료 여부
        public List<Applicant> applicants = new ArrayList<>();  // 응모자 목록

        public void addApplicant(String name, String hakbun, int count) {
            Applicant a = new Applicant();
            a.name = name;
            a.hakbun = hakbun;
            a.count = count;
            a.status = "대기";   // 기본 상태
            applicants.add(a);
        }
    }

    public static class Applicant {
        public String name;     // 응모자 이름
        public String hakbun;   // 학번
        public int count;       // 응모 횟수
        public String status;   // "대기", "당첨", "미당첨"
    }

    // ===================== 유틸 =====================

    // "1회차: SWU 봄맞이 이벤트" → "SWU 봄맞이 이벤트"
    private static String stripRoundPrefix(String rawName) {
        if (rawName == null) return "";
        int idx = rawName.indexOf(":");
        if (idx > 0 && rawName.substring(0, idx).contains("회차")) {
            return rawName.substring(idx + 1).trim();
        }
        return rawName;
    }

    // "2025-04-01 ~ 2025-04-10" → ["2025-04-01", "2025-04-10"]
    private static String[] splitPeriod(String period) {
        if (period == null) return null;
        String[] parts = period.split("~");
        if (parts.length < 2) return null;
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    // ===================== 회차 전체 조회 =====================

    public static List<LotteryRound> getAllRounds() {
        List<LotteryRound> list = new ArrayList<>();

        String sql =
                "SELECT round_id, round_name, prize_name, winner_count, " +
                "       announcement_date, application_start, application_end, " +
                "       pickup_location, pickup_start, pickup_end, is_drawn " +
                "FROM lottery_round " +
                "ORDER BY round_id ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                LotteryRound r = new LotteryRound();
                r.roundId = rs.getInt("round_id");

                String rawName = rs.getString("round_name");
                r.name = stripRoundPrefix(rawName);

                r.prizeName   = rs.getString("prize_name");
                r.winnerCount = rs.getInt("winner_count");

                Date annDate = rs.getDate("announcement_date");
                r.announcementDate = (annDate != null) ? annDate.toString() : "";

                Timestamp appStart = rs.getTimestamp("application_start");
                Timestamp appEnd   = rs.getTimestamp("application_end");
                if (appStart != null && appEnd != null) {
                    r.applicationPeriod =
                            appStart.toLocalDateTime().toLocalDate() + " ~ " +
                            appEnd.toLocalDateTime().toLocalDate();
                } else {
                    r.applicationPeriod = "-";
                }

                r.pickupLocation = rs.getString("pickup_location");

                Timestamp pickStart = rs.getTimestamp("pickup_start");
                Timestamp pickEnd   = rs.getTimestamp("pickup_end");
                if (pickStart != null && pickEnd != null) {
                    r.pickupPeriod =
                            pickStart.toLocalDateTime().toLocalDate() + " ~ " +
                            pickEnd.toLocalDateTime().toLocalDate();
                } else {
                    r.pickupPeriod = "-";
                }

                // 추첨 완료 여부
                r.isDrawn = rs.getInt("is_drawn") == 1;

                // 응모자 목록 로딩
                r.applicants = getApplicantsByRound(r.roundId);

                // 아직 추첨 전이면 상태를 "대기"로 통일
                if (!r.isDrawn) {
                    for (Applicant a : r.applicants) {
                        a.status = "대기";
                    }
                }

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== 한 회차 응모자 조회 =====================

    // round_id 기준으로 응모자 목록 로딩 (members 조인 + is_win 문자열 대응)
    public static List<Applicant> getApplicantsByRound(int roundId) {
        List<Applicant> list = new ArrayList<>();

        String sql =
                "SELECT e.hakbun, m.name, e.entry_count, e.is_win " +
                "FROM lottery_entry e " +
                "JOIN members m ON e.hakbun = m.hakbun " +
                "WHERE e.round_id = ? " +
                "ORDER BY e.raffle_id ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roundId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Applicant a = new Applicant();
                    a.hakbun = rs.getString("hakbun");
                    a.name   = rs.getString("name");
                    a.count  = rs.getInt("entry_count");

                    // 🔹 is_win 을 문자열로 읽어서 여러 케이스를 모두 처리
                    String winRaw = rs.getString("is_win");  // 예: "W", "1", "0", null ...

                    if (winRaw == null) {
                        a.status = "미당첨";
                    } else {
                        winRaw = winRaw.trim();

                        if ("W".equalsIgnoreCase(winRaw) || "1".equals(winRaw)) {
                            a.status = "당첨";
                        } else {
                            // 그 외 값은 전부 미당첨 취급 (예: "N", "0", "")
                            a.status = "미당첨";
                        }
                    }

                    list.add(a);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== 응모 (포인트 사용) =====================

    /**
     * 경품 응모 시:
     * 1) members 에서 포인트 조회
     * 2) 포인트 >= costPoints 인지 확인
     * 3) 포인트 차감
     * 4) lottery_entry 에 응모 내역 반영
     *    - 이미 존재하면 entry_count += 1
     *    - 없으면 새로 INSERT (entry_count = 1)
     */
    public static boolean applyUsingPoints(int roundId, String hakbun, int costPoints) {

        String selectPointSql =
                "SELECT point FROM members WHERE hakbun = ?";
        String updatePointSql =
                "UPDATE members SET point = point - ? WHERE hakbun = ?";

        // round+hakbun 응모 내역 확인
        String selectEntrySql =
                "SELECT entry_count FROM lottery_entry WHERE round_id = ? AND hakbun = ?";

        String insertEntrySql =
        	    "INSERT INTO lottery_entry (round_id, hakbun, entry_count, is_win) " +
        	    "VALUES (?, ?, 1, 0)";

        String updateEntrySql =
        	    "UPDATE lottery_entry SET entry_count = entry_count + 1 " +
        	    "WHERE round_id = ? AND hakbun = ?";

        // (선택) 응모 기간 체크용
        String selectRoundPeriodSql =
                "SELECT application_start, application_end FROM lottery_round WHERE round_id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            // 0) (선택) 응모 기간 체크
            try (PreparedStatement ps = conn.prepareStatement(selectRoundPeriodSql)) {
                ps.setInt(1, roundId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Timestamp tsStart = rs.getTimestamp("application_start");
                        Timestamp tsEnd   = rs.getTimestamp("application_end");

                        if (tsStart != null && tsEnd != null) {
                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime start = tsStart.toLocalDateTime();
                            LocalDateTime end   = tsEnd.toLocalDateTime();

                            if (now.isBefore(start) || now.isAfter(end)) {
                                System.out.println("[Lottery] 응모 기간이 아님. roundId=" + roundId);
                                conn.rollback();
                                return false;
                            }
                        }
                    }
                }
            }

            int currentPoint;

            // 1) 현재 포인트 조회
            try (PreparedStatement pstmt = conn.prepareStatement(selectPointSql)) {
                pstmt.setString(1, hakbun);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("[Lottery] members에서 학번을 찾지 못함: " + hakbun);
                        conn.rollback();
                        return false;
                    }
                    currentPoint = rs.getInt("point");
                }
            }

            // 2) 포인트 부족 체크
            if (currentPoint < costPoints) {
                System.out.println("[Lottery] 포인트 부족: 현재 " + currentPoint + ", 필요 " + costPoints);
                conn.rollback();
                return false;
            }

            // 3) 포인트 차감
            try (PreparedStatement pstmt = conn.prepareStatement(updatePointSql)) {
                pstmt.setInt(1, costPoints);
                pstmt.setString(2, hakbun);
                pstmt.executeUpdate();
            }

            // 4) 응모 내역 INSERT or UPDATE
            boolean exists;
            try (PreparedStatement pstmt = conn.prepareStatement(selectEntrySql)) {
                pstmt.setInt(1, roundId);
                pstmt.setString(2, hakbun);
                try (ResultSet rs = pstmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                // 이미 응모 내역 있으면 응모 횟수 +1
                try (PreparedStatement pstmt = conn.prepareStatement(updateEntrySql)) {
                    pstmt.setInt(1, roundId);
                    pstmt.setString(2, hakbun);
                    pstmt.executeUpdate();
                }
            } else {
                // 처음 응모하는 경우
                try (PreparedStatement pstmt = conn.prepareStatement(insertEntrySql)) {
                    pstmt.setInt(1, roundId);
                    pstmt.setString(2, hakbun);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== 회차 추가 =====================
 // ===================== 회차 추가 =====================

    /**
     * 관리자가 새 추첨 회차를 등록할 때 사용.
     *
     * @param titleOnly    회차 제목
     * @param prize        경품 이름
     * @param count        당첨 인원 수
     * @param annDateStr   발표일 (yyyy-MM-dd)
     * @param appStartStr  응모 시작 일시 (yyyy-MM-dd HH:mm 또는 HH:mm:ss)
     * @param appEndStr    응모 마감 일시
     * @param loc          수령 장소
     * @param pickStartStr 수령 시작 일시
     * @param pickEndStr   수령 마감 일시
     */
    public static boolean addRound(String titleOnly, String prize, int count,
                                   String annDateStr,
                                   String appStartStr, String appEndStr,
                                   String loc,
                                   String pickStartStr, String pickEndStr) {

        String sql =
                "INSERT INTO lottery_round " +
                "(round_name, prize_name, winner_count, " +
                " announcement_date, application_start, application_end, " +
                " pickup_location, pickup_start, pickup_end, is_drawn) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            // 1) 제목/경품/인원
            pstmt.setString(1, titleOnly);
            pstmt.setString(2, prize);
            pstmt.setInt(3, count);

            // 2) 발표일 (DATE)  → "yyyy-MM-dd"
            LocalDate ann = LocalDate.parse(annDateStr);   // AdminLotteryFrame 에서 이미 yyyy-MM-dd 로 포맷해줌
            pstmt.setDate(4, Date.valueOf(ann));

            // 3) 응모/수령 기간 (DATETIME)  → "yyyy-MM-dd HH:mm" 또는 "yyyy-MM-dd HH:mm:ss"
            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");

            LocalDateTime appStart  = LocalDateTime.parse(appStartStr, dtFmt);
            LocalDateTime appEnd    = LocalDateTime.parse(appEndStr, dtFmt);
            LocalDateTime pickStart = LocalDateTime.parse(pickStartStr, dtFmt);
            LocalDateTime pickEnd   = LocalDateTime.parse(pickEndStr, dtFmt);

            pstmt.setTimestamp(5, Timestamp.valueOf(appStart));
            pstmt.setTimestamp(6, Timestamp.valueOf(appEnd));

            // 4) 수령 장소
            pstmt.setString(7, loc);

            pstmt.setTimestamp(8, Timestamp.valueOf(pickStart));
            pstmt.setTimestamp(9, Timestamp.valueOf(pickEnd));

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;

        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===================== 추첨 결과 저장 =====================

    /**
     * AdminLotteryFrame.runLottery() 에서 메모리 상의 round.applicants에
     * status("당첨"/"미당첨")를 다 채운 다음,
     * 그 내용을 DB(lottery_round.is_drawn, lottery_entry.is_win)에 반영.
     */
    public static boolean saveDrawResult(LotteryRound round) {

        String sqlUpdateRound =
                "UPDATE lottery_round SET is_drawn = 1 WHERE round_id = ?";

        String sqlUpdateApplicant =
                "UPDATE lottery_entry SET is_win = ? " +
                "WHERE round_id = ? AND hakbun = ?";

        Connection conn = null;
        PreparedStatement psRound = null;
        PreparedStatement psApp = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1) 회차 상태 업데이트
            psRound = conn.prepareStatement(sqlUpdateRound);
            psRound.setInt(1, round.roundId);
            psRound.executeUpdate();

            // 2) 응모자별 is_win 업데이트
            psApp = conn.prepareStatement(sqlUpdateApplicant);

            for (Applicant a : round.applicants) {
                int isWinValue = "당첨".equals(a.status) ? 1 : 0;

                psApp.setInt(1, isWinValue);
                psApp.setInt(2, round.roundId);
                psApp.setString(3, a.hakbun);
                psApp.addBatch();
            }

            psApp.executeBatch();

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignore) {}
            }
            return false;

        } finally {
            try { if (psApp != null) psApp.close(); } catch (Exception ignored) {}
            try { if (psRound != null) psRound.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
}
