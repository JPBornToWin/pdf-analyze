create database pdf_analyze;

create table json_task (
  id int not null auto_increment,
  checksum varchar(100) not null,
  status smallint(6) not null,
  PRIMARY key(id)
)


create table content_task (
  id int not null auto_increment,
  checksum varchar(100) not null,
  status smallint(6) not null,
  PRIMARY key(id)
)