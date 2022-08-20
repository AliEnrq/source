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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.IStatFunction;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author UnAfraid
 */
public class PCriticalRateFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		throwIfPresent(base);
		
		double baseValue = calcWeaponBaseValue(creature, stat);
		if (creature.isPlayer())
		{
			// Enchanted legs bonus
			baseValue += calcEnchantBodyPart(creature, ItemTemplate.SLOT_LEGS);
		}
		final double dexBonus = creature.getDEX() > 0 ? BaseStat.DEX.calcBonus(creature) : 1;
		return validateValue(creature, Stat.defaultValue(creature, stat, baseValue * dexBonus * 10), 0, creature.isPlayable() ? Config.MAX_PCRIT_RATE : Double.MAX_VALUE);
	}
	
	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		if (isBlessed)
		{
			return (0.5 * Math.max(enchantLevel - 3, 0)) + (0.5 * Math.max(enchantLevel - 6, 0));
		}
		return (0.34 * Math.max(enchantLevel - 3, 0)) + (0.34 * Math.max(enchantLevel - 6, 0));
	}
}
