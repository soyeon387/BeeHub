// 파일명: CommunityDAO.java
package beehub;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommunityDAO {

    // ================================
    // 📌 게시글 DTO
    // ================================
    public static class PostDTO {
        public int postId;
        public String writerHakbun;
        public String writerNickname;
        public String title;
        public String content;
        public String createdDate;  // "yyyy-MM-dd"
        public int likeCount;
        public int commentCount;
    }

    // ================================
    // 📌 댓글 DTO
    // ================================
    public static class CommentDTO {
        public int commentId;
        public int postId;
        public String writerHakbun;
        public String writerNickname;
        public String content;
        public String createdDate;   // "yyyy-MM-dd HH:mm"
    }

    // ================================
    // 🔗 공통 커넥션
    // ================================
    private Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    // ================================
    // 0. 전체 게시글 (최신순)
    //    ✅ 작성자 닉네임은 항상 members 기준 최신값 사용
    // ================================
    public List<PostDTO> getAllPostsOrderByNewest() {
        List<PostDTO> list = new ArrayList<>();

        String sql =
            "SELECT p.post_id, " +
            "       p.writer_hakbun, " +
            "       COALESCE(NULLIF(m.nickname, ''), NULLIF(p.writer_nickname, ''), m.name) AS writer_nickname, " +
            "       p.title, " +
            "       p.content, " +
            "       DATE_FORMAT(p.created_at, '%Y-%m-%d') AS created_date, " +
            "       p.like_count, " +
            "       p.comment_count " +
            "FROM community_post p " +
            "LEFT JOIN members m ON p.writer_hakbun = m.hakbun " +
            "WHERE p.is_deleted = 0 " +
            "ORDER BY p.post_id DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PostDTO dto = new PostDTO();
                dto.postId         = rs.getInt("post_id");
                dto.writerHakbun   = rs.getString("writer_hakbun");
                dto.writerNickname = rs.getString("writer_nickname"); // ✅ 항상 최신 닉네임/이름
                dto.title          = rs.getString("title");
                dto.content        = rs.getString("content");
                dto.createdDate    = rs.getString("created_date");
                dto.likeCount      = rs.getInt("like_count");
                dto.commentCount   = rs.getInt("comment_count");

                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================================
    // 1-0. 단일 게시글 조회
    //      ✅ 작성자 닉네임도 members 기준 최신값 사용
    // ================================
    public PostDTO getPostById(int postId) {
        String sql =
            "SELECT p.post_id, p.writer_hakbun, " +
            "       COALESCE(NULLIF(m.nickname, ''), NULLIF(p.writer_nickname, ''), m.name) AS writer_nickname, " +
            "       p.title, p.content, " +
            "       DATE_FORMAT(p.created_at, '%Y-%m-%d') AS created_date, " +
            "       p.like_count " +
            "FROM community_post p " +
            "LEFT JOIN members m ON p.writer_hakbun = m.hakbun " +
            "WHERE p.post_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PostDTO dto = new PostDTO();
                    dto.postId         = rs.getInt("post_id");
                    dto.writerHakbun   = rs.getString("writer_hakbun");
                    dto.writerNickname = rs.getString("writer_nickname");
                    dto.title          = rs.getString("title");
                    dto.content        = rs.getString("content");
                    dto.createdDate    = rs.getString("created_date");
                    dto.likeCount      = rs.getInt("like_count");
                    dto.commentCount   = getCommentCount(conn, dto.postId);
                    return dto;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================================
    // 1-1. 특정 게시글의 댓글 개수 (같은 커넥션 재사용)
    // ================================
    private int getCommentCount(Connection conn, int postId) {
        String sql = "SELECT COUNT(*) FROM community_comment WHERE post_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================================
    // 2. 게시글 INSERT (생성된 post_id 리턴)
    // ================================
    public int insertPost(String writerHakbun, String writerNickname,
                          String title, String content) {

        String sql =
            "INSERT INTO community_post " +
            " (writer_hakbun, writer_nickname, title, content, like_count, comment_count) " +
            "VALUES (?, ?, ?, ?, 0, 0)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt =
                 conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, writerHakbun);
            pstmt.setString(2, writerNickname);   // 스냅샷(백업용)
            pstmt.setString(3, title);
            pstmt.setString(4, content);

            int affected = pstmt.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);   // 새로 생성된 post_id
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ================================
    // 3. 게시글 수정
    // ================================
    public void updatePost(int postId, String title, String content) {
        String sql = "UPDATE community_post SET title = ?, content = ? WHERE post_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, postId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================
    // 4. 게시글 삭제
    // ================================
    public void deletePost(int postId) {
        String sql = "DELETE FROM community_post WHERE post_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================
    // 5. COMMUNITY_POST.like_count 직접 세팅
    // ================================
    public void updateLikeCount(int postId, int likeCount) {
        String sql = "UPDATE community_post SET like_count = ? WHERE post_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, likeCount);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================
    // 5-0. 해당 게시글의 좋아요 수 조회
    //      ✅ 출처: community_post.like_count
    // ================================
    public int getLikeCount(int postId) {
        String sql = "SELECT like_count FROM community_post WHERE post_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("like_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================================
    // 5-1. 특정 사용자가 이미 좋아요 눌렀는지 확인
    //      👉 테이블: community_post_like(post_id, liker_hakbun, created_at)
    // ================================
    public boolean hasUserLiked(int postId, String likerHakbun) {
        String sql =
            "SELECT COUNT(*) FROM community_post_like " +
            "WHERE post_id = ? AND liker_hakbun = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            pstmt.setString(2, likerHakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================================
    // 5-2. 좋아요 추가
    //      - community_post_like 에 INSERT
    //      - community_post.like_count = like_count + 1
    // ================================
    public void addLike(int postId, String likerHakbun) {
        String insertSql =
            "INSERT INTO community_post_like " +
            " (post_id, liker_hakbun, created_at) " +
            "VALUES (?, ?, NOW())";

        String updateSql =
            "UPDATE community_post SET like_count = like_count + 1 " +
            "WHERE post_id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateSql)) {

                // 1) 좋아요 기록 추가
                pstmt1.setInt(1, postId);
                pstmt1.setString(2, likerHakbun);
                pstmt1.executeUpdate();

                // 2) 게시글 like_count + 1
                pstmt2.setInt(1, postId);
                pstmt2.executeUpdate();

                conn.commit();
            } catch (SQLIntegrityConstraintViolationException e) {
                // PK(post_id, liker_hakbun) 중복인 경우 → 이미 좋아요 누른 글
                System.out.println("이미 좋아요 누른 글입니다. postId=" + postId + ", hakbun=" + likerHakbun);
                conn.rollback();
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================
    // 6. 댓글 INSERT
    // ================================
    public void insertComment(int postId, String writerHakbun,
                              String writerNickname, String content) {
        String sql =
            "INSERT INTO community_comment " +
            " (post_id, writer_hakbun, writer_nickname, content, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            pstmt.setString(2, writerHakbun);
            pstmt.setString(3, writerNickname); // 스냅샷(백업용)
            pstmt.setString(4, content);

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // (선택) COMMUNITY_POST.comment_count 컬럼까지 맞춰 두고 싶을 때 사용할 수 있는 메소드
    @SuppressWarnings("unused")
    private void updateCommentCount(Connection conn, int postId, int commentCount) {
        String sql = "UPDATE community_post SET comment_count = ? WHERE post_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentCount);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================
    // 7. 특정 게시글의 댓글 목록 조회
    //    ✅ 댓글 작성자 닉네임도 항상 members 기준 최신값 사용
    // ================================
    public List<CommentDTO> getCommentsByPostId(int postId) {

        String sql =
            "SELECT c.comment_id, c.post_id, c.writer_hakbun, " +
            "       COALESCE(NULLIF(m.nickname, ''), NULLIF(c.writer_nickname, ''), m.name) AS writer_nickname, " +
            "       c.content, " +
            "       DATE_FORMAT(c.created_at, '%Y-%m-%d %H:%i') AS created_date " +
            "FROM community_comment c " +
            "LEFT JOIN members m ON c.writer_hakbun = m.hakbun " +
            "WHERE c.post_id = ? " +
            "ORDER BY c.comment_id ASC";

        List<CommentDTO> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CommentDTO dto = new CommentDTO();
                    dto.commentId      = rs.getInt("comment_id");
                    dto.postId         = rs.getInt("post_id");
                    dto.writerHakbun   = rs.getString("writer_hakbun");
                    dto.writerNickname = rs.getString("writer_nickname"); // ✅ 최신 닉네임/이름
                    dto.content        = rs.getString("content");
                    dto.createdDate    = rs.getString("created_date");
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================================
    // 8. 내가 작성한 게시글
    //    ✅ 리스트에서도 최신 닉네임 사용
    // ================================
    public List<PostDTO> getPostsWrittenByUser(String writerHakbun) {

        String sql =
            "SELECT p.post_id, p.writer_hakbun, " +
            "       COALESCE(NULLIF(m.nickname, ''), NULLIF(p.writer_nickname, ''), m.name) AS writer_nickname, " +
            "       p.title, p.content, " +
            "       DATE_FORMAT(p.created_at, '%Y-%m-%d') AS created_date, " +
            "       p.like_count " +
            "FROM community_post p " +
            "LEFT JOIN members m ON p.writer_hakbun = m.hakbun " +
            "WHERE p.writer_hakbun = ? " +
            "ORDER BY p.post_id DESC";

        List<PostDTO> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, writerHakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PostDTO dto = new PostDTO();
                    dto.postId         = rs.getInt("post_id");
                    dto.writerHakbun   = rs.getString("writer_hakbun");
                    dto.writerNickname = rs.getString("writer_nickname");
                    dto.title          = rs.getString("title");
                    dto.content        = rs.getString("content");
                    dto.createdDate    = rs.getString("created_date");
                    dto.likeCount      = rs.getInt("like_count");
                    dto.commentCount   = getCommentCount(conn, dto.postId);

                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================================
    // 9. 내가 댓글 단 게시글
    //    ✅ 게시글 작성자 닉네임도 최신값 사용
    // ================================
    public List<PostDTO> getPostsUserCommented(String writerHakbun) {

        String sql =
            "SELECT DISTINCT p.post_id, p.writer_hakbun, " +
            "       COALESCE(NULLIF(m.nickname, ''), NULLIF(p.writer_nickname, ''), m.name) AS writer_nickname, " +
            "       p.title, p.content, " +
            "       DATE_FORMAT(p.created_at, '%Y-%m-%d') AS created_date, " +
            "       p.like_count " +
            "FROM community_post p " +
            "JOIN community_comment c ON p.post_id = c.post_id " +
            "LEFT JOIN members m ON p.writer_hakbun = m.hakbun " +
            "WHERE c.writer_hakbun = ? " +
            "ORDER BY p.post_id DESC";

        List<PostDTO> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, writerHakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PostDTO dto = new PostDTO();
                    dto.postId         = rs.getInt("post_id");
                    dto.writerHakbun   = rs.getString("writer_hakbun");
                    dto.writerNickname = rs.getString("writer_nickname");
                    dto.title          = rs.getString("title");
                    dto.content        = rs.getString("content");
                    dto.createdDate    = rs.getString("created_date");
                    dto.likeCount      = rs.getInt("like_count");
                    dto.commentCount   = getCommentCount(conn, dto.postId);

                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================================
    // 10. 내가 좋아요 누른 게시글
    //      - 좋아요 테이블: community_post_like(post_id, liker_hakbun, created_at)
    //      ✅ 게시글 작성자 닉네임도 최신값 사용
    // ================================
    public List<PostDTO> getPostsUserLiked(String likerHakbun) {

        String sql =
            "SELECT DISTINCT p.post_id, p.writer_hakbun, " +
            "       COALESCE(NULLIF(m.nickname, ''), NULLIF(p.writer_nickname, ''), m.name) AS writer_nickname, " +
            "       p.title, p.content, " +
            "       DATE_FORMAT(p.created_at, '%Y-%m-%d') AS created_date, " +
            "       p.like_count " +
            "FROM community_post p " +
            "JOIN community_post_like l ON p.post_id = l.post_id " +
            "LEFT JOIN members m ON p.writer_hakbun = m.hakbun " +
            "WHERE l.liker_hakbun = ? " +
            "ORDER BY p.post_id DESC";

        List<PostDTO> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, likerHakbun);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PostDTO dto = new PostDTO();
                    dto.postId         = rs.getInt("post_id");
                    dto.writerHakbun   = rs.getString("writer_hakbun");
                    dto.writerNickname = rs.getString("writer_nickname");
                    dto.title          = rs.getString("title");
                    dto.content        = rs.getString("content");
                    dto.createdDate    = rs.getString("created_date");
                    dto.likeCount      = rs.getInt("like_count");
                    dto.commentCount   = getCommentCount(conn, dto.postId);

                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
