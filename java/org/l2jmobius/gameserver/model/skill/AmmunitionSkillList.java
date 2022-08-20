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
package org.l2jmobius.gameserver.model.skill;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.model.holders.ItemSkillHolder;

/**
 * @author Mobius
 */
public class AmmunitionSkillList
{
	private static final Set<Integer> SKILLS = ConcurrentHashMap.newKeySet();
	
	public static void add(List<ItemSkillHolder> skills)
	{
		for (ItemSkillHolder skill : skills)
		{
			SKILLS.add(skill.getSkillId());
		}
	}
	
	public static Set<Integer> values()
	{
		return SKILLS;
	}
}
