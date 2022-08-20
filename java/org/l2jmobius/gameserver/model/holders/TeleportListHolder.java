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
 * @author NviX
 */
public class TeleportListHolder
{
	private final int _locId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _price;
	private final boolean _special;
	
	public TeleportListHolder(int locId, int x, int y, int z, int price, boolean special)
	{
		_locId = locId;
		_x = x;
		_y = y;
		_z = z;
		_price = price;
		_special = special;
	}
	
	public int getLocId()
	{
		return _locId;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public boolean isSpecial()
	{
		return _special;
	}
}