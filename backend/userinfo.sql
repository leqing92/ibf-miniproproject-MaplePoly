CREATE TABLE `university`.`userinfo` ( 
  `id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NULL,
  `name` VARCHAR(255) NULL,
  `password` VARCHAR(255) NULL,
  `roles` VARCHAR(255) NULL,
  `gid` VARCHAR(8) NULL,
  `dob` DATETIME(6) NULL,
  `inGame` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE,
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
