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
public class PlayerCollectionData
{
	private final int _collectionId;
	private final int _itemId;
	private final int _index;
	
	public PlayerCollectionData(int collectionId, int itemId, int index)
	{
		_collectionId = collectionId;
		_itemId = itemId;
		_index = index;
	}
	
	public int getCollectionId()
	{
		return _collectionId;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getIndex()
	{
		return _index;
	}
}