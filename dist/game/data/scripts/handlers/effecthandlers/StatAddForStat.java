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
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Mobius
 */
public class StatAddForStat extends AbstractEffect
{
	private final Stat _stat;
	private final int _min;
	private final int _max;
	private final Stat _addStat;
	private final double _amount;
	
	public StatAddForStat(StatSet params)
	{
		_stat = params.getEnum("stat", Stat.class);
		_min = params.getInt("min", 0);
		_max = params.getInt("max", 2147483647);
		_addStat = params.getEnum("addStat", Stat.class);
		_amount = params.getDouble("amount", 0);
		if (params.getEnum("mode", StatModifierType.class, StatModifierType.DIFF) != StatModifierType.DIFF)
		{
			LOGGER.warning(getClass().getSimpleName() + " can only use DIFF mode.");
		}
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		final int currentValue = (int) effected.getStat().getValue(_stat);
		if ((currentValue >= _min) && (currentValue <= _max))
		{
			effected.getStat().mergeAdd(_addStat, _amount);
		}
	}
}
