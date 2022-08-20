DROP TABLE IF EXISTS `character_potens`;
CREATE TABLE IF NOT EXISTS `character_potens` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `enchant_level` INT,
  `enchant_exp` INT,
  `poten_id` INT,
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;