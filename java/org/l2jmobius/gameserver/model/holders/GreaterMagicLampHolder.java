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

import org.l2jmobius.gameserver.model.StatSet;

/**
 * @author L2CCCP
 */
public class GreaterMagicLampHolder
{
	private final int _itemId;
	private final long _count;
	
	public GreaterMagicLampHolder(StatSet params)
	{
		_itemId = params.getInt("item");
		_count = params.getLong("count");
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public long getCount()
	{
		return _count;
	}
}