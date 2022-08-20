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
package org.l2jmobius.gameserver.model.conditions;

import org.l2jmobius.gameserver.enums.SoulType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * The Class ConditionPlayerSouls.
 */
public class ConditionPlayerSouls extends Condition
{
	private final int _souls;
	private final SoulType _type;
	
	/**
	 * Instantiates a new condition player souls.
	 * @param souls the souls
	 * @param type the soul type
	 */
	public ConditionPlayerSouls(int souls, SoulType type)
	{
		_souls = souls;
		_type = type;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return (effector.getActingPlayer() != null) && (effector.getActingPlayer().getChargedSouls(_type) >= _souls);
	}
}
