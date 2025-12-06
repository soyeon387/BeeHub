1) MySQL Workbench 실행
2) 아래 명령으로 스키마 생성:
   SOURCE db/beehub_schema.sql;

3) 샘플 데이터 넣기:
   SOURCE db/beehub_sample_data.sql;

4) DBUtil.java 설정:
   URL = jdbc:mysql://localhost:3306/beehub
   USER = root
   PASS = 각자 비번

5) BeeHub 실행
