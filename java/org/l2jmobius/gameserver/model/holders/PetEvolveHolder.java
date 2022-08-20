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

import org.l2jmobius.gameserver.enums.EvolveLevel;

/**
 * Written by Berezkin Nikolay, on 16.05.2021
 */
public class PetEvolveHolder
{
	private final int _index;
	private final int _level;
	private final EvolveLevel _evolve;
	private final long _exp;
	private final String _name;
	
	public PetEvolveHolder(int index, int evolve, String name, int level, long exp)
	{
		_index = index;
		_evolve = EvolveLevel.values()[evolve];
		_level = level;
		_exp = exp;
		_name = name;
	}
	
	public int getIndex()
	{
		return _index;
	}
	
	public EvolveLevel getEvolve()
	{
		return _evolve;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public String getName()
	{
		return _name;
	}
}
