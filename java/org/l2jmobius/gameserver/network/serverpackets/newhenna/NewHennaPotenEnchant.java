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
package org.l2jmobius.gameserver.network.serverpackets.newhenna;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Index, Serenitty
 */
public class NewHennaPotenEnchant implements IClientOutgoingPacket
{
	private final int _slotId;
	private final int _enchantStep;
	private final int _enchantExp;
	private final int _dailyStep;
	private final int _dailyCount;
	private final int _activeStep;
	private final boolean _success;
	
	public NewHennaPotenEnchant(int slotId, int enchantStep, int enchantExp, int dailyStep, int dailyCount, int activeStep, boolean success)
	{
		_slotId = slotId;
		_enchantStep = enchantStep;
		_enchantExp = enchantExp;
		_dailyStep = dailyStep;
		_dailyCount = dailyCount;
		_activeStep = activeStep;
		_success = success;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_NEW_HENNA_POTEN_ENCHANT.writeId(packet);
		packet.writeC(_slotId);
		packet.writeH(_enchantStep);
		packet.writeD(_enchantExp);
		packet.writeH(_dailyStep);
		packet.writeH(_dailyCount);
		packet.writeH(_activeStep);
		packet.writeC(_success ? 1 : 0);
		return true;
	}
}
