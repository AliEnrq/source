DROP TABLE IF EXISTS `character_pledge_donation`;
CREATE TABLE IF NOT EXISTS `character_pledge_donation` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `points` int(10) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;