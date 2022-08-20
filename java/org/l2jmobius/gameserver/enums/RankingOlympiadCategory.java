/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.enums;

import static org.l2jmobius.gameserver.enums.RankingOlympiadScope.SELF;
import static org.l2jmobius.gameserver.enums.RankingOlympiadScope.TOP_100;
import static org.l2jmobius.gameserver.enums.RankingOlympiadScope.TOP_50;

/**
 * Written by Berezkin Nikolay, on 10.05.2021
 */
public enum RankingOlympiadCategory
{
	SERVER,
	CLASS;
	
	public RankingOlympiadScope getScopeByGroup(int id)
	{
		switch (this)
		{
			case SERVER:
			{
				return id == 0 ? TOP_100 : SELF;
			}
			case CLASS:
			{
				return id == 0 ? TOP_50 : SELF;
			}
			default:
			{
				return null;
			}
		}
	}
}
