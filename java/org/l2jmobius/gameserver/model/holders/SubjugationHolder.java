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

import java.util.List;
import java.util.Map;

/**
 * Written by Berezkin Nikolay, on 04.05.2021
 */
public class SubjugationHolder
{
	private final int _category;
	private final List<int[]> _hottimes;
	private final Map<Integer, Integer> _npcs;
	
	public SubjugationHolder(int category, List<int[]> hottimes, Map<Integer, Integer> npcs)
	{
		_category = category;
		_hottimes = hottimes;
		_npcs = npcs;
	}
	
	public int getCategory()
	{
		return _category;
	}
	
	public List<int[]> getHottimes()
	{
		return _hottimes;
	}
	
	public Map<Integer, Integer> getNpcs()
	{
		return _npcs;
	}
}
