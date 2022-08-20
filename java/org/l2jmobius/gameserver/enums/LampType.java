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
package org.l2jmobius.gameserver.enums;

/**
 * @author L2CCCP
 */
public enum LampType
{
	RED(1),
	PURPLE(2),
	BLUE(3),
	GREEN(4);
	
	private int _grade;
	
	LampType(int grade)
	{
		_grade = grade;
	}
	
	public int getGrade()
	{
		return _grade;
	}
}