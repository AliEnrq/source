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
 * @author Mode
 */
public class RandomCraftRewardDataHolder
{
	private final int _itemId;
	private final long _count;
	private final double _chance;
	private final boolean _announce;
	
	public RandomCraftRewardDataHolder(int itemId, long count, double chance, boolean announce)
	{
		_itemId = itemId;
		_count = count;
		_chance = chance;
		_announce = announce;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public long getCount()
	{
		return _count;
	}
	
	public double getChance()
	{
		return _chance;
	}
	
	public boolean isAnnounce()
	{
		return _announce;
	}
}
