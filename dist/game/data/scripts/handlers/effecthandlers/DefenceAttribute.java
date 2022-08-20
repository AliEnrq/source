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
package handlers.effecthandlers;

import org.l2jmobius.gameserver.enums.AttributeType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class DefenceAttribute extends AbstractEffect
{
	private final AttributeType _attribute;
	private final double _amount;
	
	public DefenceAttribute(StatSet params)
	{
		_amount = params.getDouble("amount", 0);
		_attribute = params.getEnum("attribute", AttributeType.class, AttributeType.FIRE);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		Stat stat = Stat.FIRE_RES;
		
		switch (_attribute)
		{
			case WATER:
			{
				stat = Stat.WATER_RES;
				break;
			}
			case WIND:
			{
				stat = Stat.WIND_RES;
				break;
			}
			case EARTH:
			{
				stat = Stat.EARTH_RES;
				break;
			}
			case HOLY:
			{
				stat = Stat.HOLY_RES;
				break;
			}
			case DARK:
			{
				stat = Stat.DARK_RES;
				break;
			}
		}
		effected.getStat().mergeAdd(stat, _amount);
	}
}
