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
 * Written by Berezkin Nikolay, on 11.05.2021
 */
public class ReplaceSkillEntryHolder
{
	private final int _abnormalSkillId;
	private final int _abnormalSkillLvl;
	private final int _originalSkillId;
	private final int _replaceSkillId;
	
	public ReplaceSkillEntryHolder(int abnormalSkillId, int abnormalSkillLvl, int originalSkillId, int replaceSkillId)
	{
		_abnormalSkillId = abnormalSkillId;
		_abnormalSkillLvl = abnormalSkillLvl;
		_originalSkillId = originalSkillId;
		_replaceSkillId = replaceSkillId;
	}
	
	public int getAbnormalSkillId()
	{
		return _abnormalSkillId;
	}
	
	public int getAbnormalSkillLvl()
	{
		return _abnormalSkillLvl;
	}
	
	public int getOriginalSkillId()
	{
		return _originalSkillId;
	}
	
	public int getReplaceSkillId()
	{
		return _replaceSkillId;
	}
}
