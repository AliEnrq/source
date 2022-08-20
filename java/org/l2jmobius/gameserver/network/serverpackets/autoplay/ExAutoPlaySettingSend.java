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
package org.l2jmobius.gameserver.network.serverpackets.autoplay;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author JoeAlisson
 */
public class ExAutoPlaySettingSend implements IClientOutgoingPacket
{
	private final int _options;
	private final boolean _active;
	private final boolean _pickUp;
	private final int _nextTargetMode;
	private final boolean _shortRange;
	private final int _potionPercent;
	private final boolean _respectfulHunting;
	
	public ExAutoPlaySettingSend(int options, boolean active, boolean pickUp, int nextTargetMode, boolean shortRange, int potionPercent, boolean respectfulHunting)
	{
		_options = options;
		_active = active;
		_pickUp = pickUp;
		_nextTargetMode = nextTargetMode;
		_shortRange = shortRange;
		_potionPercent = potionPercent;
		_respectfulHunting = respectfulHunting;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_AUTOPLAY_SETTING.writeId(packet);
		packet.writeH(_options);
		packet.writeC(_active ? 1 : 0);
		packet.writeC(_pickUp ? 1 : 0);
		packet.writeH(_nextTargetMode);
		packet.writeC(_shortRange ? 1 : 0);
		packet.writeD(_potionPercent);
		packet.writeD(0); // 272
		packet.writeC(_respectfulHunting ? 1 : 0);
		return true;
	}
}
