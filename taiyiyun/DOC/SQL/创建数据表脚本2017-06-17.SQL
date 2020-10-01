/**
 * 用户表（测试） 2017-06-17
 */
drop table if exists passport_user;
create table passport_user (
	id integer not null primary key auto_increment comment 'ID', 
	user_name varchar(32) not null unique comment '用户名', 
	`password` varchar(32) not null comment '密码', 
	`age` integer not null default 0 comment '年龄'
)  engine=InnoDB default charset=utf8 comment '用户表（测试）';