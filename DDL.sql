create database pdf_analyze;

create table document
(
	id bigint auto_increment
		primary key,
	create_time datetime null,
	user_id bigint null,
	status tinyint null,
	file_name varchar(100) null,
	author varchar(100) null,
	title varchar(100) null,
	blob_id bigint null,
	area_version int null
)
;

create table pdf_blob
(
	id bigint auto_increment
		primary key,
	create_time datetime null,
	status tinyint null,
	user_id bigint null,
	file_size bigint null,
	checksum varchar(300) null
)
;

create table pdf_block
(
	id bigint auto_increment
		primary key,
	block_order int not null,
	type tinyint null,
	checksum varchar(300) null,
	text_body text null,
	font_family varchar(300) null,
	font_size tinyint null,
	x1 float null,
	y1 float null,
	x2 float null,
	y2 float null,
	page_id bigint not null
)
;

create table pdf_page
(
	id bigint auto_increment
		primary key,
	page_index int null,
	width float null,
	height float null,
	lines_space float null,
	font_size tinyint null,
	font_family varchar(100) null,
	column_width float null,
	blob_id bigint null
)
;

create table pdf_page_block
(
	page_id bigint null,
	area_version int null,
	pdf_block_id bigint null
)
;

create table task
(
	id bigint auto_increment
		primary key,
	error_info varchar(100) null,
	status tinyint default '0' null,
	doc_id bigint null
)
;

create table user_account
(
	id bigint auto_increment
		primary key,
	username varchar(30) not null,
	gender tinyint null,
	salt varchar(50) not null,
	password varchar(100) null,
	phone varchar(20) null,
	email varchar(50) null,
	birthday datetime null,
	address varchar(100) null,
	status tinyint default '0' null,
	create_time datetime null,
	secret varchar(50) null,
	constraint username
		unique (username)
)
;

