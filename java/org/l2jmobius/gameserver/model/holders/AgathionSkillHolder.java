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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * @author Mobius
 */
public class AgathionSkillHolder
{
	private final Map<Integer, List<Skill>> _mainSkill;
	private final Map<Integer, List<Skill>> _subSkill;
	
	public AgathionSkillHolder(Map<Integer, List<Skill>> mainSkill, Map<Integer, List<Skill>> subSkill)
	{
		_mainSkill = mainSkill;
		_subSkill = subSkill;
	}
	
	public Map<Integer, List<Skill>> getMainSkills()
	{
		return _mainSkill;
	}
	
	public Map<Integer, List<Skill>> getSubSkills()
	{
		return _subSkill;
	}
	
	public List<Skill> getMainSkills(int enchantLevel)
	{
		if (!_mainSkill.containsKey(enchantLevel))
		{
			return Collections.emptyList();
		}
		return _mainSkill.get(enchantLevel);
	}
	
	public List<Skill> getSubSkills(int enchantLevel)
	{
		if (!_subSkill.containsKey(enchantLevel))
		{
			return Collections.emptyList();
		}
		return _subSkill.get(enchantLevel);
	}
}
