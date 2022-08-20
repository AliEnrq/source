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
 * @author NasSeKa
 */
public class SharedTeleportHolder
{
	private final int _tpId;
	private final String _charName;
	private int _timesUsed;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public SharedTeleportHolder(int tpId, String charName, int timesUsed, int x, int y, int z)
	{
		_tpId = tpId;
		_charName = charName;
		_timesUsed = timesUsed;
		_x = x;
		_y = y;
		_z = z;
	}
	
	public int getTpId()
	{
		return _tpId;
	}
	
	public String getCharName()
	{
		return _charName;
	}
	
	public int getTimesUsed()
	{
		return _timesUsed;
	}
	
	public void useTeleport()
	{
		_timesUsed -= 1;
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
}