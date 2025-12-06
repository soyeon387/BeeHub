package admin;

import beehub.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LotteryManager {

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

    // "1회차: SWU 봄맞이 이벤트" 처럼 저장돼 있다면 "SWU 봄맞이 이벤트"로 잘라내기
    private static String stripRoundPrefix(String rawName) {
        if (rawName == null) return "";
        int idx = rawName.indexOf(":");
        if (idx > 0 && rawName.substring(0, idx).contains("회차")) {
            return rawName.substring(idx + 1).trim();
        }
        return rawName;
    }

    // "2025-04-01 ~ 2025-04-10" 이런 문자열을 2개로 분리
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
     * roundId 회차에 hakbun 학생이 포인트 100 꿀을 사용해서 1회 응모.
     * - member.point >= 100 인지 확인 후 차감
     * - lottery_entry 에 응모 기록 insert
     */
    public static boolean applyUsingPoints(int roundId, String hakbun) {
        String sqlUpdatePoint =
                "UPDATE member SET point = point - 100 " +
                "WHERE hakbun = ? AND point >= 100";

        String sqlInsertEntry =
                "INSERT INTO lottery_entry (round_id, hakbun, entry_count, is_win, created_at) " +
                "VALUES (?, ?, 1, 0, NOW())";

        Connection conn = null;
        PreparedStatement psPoint = null;
        PreparedStatement psEntry = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1) 포인트 차감
            psPoint = conn.prepareStatement(sqlUpdatePoint);
            psPoint.setString(1, hakbun);
            int updated = psPoint.executeUpdate();
            if (updated == 0) {
                // 포인트 부족
                conn.rollback();
                return false;
            }

            // 2) 응모 기록 추가
            psEntry = conn.prepareStatement(sqlInsertEntry);
            psEntry.setInt(1, roundId);
            psEntry.setString(2, hakbun);
            psEntry.executeUpdate();

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignore) {}
            }
            return false;

        } finally {
            try { if (psEntry != null) psEntry.close(); } catch (Exception ignored) {}
            try { if (psPoint != null) psPoint.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===================== 회차 추가 =====================

    /**
     * 관리자가 새 추첨 회차를 등록할 때 사용.
     *
     * @param titleOnly   화면에 보일 회차 이름(예: "꿀단지 이용 감사 추첨")
     * @param prize       경품 이름
     * @param count       당첨 인원 수
     * @param annDate     발표일 (yyyy-MM-dd)
     * @param appPeriod   응모기간 문자열 ("2025-04-01 ~ 2025-04-10" or "-")
     * @param loc         수령 장소
     * @param pickPeriod  수령기간 문자열
     */
    public static boolean addRound(String titleOnly, String prize, int count,
                                   String annDate, String appPeriod,
                                   String loc, String pickPeriod) {

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

            // DB에는 "1회차: ..." 대신 그냥 제목만 넣어두기로.
            pstmt.setString(1, titleOnly);
            pstmt.setString(2, prize);
            pstmt.setInt(3, count);

            // 발표일
            LocalDate ann = LocalDate.parse(annDate);
            pstmt.setDate(4, Date.valueOf(ann));

            // 응모기간
            Timestamp appStartTs = null;
            Timestamp appEndTs   = null;
            String[] appRange = splitPeriod(appPeriod);
            if (appRange != null) {
                LocalDate s = LocalDate.parse(appRange[0]);
                LocalDate e = LocalDate.parse(appRange[1]);
                appStartTs = Timestamp.valueOf(LocalDateTime.of(s, java.time.LocalTime.MIDNIGHT));
                appEndTs   = Timestamp.valueOf(LocalDateTime.of(e, java.time.LocalTime.MIDNIGHT));
            }
            pstmt.setTimestamp(5, appStartTs);
            pstmt.setTimestamp(6, appEndTs);

            // 수령 장소
            pstmt.setString(7, loc);

            // 수령기간
            Timestamp pickStartTs = null;
            Timestamp pickEndTs   = null;
            String[] pickRange = splitPeriod(pickPeriod);
            if (pickRange != null) {
                LocalDate s = LocalDate.parse(pickRange[0]);
                LocalDate e = LocalDate.parse(pickRange[1]);
                pickStartTs = Timestamp.valueOf(LocalDateTime.of(s, java.time.LocalTime.MIDNIGHT));
                pickEndTs   = Timestamp.valueOf(LocalDateTime.of(e, java.time.LocalTime.MIDNIGHT));
            }
            pstmt.setTimestamp(8, pickStartTs);
            pstmt.setTimestamp(9, pickEndTs);

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
     * AdminLotteryFrame.runLottery() 에서 메모리 상의 round/applicants에
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
