package council;

import beehub.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventManager {

    // 여러 화면에서 공유하는 날짜 포맷
    public static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd (E) HH:mm");

    // =========================
    //  회비 조건
    // =========================
    public enum FeeType {
        NONE("누구나 참여 가능"),
        SCHOOL("학교 학생회비 납부자"),
        DEPT("과 학생회비 납부자");

        private final String label;
        FeeType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    // =========================
    //  명단 DTO
    // =========================
    public static class Recipient {
        public String name;
        public String hakbun;
        public String paidFlag; // "O" 등

        public Recipient(String name, String hakbun, String paidFlag) {
            this.name = name;
            this.hakbun = hakbun;
            this.paidFlag = paidFlag;
        }
    }

    // =========================
    //  행사 데이터 DTO
    // =========================
    public static class EventData {
        public int eventId;
        public String eventType;            // SNACK / ACTIVITY
        public String title;
        public LocalDateTime date;          // 행사 일시
        public String location;
        public LocalDateTime applyStart;
        public LocalDateTime applyEnd;
        public LocalDateTime endDateTime;
        public int totalCount;
        public int currentCount;
        public String secretCode;
        public String description;
        public String status;               // "진행중" / "신청마감" / "종료" / "삭제"
        public String targetDept;           // target_major
        public String ownerHakbun;          // 주최 학생회 ID
        public LocalDateTime startDateTime;

        public FeeType requiredFee = FeeType.NONE;

        // 명단 (UI 용)
        public List<Recipient> recipients = new ArrayList<>();

        public String getPeriodString() {
            if (applyStart == null || applyEnd == null) return "";
            return applyStart.format(DATE_FMT) + " ~ " + applyEnd.format(DATE_FMT);
        }

        // 신청 처리 (명단 추가)
        public boolean addRecipient(String name, String hakbun, String paidFlag) {
            boolean ok = EventManager.insertApply(this, hakbun);
            if (ok) {
                recipients.add(new Recipient(name, hakbun, paidFlag));
            }
            return ok;
        }
    }

    // =========================
    //  참여 인원 카운트
    // =========================
    private static int getParticipantCount(int eventId) {
        String sql = "SELECT COUNT(*) FROM event_participation WHERE event_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =========================
    //  ResultSet -> EventData 매핑
    // =========================
    private static EventData mapRow(ResultSet rs) throws SQLException {
        EventData d = new EventData();

        d.eventId   = rs.getInt("event_id");
        d.eventType = rs.getString("event_type");
        d.title     = rs.getString("event_name");

        Timestamp tEvent = rs.getTimestamp("event_date");
        if (tEvent != null) {
            d.date = tEvent.toLocalDateTime();
            d.startDateTime = d.date; // 옛 필드와 동기화
        }

        d.location = rs.getString("location");

        Timestamp tStart = rs.getTimestamp("apply_start");
        Timestamp tEnd   = rs.getTimestamp("apply_end");
        if (tStart != null) d.applyStart = tStart.toLocalDateTime();
        if (tEnd   != null) d.applyEnd   = tEnd.toLocalDateTime();

        int total  = rs.getInt("total_quantity");
        int remain = rs.getInt("remaining_quantity");
        d.totalCount = total;

        // 인원은 event_participation 레코드 수로 계산
        d.currentCount = getParticipantCount(d.eventId);

        d.secretCode  = rs.getString("secret_code");
        d.description = rs.getString("description");

        String dbStatus = rs.getString("status"); // SCHEDULED / PROGRESS / CLOSED / DELETED
        if ("CLOSED".equalsIgnoreCase(dbStatus)) {
            d.status = "종료";
        } else if ("DELETED".equalsIgnoreCase(dbStatus)) {
            d.status = "삭제";
        } else if ("PROGRESS".equalsIgnoreCase(dbStatus)) {
            d.status = "진행중";
        } else {
            d.status = "진행중"; // 기본값
        }

        d.targetDept  = rs.getString("target_major");
        d.ownerHakbun = rs.getString("owner_hakbun");

        // 회비 조건 컬럼 읽기 (없으면 NONE)
        String feeCode = null;
        try {
            feeCode = rs.getString("required_fee");
        } catch (SQLException ignore) { }

        if (feeCode == null || feeCode.isEmpty() || "NONE".equalsIgnoreCase(feeCode)) {
            d.requiredFee = FeeType.NONE;
        } else if ("SCHOOL".equalsIgnoreCase(feeCode)) {
            d.requiredFee = FeeType.SCHOOL;
        } else if ("DEPT".equalsIgnoreCase(feeCode)) {
            d.requiredFee = FeeType.DEPT;
        } else {
            d.requiredFee = FeeType.NONE;
        }

        return d;
    }

    // =========================
    //  조회 메서드들
    // =========================

    /** 특정 학생회가 주최한 행사 (과학생회 관리 화면) */
    public static List<EventData> getEventsByOwner(String ownerHakbun) {
        List<EventData> list = new ArrayList<>();

        String sql = "SELECT * FROM events " +
                     "WHERE owner_hakbun = ? " +
                     "  AND (status IS NULL OR status <> 'DELETED') " +
                     "ORDER BY event_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ownerHakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 전체 행사 목록 (학생 메인 등에서 사용) – 삭제된 행사 제외 */
    public static List<EventData> getAllEvents() {
        List<EventData> list = new ArrayList<>();

        String sql =
                "SELECT * FROM events " +
                "WHERE status IS NULL OR status <> 'DELETED' " +
                "ORDER BY event_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 특정 학과 학생이 보는 "과 행사" 목록
     *  - event_type = 'ACTIVITY'
     *  - target_major = 내 학과 OR '전체' / 'ALL' / NULL / ''
     *  - 삭제된 행사 제외
     */
    public static List<EventData> getDeptEventsForStudent(String major) {
        List<EventData> list = new ArrayList<>();

        String sql =
            "SELECT * FROM events " +
            "WHERE event_type = 'ACTIVITY' " +
            "  AND (status IS NULL OR status <> 'DELETED') " +
            "  AND (" +
            "       target_major = ? " +
            "    OR target_major = '전체' " +
            "    OR target_major = 'ALL' " +
            "    OR target_major IS NULL " +
            "    OR target_major = ''" +
            "  ) " +
            "ORDER BY event_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, major);   // 예: "수학과"

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    //  행사 등록 / 수정
    // =========================

    /** 새 행사 등록 또는 수정 (eventId == 0 이면 INSERT, 아니면 UPDATE) */
    public static void addEvent(EventData d) {
        if (d == null) return;

        int remain = d.totalCount - d.currentCount;
        if (remain < 0) remain = 0;

        String dbStatus;
        if ("삭제".equals(d.status)) dbStatus = "DELETED";
        else if ("종료".equals(d.status)) dbStatus = "CLOSED";
        else if ("진행중".equals(d.status)) dbStatus = "PROGRESS";
        else dbStatus = "SCHEDULED";

        if (d.eventType == null || d.eventType.isEmpty()) {
            d.eventType = "SNACK";
        }

        // FeeType → DB 코드
        String feeCode = "NONE";
        if (d.requiredFee != null) {
            switch (d.requiredFee) {
                case SCHOOL: feeCode = "SCHOOL"; break;
                case DEPT:   feeCode = "DEPT";   break;
                case NONE:
                default:     feeCode = "NONE";   break;
            }
        }

        try (Connection conn = DBUtil.getConnection()) {
            if (d.eventId == 0) {
                // INSERT
                String sql = "INSERT INTO events (" +
                        "event_type, event_name, event_date, location, " +
                        "apply_start, apply_end, total_quantity, remaining_quantity, " +
                        "secret_code, description, status, target_major, owner_hakbun, " +
                        "required_fee, created_at, updated_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

                try (PreparedStatement pstmt =
                             conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    pstmt.setString(1, d.eventType);
                    pstmt.setString(2, d.title);
                    pstmt.setTimestamp(3, d.date != null ? Timestamp.valueOf(d.date) : null);
                    pstmt.setString(4, d.location);
                    pstmt.setTimestamp(5, d.applyStart != null ? Timestamp.valueOf(d.applyStart) : null);
                    pstmt.setTimestamp(6, d.applyEnd != null ? Timestamp.valueOf(d.applyEnd) : null);
                    pstmt.setInt(7, d.totalCount);
                    pstmt.setInt(8, remain);
                    pstmt.setString(9, d.secretCode);
                    pstmt.setString(10, d.description);
                    pstmt.setString(11, dbStatus);
                    pstmt.setString(12, d.targetDept);
                    pstmt.setString(13, d.ownerHakbun);
                    pstmt.setString(14, feeCode);

                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) d.eventId = rs.getInt(1);
                    }
                }
            } else {
                // UPDATE
                String sql = "UPDATE events SET " +
                        "event_type=?, event_name=?, event_date=?, location=?, " +
                        "apply_start=?, apply_end=?, total_quantity=?, remaining_quantity=?, " +
                        "secret_code=?, description=?, status=?, target_major=?, owner_hakbun=?, " +
                        "required_fee=?, updated_at = NOW() " +
                        "WHERE event_id=?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, d.eventType);
                    pstmt.setString(2, d.title);
                    pstmt.setTimestamp(3, d.date != null ? Timestamp.valueOf(d.date) : null);
                    pstmt.setString(4, d.location);
                    pstmt.setTimestamp(5, d.applyStart != null ? Timestamp.valueOf(d.applyStart) : null);
                    pstmt.setTimestamp(6, d.applyEnd != null ? Timestamp.valueOf(d.applyEnd) : null);
                    pstmt.setInt(7, d.totalCount);
                    pstmt.setInt(8, remain);
                    pstmt.setString(9, d.secretCode);
                    pstmt.setString(10, d.description);
                    pstmt.setString(11, dbStatus);
                    pstmt.setString(12, d.targetDept);
                    pstmt.setString(13, d.ownerHakbun);
                    pstmt.setString(14, feeCode);
                    pstmt.setInt(15, d.eventId);

                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    //  신청 INSERT + 중복 체크
    // =========================

    private static boolean insertApply(EventData event, String hakbun) {

        String sqlCheck =
                "SELECT COUNT(*) FROM event_participation " +
                "WHERE event_id = ? AND participant_hakbun = ? " +
                "AND participation_type = 'APPLY'";

        String sqlInsert =
                "INSERT INTO event_participation " +
                "(event_id, participant_hakbun, participation_type, participation_date, quantity, note) " +
                "VALUES (?, ?, 'APPLY', NOW(), 1, NULL)";

        String sqlUpdate =
                "UPDATE events SET remaining_quantity = remaining_quantity - 1, " +
                "updated_at = NOW() " +
                "WHERE event_id = ? AND remaining_quantity > 0";

        String sqlSelect =
                "SELECT total_quantity, remaining_quantity " +
                "FROM events WHERE event_id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pCheck = conn.prepareStatement(sqlCheck);
                 PreparedStatement p1 = conn.prepareStatement(sqlInsert);
                 PreparedStatement p2 = conn.prepareStatement(sqlUpdate);
                 PreparedStatement p3 = conn.prepareStatement(sqlSelect)) {

                // 중복 체크
                pCheck.setInt(1, event.eventId);
                pCheck.setString(2, hakbun);
                try (ResultSet rs = pCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        return false; // 이미 신청함
                    }
                }

                // 1) 신청 기록 추가
                p1.setInt(1, event.eventId);
                p1.setString(2, hakbun);
                p1.executeUpdate();

                // 2) 남은 수량 감소
                p2.setInt(1, event.eventId);
                int updated = p2.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false; // 잔여 수량 없음
                }

                // 3) 현재/잔여 수량 다시 조회
                p3.setInt(1, event.eventId);
                try (ResultSet rs = p3.executeQuery()) {
                    if (rs.next()) {
                        int total  = rs.getInt("total_quantity");
                        int remain = rs.getInt("remaining_quantity");
                        event.totalCount = total;
                    }
                }

                // 현재 인원 카운트 동기화
                event.currentCount = getParticipantCount(event.eventId);

                conn.commit();
                return true;

            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    //  삭제 (과학생회에서 행사 삭제) - 소프트 삭제
    // =========================
    public static void deleteEvent(int eventId) {
        String sql1 = "DELETE FROM event_participation WHERE event_id = ?";
        String sql2 = "UPDATE events " +
                      "SET status = 'DELETED', updated_at = NOW() " +
                      "WHERE event_id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement p1 = conn.prepareStatement(sql1);
                 PreparedStatement p2 = conn.prepareStatement(sql2)) {

                // 1) 참가 내역 삭제
                p1.setInt(1, eventId);
                p1.executeUpdate();

                // 2) 행사 상태를 DELETED 로 변경
                p2.setInt(1, eventId);
                p2.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
