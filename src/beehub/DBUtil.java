package beehub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/beehub?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul";


    private static final String USER = "root";     // 실제 DB 사용자 ID로 변경하세요!
    private static final String PASS = "1234"; // 실제 DB 비밀번호로 변경하세요!

    // 1. Connection 객체 가져오기
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(DRIVER); // 드라이버 로드
            conn = DriverManager.getConnection(URL, USER, PASS); // 연결
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버를 찾을 수 없습니다. (jar 파일 확인 필요): " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("DB 연결 오류 (URL/ID/PW 확인 필요): " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    // 2. 자원 해제 (Connection, Statement, ResultSet)
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("DB 자원 해제 오류: " + e.getMessage());
        }
    }
    
    // 3. 자원 해제 (Connection, Statement)
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
}