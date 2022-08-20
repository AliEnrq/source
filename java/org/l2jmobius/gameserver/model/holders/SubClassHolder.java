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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.ClassId;

/**
 * Character Sub-Class Definition<br>
 * Used to store key information about a character's sub-class.
 * @author Tempy
 */
public class SubClassHolder
{
	private static final int MAX_LEVEL = Config.MAX_SUBCLASS_LEVEL < ExperienceData.getInstance().getMaxLevel() ? Config.MAX_SUBCLASS_LEVEL : ExperienceData.getInstance().getMaxLevel() - 1;
	
	private static final int MAX_VITALITY_POINTS = 3500000;
	private static final int MIN_VITALITY_POINTS = 0;
	
	private ClassId _class;
	private long _exp = ExperienceData.getInstance().getExpForLevel(Config.BASE_SUBCLASS_LEVEL);
	private long _sp = 0;
	private int _level = Config.BASE_SUBCLASS_LEVEL;
	private int _classIndex = 1;
	private int _vitalityPoints = 0;
	private boolean _dualClass = false;
	
	public SubClassHolder()
	{
		// Used for specifying ALL attributes of a sub class directly,
		// using the preset default values.
	}
	
	public ClassId getClassDefinition()
	{
		return _class;
	}
	
	public int getClassId()
	{
		return _class.getId();
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public long getSp()
	{
		return _sp;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getVitalityPoints()
	{
		return Math.min(Math.max(_vitalityPoints, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
	}
	
	public void setVitalityPoints(int value)
	{
		_vitalityPoints = Math.min(Math.max(value, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
	}
	
	/**
	 * First Sub-Class is index 1.
	 * @return int _classIndex
	 */
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	public void setClassId(int classId)
	{
		_class = ClassId.getClassId(classId);
	}
	
	public void setExp(long expValue)
	{
		if (!_dualClass && (expValue > (ExperienceData.getInstance().getExpForLevel(MAX_LEVEL + 1) - 1)))
		{
			_exp = ExperienceData.getInstance().getExpForLevel(MAX_LEVEL + 1) - 1;
			return;
		}
		_exp = expValue;
	}
	
	public void setSp(long spValue)
	{
		_sp = spValue;
	}
	
	public void setClassIndex(int classIndex)
	{
		_classIndex = classIndex;
	}
	
	public boolean isDualClass()
	{
		return _dualClass;
	}
	
	public void setDualClassActive(boolean dualClass)
	{
		_dualClass = dualClass;
	}
	
	public void setLevel(int levelValue)
	{
		if (!_dualClass && (levelValue > MAX_LEVEL))
		{
			_level = MAX_LEVEL;
			return;
		}
		else if (levelValue < Config.BASE_SUBCLASS_LEVEL)
		{
			_level = Config.BASE_SUBCLASS_LEVEL;
			return;
		}
		_level = levelValue;
	}
}
