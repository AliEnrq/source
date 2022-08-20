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
package org.l2jmobius.gameserver.model.item.henna;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * @author Serenitty
 */
public class DyePotential
{
	private final int _id;
	private final int _slotId;
	private final int _skillId;
	private final Skill[] _skills;
	private final int _maxSkillLevel;
	
	public DyePotential(int id, int slotId, int skillId, int maxSkillLevel)
	{
		_id = id;
		_slotId = slotId;
		_skillId = skillId;
		_skills = new Skill[maxSkillLevel];
		for (int i = 1; i <= maxSkillLevel; i++)
		{
			_skills[i - 1] = SkillData.getInstance().getSkill(skillId, i);
		}
		_maxSkillLevel = maxSkillLevel;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getSlotId()
	{
		return _slotId;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public Skill getSkill(int level)
	{
		return _skills[level - 1];
	}
	
	public int getMaxSkillLevel()
	{
		return _maxSkillLevel;
	}
}