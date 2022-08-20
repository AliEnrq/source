DROP TABLE IF EXISTS `pet_evolves`;
CREATE TABLE `pet_evolves` (
	`itemObjId` bigint NOT NULL DEFAULT '0',
	`index` int NOT NULL DEFAULT '0',
	`level` int NOT NULL DEFAULT '0',
	PRIMARY KEY  (`itemObjId`, `index`, `level`),
    UNIQUE KEY `pet_evolves` (`itemObjId`, `index`, `level`)
) ENGINE=MyISAM;