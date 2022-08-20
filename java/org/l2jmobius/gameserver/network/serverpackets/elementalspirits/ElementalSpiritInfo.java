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
package org.l2jmobius.gameserver.network.serverpackets.elementalspirits;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author JoeAlisson
 */
public class ElementalSpiritInfo extends AbstractElementalSpiritPacket
{
	private final Player _player;
	private final byte _spiritType;
	private final byte _type;
	
	public ElementalSpiritInfo(Player player, byte spiritType, byte packetType)
	{
		_player = player;
		_spiritType = spiritType;
		_type = packetType;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ELEMENTAL_SPIRIT_INFO.writeId(packet);
		final ElementalSpirit[] spirits = _player.getSpirits();
		if (spirits == null)
		{
			packet.writeC(0);
			packet.writeC(0);
			packet.writeC(0);
			return true;
		}
		packet.writeC(_type); // show spirit info window 1; Change type 2; Only update 0
		packet.writeC(_spiritType);
		packet.writeC(spirits.length); // spirit count
		for (ElementalSpirit spirit : spirits)
		{
			packet.writeC(spirit.getType());
			packet.writeC(1); // spirit active ?
			// if active
			writeSpiritInfo(packet, spirit);
		}
		packet.writeD(1); // Reset talent items count
		for (int j = 0; j < 1; j++)
		{
			packet.writeD(57);
			packet.writeQ(50000);
		}
		return true;
	}
}