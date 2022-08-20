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
package org.l2jmobius.gameserver.model.vip;

/**
 * @author Gabriel Costa Souza
 */
public class VipInfo
{
	private final byte _tier;
	private final long _pointsRequired;
	private final long _pointsDepreciated;
	private int _skill;
	
	public VipInfo(byte tier, long pointsRequired, long pointsDepreciated)
	{
		_tier = tier;
		_pointsRequired = pointsRequired;
		_pointsDepreciated = pointsDepreciated;
	}
	
	public byte getTier()
	{
		return _tier;
	}
	
	public long getPointsRequired()
	{
		return _pointsRequired;
	}
	
	public long getPointsDepreciated()
	{
		return _pointsDepreciated;
	}
	
	public int getSkill()
	{
		return _skill;
	}
	
	public void setSkill(int skill)
	{
		_skill = skill;
	}
}
