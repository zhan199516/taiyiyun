create database passport character set utf8 collate utf8_general_ci;
create user 'passport'@'%' identified by 'passport123';
grant all on passport.* to passport;