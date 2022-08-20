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
package org.l2jmobius.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.IStatFunction;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;

/**
 * @author UnAfraid
 */
public class BaseStatFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		throwIfPresent(base);
		
		// Apply template value
		double baseValue = creature.getTemplate().getBaseValue(stat, 0);
		
		// Should not apply armor set and henna bonus to summons.
		if (creature.isPlayer())
		{
			final Player player = creature.getActingPlayer();
			
			// Armor sets calculation
			baseValue += player.getInventory().getPaperdollCache().getBaseStatValue(player, BaseStat.valueOf(stat));
			
			// Henna calculation
			baseValue += player.getHennaValue(BaseStat.valueOf(stat));
			
			// Bonus stats
			switch (stat)
			{
				case STAT_STR:
				{
					baseValue += player.getVariables().getInt(PlayerVariables.STAT_STR, 0);
					break;
				}
				case STAT_CON:
				{
					baseValue += player.getVariables().getInt(PlayerVariables.STAT_CON, 0);
					break;
				}
				case STAT_DEX:
				{
					baseValue += player.getVariables().getInt(PlayerVariables.STAT_DEX, 0);
					break;
				}
				case STAT_INT:
				{
					baseValue += player.getVariables().getInt(PlayerVariables.STAT_INT, 0);
					break;
				}
				case STAT_MEN:
				{
					baseValue += player.getVariables().getInt(PlayerVariables.STAT_MEN, 0);
					break;
				}
				case STAT_WIT:
				{
					baseValue += player.getVariables().getInt(PlayerVariables.STAT_WIT, 0);
					break;
				}
			}
		}
		
		return validateValue(creature, Stat.defaultValue(creature, stat, baseValue), 1, BaseStat.MAX_STAT_VALUE - 1);
	}
}
