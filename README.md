# 🐝 BeeHub: All-in-One Campus Life Platform (대학생 올인원 캠퍼스 라이프 플랫폼)

교내 물품 대여와 공간 예약, 커뮤니티 활동 및 포인트 기반 이벤트(추첨) 기능을 통합하여 대학생들의 편리한 학교생활을 지원하는 올인원 캠퍼스 플랫폼
1. 주요 기술 스택 (Tech Stack)

Java 기반의 애플리케이션으로 구축, 데이터 처리를 위해 JDBC를 사용

* Language: Java (JDK 8)
* Database: MySQL (Connector/J com.mysql.cj.jdbc.Driver 사용)
* Architecture: Model(DAO/DTO) 중심의 설계
* DAO (Data Access Object): DB 접근 로직 분리 (MemberDAO, ItemDAO 등)
* DTO (Data Transfer Object): 데이터 운반용 클래스 (내부 클래스로 정의된 PostDTO, ReservationSummary 등)

2. 로직 설명

시스템은 크게 회원, 물품 대여, 공간 예약, 커뮤니티, 관리자 기능(추첨, 패널티)으로 구성, DBUtil을 통해 중앙에서 DB 연결을 관리.

* 데이터 접근 계층 (DAO): DBUtil.getConnection()을 통해 DB 연결, PreparedStatement를 사용하여 SQL Injection을 방지하며 쿼리를 실행
* 물품 대여 로직:
* 대여 시 ItemDAO에서 available_stock(재고)을 감소시키고, RentDAO에 대여 기록을 INSERT
* 반납 시 is_returned 플래그를 업데이트하고 재고를 다시 증가


* 공간 예약 로직:
* 예약 시 SpaceReservationDAO가 해당 날짜/공간의 기존 예약(status='RESERVED')을 조회하여 중복 시간을 1차 검증
* 다중 시간 예약 시 Batch Processing(addBatch)과 Transaction(setAutoCommit(false))을 사용하여 모든 시간 슬롯이 성공적으로 들어갈 때만 예약이 확정되도록 처리


* 추첨(Lottery) 시스템:
* 포인트를 사용하여 응모, 응모 시 포인트 차감과 응모 내역 저장을 하나의 트랜잭션으로 묶어 데이터 무결성을 보장



3. 맡은 부분 (Backend & Database)

백엔드와 데이터베이스를 전담함

A. 데이터베이스 스키마 설계 및 구현

코드의 SQL문을 통해 다음과 같은 테이블 구조를 설계, 연동

* Members: 학번(PK), 비밀번호, 포인트, 회비 납부 여부 등
* Item / Rental: 물품 정보 및 대여 이력 관리
* Space_Reservation: 공간, 날짜, 시간 슬롯(`09:00~10:00` 등), 상태 관리
* Community_Post / Comment / Like: 게시판, 댓글, 좋아요 기능 구현
* Lottery_Round / Entry: 경품 추첨 회차 및 응모자 관리

B. 트랜잭션 관리 및 데이터 무결성 보장

단순 CRUD를 넘어, 데이터가 꼬이지 않도록 논리적인 처리를 수행

* 포인트 사용과 응모의 원자성: LotteryManager에서 포인트 차감 후 응모 실패 시 rollback()을 수행하여 포인트 증발을 방지
* 재고 동시성 제어 (기초): 물품 대여 시 available_stock > 0 조건을 UPDATE 문에 포함시켜 재고가 없을 때 대여되는 것을 DB 수준에서 막음

4. 문제 해결 방향 (Problem Solving)

주요 문제 해결

* 문제 1: 중복 예약 방지
* 해결: SpaceReservationDAO에서 getBookedTimeSlots 메소드로 이미 예약된 시간을 Set으로 로드하여 사용자가 선택한 시간과 O(1)로 비교 검증 후 예약을 진행


* 문제 2: DB 자원 누수 방지
* 해결: DBUtil 클래스에 close() 메소드를 중앙화, 모든 DAO의 finally 블록 또는 try-with-resources 구문을 통해 Connection, Statement, ResultSet을 확실하게 해제했습니다.


* 문제 3: 패널티 관리의 효율성
* 해결: PenaltyManager 에서 공간 예약 경고는 빈번하게 발생할 수 있어 메모리(HashMap)로 가볍게 관리하고, 대여 정지와 같이 장기간 유지되어야 하는 데이터는 DB(UserDAO)에 저장하는 방식을 채택



5. 성과 정리

1. 안정적인 데이터 처리 인프라 구축:DBUtil과 Singleton DAO 패턴을 도입하여 DB 연결 관리하고 코드의 재사용성을 높임
2. 복잡한 비즈니스 로직 구현: 단순 게시판을 넘어 시간 단위 공간 예약, 포인트 기반 추첨, 재고 관리 등 복잡한 요구사항을 SQL과 자바 로직으로 구현
3. 데이터 신뢰성 확보: 트랜잭션(commit/rollback) 처리를 적용하여 시스템 오류 발생 시에도 데이터가 오염되지 않도록 방어
4. 확장 가능한 구조: CommunityDAO에서 게시글 목록을 가져올 때 댓글 수를 서브쿼리로 가져오는 등 성능을 고려, DTO를 활용해 데이터를 구조화하여 프론트엔드(UI)와의 결합을 쉽게 처리
