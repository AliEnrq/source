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
public class RandomCraftRewardItemHolder
{
	private final int _id;
	private final long _count;
	private boolean _locked;
	private int _lockLeft;
	
	public RandomCraftRewardItemHolder(int id, long count, boolean locked, int lockLeft)
	{
		_id = id;
		_count = count;
		_locked = locked;
		_lockLeft = lockLeft;
	}
	
	public int getItemId()
	{
		return _id;
	}
	
	public long getItemCount()
	{
		return _count;
	}
	
	public boolean isLocked()
	{
		return _locked;
	}
	
	public int getLockLeft()
	{
		return _lockLeft;
	}
	
	public void lock()
	{
		_locked = true;
	}
	
	public void decLock()
	{
		_lockLeft--;
		if (_lockLeft <= 0)
		{
			_locked = false;
		}
	}
}
