DROP TABLE IF EXISTS `character_purge`;
CREATE TABLE IF NOT EXISTS `character_purge` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `category` int(3) UNSIGNED NOT NULL DEFAULT 0,
  `points` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `keys` int(10) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`category`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;