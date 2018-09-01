CREATE DATABASE VaultDragon;

USE VaultDragon;

CREATE TABLE Repository (
id int(11) NOT NULL auto_increment,
skey varchar(50) NOT NULL,
svalue varchar(50) NOT NULL,
dtime timestamp NOT NULL,
PRIMARY KEY (id)
);

