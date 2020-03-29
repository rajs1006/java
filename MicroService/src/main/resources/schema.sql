DROP TABLE IF EXISTS `publications`;
DROP TABLE IF EXISTS `schedules`;
DROP TABLE IF EXISTS `properties`;
DROP TABLE IF EXISTS `services`;

-- These tables should not be dropped
-- DROP TABLE IF EXISTS `sitemaps`;
-- DROP TABLE IF EXISTS `executed_articles`;
-- DROP TABLE IF EXISTS `pending_articles`;
-- DROP TABLE IF EXISTS `failed_articles`;

CREATE TABLE IF NOT EXISTS `publications` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `publication` VARCHAR unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`publication`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `properties` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `publication_id` int unsigned NOT NULL,
  `sitemap_file` VARCHAR unsigned NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (publication_id) references publications(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `services` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `service` VARCHAR unsigned NOT NULL,
  `sequence` int unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `schedules` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `publication_id` int unsigned NOT NULL,
  `service_id` int unsigned NOT NULL,
  `enabled` BOOLEAN DEFAULT true,
  `delay` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (publication_id) references publications(id),
  FOREIGN KEY (service_id) references services(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- Article repository, populated at runtime

CREATE TABLE IF NOT EXISTS `sitemaps` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `publication` VARCHAR unsigned NOT NULL,
  `url` VARCHAR unsigned NOT NULL,
  `last_mod_date` VARCHAR unsigned,
  `execution_status` BOOLEAN unsigned NOT NULL default false,
  `load_date` TIMESTAMP unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`publication`, `url`, `last_mod_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `executed_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `publication` VARCHAR unsigned NOT NULL,
  `asset_id` VARCHAR unsigned NOT NULL,
  `url` VARCHAR unsigned NOT NULL,
  `execution_date` TIMESTAMP unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`asset_id`, `publication`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `pending_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `publication` VARCHAR unsigned NOT NULL,
  `url` VARCHAR unsigned NOT NULL,
  `execution_date` TIMESTAMP unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `failed_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `publication` VARCHAR unsigned NOT NULL,
  `url` VARCHAR unsigned NOT NULL,
  `execution_date` TIMESTAMP unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;