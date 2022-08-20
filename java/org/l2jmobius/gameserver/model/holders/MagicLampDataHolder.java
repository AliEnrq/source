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

import org.l2jmobius.gameserver.enums.LampMode;
import org.l2jmobius.gameserver.enums.LampType;
import org.l2jmobius.gameserver.model.StatSet;

/**
 * @author L2CCCP
 */
public class MagicLampDataHolder
{
	private final LampMode _mode;
	private final LampType _type;
	private final long _exp;
	private final long _sp;
	private final double _chance;
	
	public MagicLampDataHolder(StatSet params)
	{
		_mode = params.getEnum("mode", LampMode.class);
		_type = params.getEnum("type", LampType.class);
		_exp = params.getLong("exp");
		_sp = params.getLong("sp");
		_chance = params.getDouble("chance");
	}
	
	public LampMode getMode()
	{
		return _mode;
	}
	
	public LampType getType()
	{
		return _type;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public long getSp()
	{
		return _sp;
	}
	
	public double getChance()
	{
		return _chance;
	}
}