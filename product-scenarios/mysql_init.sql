CREATE DATABASE test;

USE test;

CREATE TABLE `selecttest` (
  `id` int(11) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`));

INSERT INTO selecttest values(1, "Dan Brown");

INSERT INTO selecttest values(2, "J K Rowling");

