DROP TABLE IF EXISTS `collections`;
CREATE TABLE IF NOT EXISTS `collections` (
  `accountName` VARCHAR(45) NOT NULL DEFAULT '',
  `itemId` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `collectionId` int(3) UNSIGNED NOT NULL DEFAULT 0,
  `index` tinyint(3) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`accountName`,`collectionId`,`index`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;