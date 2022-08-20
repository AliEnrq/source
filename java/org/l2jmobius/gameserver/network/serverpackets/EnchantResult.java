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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class EnchantResult implements IClientOutgoingPacket
{
	public static final int SUCCESS = 0;
	public static final int FAIL = 1;
	public static final int ERROR = 2;
	public static final int BLESSED_FAIL = 3;
	public static final int NO_CRYSTAL = 4;
	public static final int SAFE_FAIL = 5;
	
	private final int _result;
	private final int _crystal;
	private final int _count;
	private final int _enchantLevel;
	@SuppressWarnings("unused")
	private final int[] _enchantOptions;
	
	public EnchantResult(int result, int crystal, int count, int enchantLevel, int[] options)
	{
		_result = result;
		_crystal = crystal;
		_count = count;
		_enchantLevel = enchantLevel;
		_enchantOptions = options;
	}
	
	public EnchantResult(int result, int crystal, int count)
	{
		this(result, crystal, count, 0, Item.DEFAULT_ENCHANT_OPTIONS);
	}
	
	public EnchantResult(int result, Item item)
	{
		this(result, 0, 0, item.getEnchantLevel(), item.getEnchantOptions());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ENCHANT_RESULT.writeId(packet);
		packet.writeD(_result);
		packet.writeD(_crystal);
		packet.writeQ(_count);
		// Guessing.
		// With 166 options became 3x write integers instead of shorts and enchant level moved bellow.
		// Commenting until actually knowing.
		// for (int option : _enchantOptions)
		// {
		// packet.writeD(option);
		// }
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(_enchantLevel); // Confirmed.
		return true;
	}
}
