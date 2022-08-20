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

import org.l2jmobius.gameserver.enums.StatModifierType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Mobius
 */
public class StatMulForBaseStat extends AbstractEffect
{
	private final BaseStat _baseStat;
	private final int _min;
	private final int _max;
	private final Stat _mulStat;
	private final double _amount;
	
	public StatMulForBaseStat(StatSet params)
	{
		_baseStat = params.getEnum("baseStat", BaseStat.class);
		_min = params.getInt("min", 0);
		_max = params.getInt("max", 2147483647);
		_mulStat = params.getEnum("mulStat", Stat.class);
		_amount = params.getDouble("amount", 0);
		if (params.getEnum("mode", StatModifierType.class, StatModifierType.PER) != StatModifierType.PER)
		{
			LOGGER.warning(getClass().getSimpleName() + " can only use PER mode.");
		}
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		int currentValue = 0;
		switch (_baseStat)
		{
			case STR:
			{
				currentValue = effected.getSTR();
				break;
			}
			case INT:
			{
				currentValue = effected.getINT();
				break;
			}
			case DEX:
			{
				currentValue = effected.getDEX();
				break;
			}
			case WIT:
			{
				currentValue = effected.getWIT();
				break;
			}
			case CON:
			{
				currentValue = effected.getCON();
				break;
			}
			case MEN:
			{
				currentValue = effected.getMEN();
				break;
			}
		}
		
		if ((currentValue >= _min) && (currentValue <= _max))
		{
			effected.getStat().mergeMul(_mulStat, (_amount / 100) + 1);
		}
	}
}
