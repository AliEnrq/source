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

/**
 * @author Berezkin Nikolay
 */
public class PetSkillAcquireHolder
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _reqLvl;
	private final int _evolve;
	private final ItemHolder _item;
	
	public PetSkillAcquireHolder(int skillId, int skillLevel, int reqLvl, int evolve, ItemHolder item)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
		_reqLvl = reqLvl;
		_evolve = evolve;
		_item = item;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	public int getReqLvl()
	{
		return _reqLvl;
	}
	
	public int getEvolve()
	{
		return _evolve;
	}
	
	public ItemHolder getItem()
	{
		return _item;
	}
}
