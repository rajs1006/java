CREATE DATABASE 'auto_tagging' DEFAULT CHARACTER SET 'utf8';
CREATE USER 'semantic'@'%' IDENTIFIED BY 'semantic';
GRANT ALL PRIVILEGES ON auto_tagging.* TO 'semantic'@'%';
FLUSH PRIVILEGES;
