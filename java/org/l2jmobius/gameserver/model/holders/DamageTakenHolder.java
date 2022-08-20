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
package org.l2jmobius.gameserver.model.holders;

import org.l2jmobius.gameserver.model.actor.Creature;

/**
 * @author Mobius
 */
public class DamageTakenHolder
{
	private final Creature _creature;
	private final int _skillId;
	private final double _damage;
	
	public DamageTakenHolder(Creature creature, int skillId, double damage)
	{
		_creature = creature;
		_skillId = skillId;
		_damage = damage;
	}
	
	public Creature getCreature()
	{
		return _creature;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public double getDamage()
	{
		return _damage;
	}
}
