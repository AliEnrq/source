DROP TABLE IF EXISTS `pet_skills`;
CREATE TABLE IF NOT EXISTS `pet_skills` (
  `petObjItemId` INT NOT NULL DEFAULT 0,
  `skillId` INT NOT NULL DEFAULT 0,
  `skillLevel` INT(3) NOT NULL DEFAULT 1,
  PRIMARY KEY (`petObjItemId`,`skillId`,`skillLevel`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;