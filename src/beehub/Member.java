package beehub;

// 학생/회원 정보를 담는 DTO(VO) 역할 클래스
public class Member {

    private String hakbun;       // 학번 (로그인 ID)
    private String pw;           // 비밀번호
    private String name;         // 이름
    private String nickname;     // 닉네임
    private String major;        // 전공
    private String phone;        // 전화번호
    private String isFeePaid;    // 회비 납부 여부 ("Y"/"N")
    private String deptFeeYn; 
    private int point;           // 포인트
    private String grade;        // 등급 (일벌/꿀벌/여왕벌 등)
    private String penaltyDate;  // 페널티 날짜 문자열
    private int warningCount;    // 경고 횟수
    private String role;         // 권한 (USER / ADMIN_TOTAL / ADMIN_COUNCIL)

    public Member() {
    }

    public Member(String hakbun, String pw, String name,
                  String nickname, String major,
                  String phone, String isFeePaid, int point, String grade,
                  String penaltyDate, int warningCount, String role) {
        this.hakbun = hakbun;
        this.pw = pw;
        this.name = name;
        this.nickname = nickname;
        this.major = major;
        this.phone = phone;
        this.isFeePaid = isFeePaid;
        this.deptFeeYn = deptFeeYn;
        this.point = point;
        this.grade = grade;
        this.penaltyDate = penaltyDate;
        this.warningCount = warningCount;
        this.role = role;
    }

    public String getHakbun() {
        return hakbun;
    }

    public void setHakbun(String hakbun) {
        this.hakbun = hakbun;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIsFeePaid() {
        return isFeePaid;
    }

    public void setIsFeePaid(String isFeePaid) {
        this.isFeePaid = isFeePaid;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getPenaltyDate() {
        return penaltyDate;
    }

    public void setPenaltyDate(String penaltyDate) {
        this.penaltyDate = penaltyDate;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getDeptFeeYn() { return deptFeeYn; }
    public void setDeptFeeYn(String deptFeeYn) { this.deptFeeYn = deptFeeYn; }
}
