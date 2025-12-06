package beehub;

public class User {
    private String id;       // 학번
    private String password;
    private String name;     // 이름
    private String nickname;   // ⭐ 닉네임
    private String dept;     // 학과
    private int points;      // 꿀 포인트
    private String role;
    private String phone;



    
    public User() {}

    public User(String id, String password, String name, String dept, int points) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.dept = dept;
        this.points = points;
    }

    public String getId() { return id; }
    public void setId(String id) {this.id = id;}
    public String getPassword() { return password; }
    public void setPassword(String password) {this.password = password;}
    public String getName() { return name; }
    public void setName(String name) {this.name = name;}
    public String getNickname() {return nickname;}
    public void setNickname(String nickname) {this.nickname = nickname;}
    public String getDept() { return dept; }
    public void setDept(String dept) {this.dept = dept;}
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}
    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}





}

