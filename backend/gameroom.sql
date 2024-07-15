CREATE TABLE `university`.`gamerooms` (
  `gid` VARCHAR(8) NOT NULL,
  `is_start` TINYINT NOT NULL DEFAULT 0,
  `owner` varchar(50) NOT NULL,
  `players` varchar(250) NOT NULL,
  PRIMARY KEY (`gid`),
  UNIQUE INDEX `gid_UNIQUE` (`gid` ASC) VISIBLE);