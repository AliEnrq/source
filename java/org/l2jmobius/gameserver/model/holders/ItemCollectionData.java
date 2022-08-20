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
 * Written by Berezkin Nikolay, on 04.05.2021
 */
public class ItemCollectionData
{
	private final int _enchantLevel;
	private final int _itemId;
	private final long _count;
	
	public ItemCollectionData(int itemId, long count, int enchantLevel)
	{
		_itemId = itemId;
		_count = count;
		_enchantLevel = enchantLevel;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public long getCount()
	{
		return _count;
	}
}
