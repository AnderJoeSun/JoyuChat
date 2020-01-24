drop database if exists joyuchat;
create database joyuchat;
use joyuchat;
CREATE TABLE managers (
  username varchar(20) NOT NULL primary key,
 password varchar(20) NOT NULL,
 realname varchar(20) NOT NULL,
 Sex varchar(10) NOT null,
 Email varchar(80) ,
 phone varchar(20) ,
 address varchar(100)
);
CREATE TABLE users (
  username varchar(20) NOT NULL primary key,
 password varchar(20) NOT NULL,
 realname varchar(20) NOT NULL,
 Sex varchar(10) NOT null,
 Email varchar(80) ,
 phone varchar(20) ,
 address varchar(100)
);
CREATE TABLE filesForShare (
    fileName varchar(100) NOT NULL primary key,
    status varchar(10) NOT NULL,
 fileKind varchar(10) NOT NULL,
 fileSize varchar(30) NOT NULL,
 fileFrom varchar(20) NOT null,
 fileCreatedTime varchar(20) ,
 fileModifiedTime varchar(20) ,
 savePath varchar(200)
 
);
CREATE TABLE log_Info (
    id int not null primary key AUTO_INCREMENT,
    username varchar(20) NOT NULL,
 thetime varchar(50) NOT NULL,
 status varchar(10) NOT NULL
 
);
 
insert into users values('a','a','阳光','男','abc@qq.com','123456','china');
insert into users values('b','b','夏雪','女','abc@qq.com','123456','china');
insert into users values('d','d','快乐','男','abc@qq.com','123456','china');
insert into managers values('admin','admin','阳光','男','abc@qq.com','123456','china');
CREATE TABLE messages (
  username varchar(20) NOT NULL,
 thetime varchar(50) NOT NULL,
 message varchar(30000) NOT NULL
 
);