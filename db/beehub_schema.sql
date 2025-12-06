-- ===========================================
-- BeeHub FULL SCHEMA
-- ===========================================

CREATE DATABASE IF NOT EXISTS beehub
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE beehub;

-- ===========================================
-- members
-- ===========================================
CREATE TABLE IF NOT EXISTS members (
  hakbun              VARCHAR(20)  PRIMARY KEY,
  pw                  VARCHAR(100) NOT NULL,
  name                VARCHAR(50)  NOT NULL,
  nickname            VARCHAR(50),
  major               VARCHAR(100),
  phone               VARCHAR(20),
  is_fee_paid         CHAR(1)      DEFAULT 'N',
  point               INT          DEFAULT 0,
  grade               VARCHAR(20),
  penalty_date        VARCHAR(20),
  warning_count       INT          DEFAULT 0,
  role                VARCHAR(20)  DEFAULT 'USER',
  rental_ban_end_date DATE
) ENGINE=InnoDB;

-- ===========================================
-- community_post
-- ===========================================
CREATE TABLE IF NOT EXISTS community_post (
  post_id          INT AUTO_INCREMENT PRIMARY KEY,
  writer_hakbun    VARCHAR(20)  NOT NULL,
  writer_nickname  VARCHAR(50)  NOT NULL,
  title            VARCHAR(200) NOT NULL,
  content          TEXT         NOT NULL,
  like_count       INT          DEFAULT 0,
  comment_count    INT          DEFAULT 0,
  is_notice        TINYINT(1)   DEFAULT 0,
  is_deleted       TINYINT(1)   DEFAULT 0,
  created_at       DATETIME     NOT NULL,
  updated_at       DATETIME,
  reward_given     TINYINT(1)   DEFAULT 0,
  FOREIGN KEY (writer_hakbun) REFERENCES members(hakbun)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_post_created_at ON community_post (created_at);
CREATE INDEX idx_post_notice ON community_post (is_notice);

-- ===========================================
-- community_comment
-- ===========================================
CREATE TABLE IF NOT EXISTS community_comment (
  comment_id       INT AUTO_INCREMENT PRIMARY KEY,
  post_id          INT          NOT NULL,
  writer_hakbun    VARCHAR(20)  NOT NULL,
  writer_nickname  VARCHAR(50)  NOT NULL,
  content          VARCHAR(500) NOT NULL,
  created_at       DATETIME     NOT NULL,
  updated_at       DATETIME,
  FOREIGN KEY (post_id) REFERENCES community_post(post_id)
    ON DELETE CASCADE,
  FOREIGN KEY (writer_hakbun) REFERENCES members(hakbun)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_comment_post ON community_comment (post_id);

-- ===========================================
-- community_post_like
-- ===========================================
CREATE TABLE IF NOT EXISTS community_post_like (
  post_id      INT         NOT NULL,
  liker_hakbun VARCHAR(20) NOT NULL,
  created_at   DATETIME    NOT NULL,
  PRIMARY KEY (post_id, liker_hakbun),
  FOREIGN KEY (post_id) REFERENCES community_post(post_id)
    ON DELETE CASCADE,
  FOREIGN KEY (liker_hakbun) REFERENCES members(hakbun)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===========================================
-- events
-- ===========================================
CREATE TABLE IF NOT EXISTS events (
  event_id           INT AUTO_INCREMENT PRIMARY KEY,
  event_type         ENUM('SNACK','ACTIVITY') NOT NULL,
  event_name         VARCHAR(100) NOT NULL,
  event_date         DATETIME,
  location           VARCHAR(200),
  apply_start        DATETIME,
  apply_end          DATETIME,
  total_quantity     INT,
  remaining_quantity INT,
  secret_code        VARCHAR(50),
  description        TEXT,
  status             ENUM('SCHEDULED','PROGRESS','CLOSED') DEFAULT 'SCHEDULED',
  target_major       VARCHAR(100),
  created_at         DATETIME NOT NULL,
  updated_at         DATETIME,
  owner_hakbun       VARCHAR(20),
  FOREIGN KEY (owner_hakbun) REFERENCES members(hakbun)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ===========================================
-- event_participation
-- ===========================================
CREATE TABLE IF NOT EXISTS event_participation (
  participation_id   INT AUTO_INCREMENT PRIMARY KEY,
  event_id           INT NOT NULL,
  participant_hakbun VARCHAR(20) NOT NULL,
  participation_type ENUM('APPLY','PICKUP') NOT NULL,
  participation_date DATETIME NOT NULL,
  quantity           INT DEFAULT 1,
  note               VARCHAR(255),
  FOREIGN KEY (event_id) REFERENCES events(event_id)
    ON DELETE CASCADE,
  FOREIGN KEY (participant_hakbun) REFERENCES members(hakbun)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_event_participation_event ON event_participation (event_id);

-- ===========================================
-- item
-- ===========================================
CREATE TABLE IF NOT EXISTS item (
  item_id         INT AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(100) NOT NULL,
  total_stock     INT NOT NULL,
  available_stock INT NOT NULL,
  max_rent_days   INT NOT NULL,
  target_major    VARCHAR(50),
  image_path      VARCHAR(255),
  is_active       TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

-- ===========================================
-- rental
-- ===========================================
CREATE TABLE IF NOT EXISTS rental (
  rental_id    INT AUTO_INCREMENT PRIMARY KEY,
  item_id      INT          NOT NULL,
  item_name    VARCHAR(100) NOT NULL,
  renter_id    VARCHAR(50)  NOT NULL,
  renter_name  VARCHAR(50)  NOT NULL,
  rent_date    DATE NOT NULL,
  due_date     DATE NOT NULL,
  return_date  DATE,
  is_returned  TINYINT(1) DEFAULT 0,
  FOREIGN KEY (item_id) REFERENCES item(item_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_rental_item ON rental(item_id);

-- ===========================================
-- lottery_round
-- ===========================================
CREATE TABLE IF NOT EXISTS lottery_round (
  round_id          INT AUTO_INCREMENT PRIMARY KEY,
  round_name        VARCHAR(100) NOT NULL,
  prize_name        VARCHAR(100) NOT NULL,
  winner_count      INT NOT NULL,
  announcement_date DATE,
  application_start DATETIME,
  application_end   DATETIME,
  pickup_location   VARCHAR(100),
  pickup_start      DATETIME,
  pickup_end        DATETIME,
  is_drawn          TINYINT(1) DEFAULT 0,
  created_at        DATETIME NOT NULL
) ENGINE=InnoDB;

-- ===========================================
-- lottery_entry
-- ===========================================
CREATE TABLE IF NOT EXISTS lottery_entry (
  raffle_id   INT AUTO_INCREMENT PRIMARY KEY,
  round_id    INT         NOT NULL,
  hakbun      VARCHAR(20) NOT NULL,
  entry_count INT DEFAULT 1,
  is_win      CHAR(1) DEFAULT 'N',
  created_at  DATETIME NOT NULL,
  FOREIGN KEY (round_id) REFERENCES lottery_round(round_id)
    ON DELETE CASCADE,
  FOREIGN KEY (hakbun) REFERENCES members(hakbun)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_lottery_round ON lottery_entry (round_id);

-- ===========================================
-- space_info
-- ===========================================
CREATE TABLE IF NOT EXISTS space_info (
  space_id      INT AUTO_INCREMENT PRIMARY KEY,
  building_name VARCHAR(45) NOT NULL,
  room_name     VARCHAR(45) NOT NULL,
  min_people    INT,
  max_people    INT,
  oper_time     VARCHAR(45),
  room_type     VARCHAR(45),
  is_active     TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

-- ===========================================
-- space_reservation
-- ===========================================
CREATE TABLE IF NOT EXISTS space_reservation (
  reservation_id INT AUTO_INCREMENT PRIMARY KEY,
  space_id       INT NOT NULL,
  reserve_date   DATE NOT NULL,
  time_slot      VARCHAR(20) NOT NULL,
  hakbun         VARCHAR(20) NOT NULL,
  status         VARCHAR(20) NOT NULL,
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (space_id) REFERENCES space_info(space_id)
    ON DELETE CASCADE,
  FOREIGN KEY (hakbun) REFERENCES members(hakbun)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_space_reservation
  ON space_reservation (space_id, reserve_date, time_slot);
