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

import org.l2jmobius.gameserver.model.interfaces.IUpdateTypeComponent;

/**
 * @author Sdw
 */
public enum UserInfoType implements IUpdateTypeComponent
{
	RELATION(0x00, 4),
	BASIC_INFO(0x01, 23),
	BASE_STATS(0x02, 18),
	MAX_HPCPMP(0x03, 14),
	CURRENT_HPMPCP_EXP_SP(0x04, 38),
	ENCHANTLEVEL(0x05, 5),
	APPAREANCE(0x06, 19),
	STATUS(0x07, 6),
	
	STATS(0x08, 64),
	ELEMENTALS(0x09, 14),
	POSITION(0x0A, 18),
	SPEED(0x0B, 18),
	MULTIPLIER(0x0C, 18),
	COL_RADIUS_HEIGHT(0x0D, 18),
	ATK_ELEMENTAL(0x0E, 5),
	CLAN(0x0F, 32),
	
	SOCIAL(0x10, 30),
	VITA_FAME(0x11, 19),
	SLOTS(0x12, 12),
	MOVEMENTS(0x13, 4),
	COLOR(0x14, 10),
	INVENTORY_LIMIT(0x15, 13),
	TRUE_HERO(0x16, 9),
	
	ATT_SPIRITS(0x17, 26),
	
	RANKING(0x18, 6),
	
	STAT_POINTS(0x19, 16),
	STAT_ABILITIES(0x1A, 18),
	
	ELIXIR_USED(0x1B, 1);
	
	/** Int mask. */
	private final int _mask;
	private final int _blockLength;
	
	private UserInfoType(int mask, int blockLength)
	{
		_mask = mask;
		_blockLength = blockLength;
	}
	
	/**
	 * Gets the int mask.
	 * @return the int mask
	 */
	@Override
	public int getMask()
	{
		return _mask;
	}
	
	public int getBlockLength()
	{
		return _blockLength;
	}
}