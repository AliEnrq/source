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
import org.l2jmobius.gameserver.enums.ElementalType;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author JoeAlisson
 */
public abstract class UpdateElementalSpiritPacket extends AbstractElementalSpiritPacket
{
	private final Player _player;
	private final byte _type;
	private final boolean _update;
	
	UpdateElementalSpiritPacket(Player player, byte type, boolean update)
	{
		_player = player;
		_type = type;
		_update = update;
	}
	
	protected void writeUpdate(PacketWriter packet)
	{
		packet.writeC(_update ? 1 : 0);
		packet.writeC(_type);
		if (_update)
		{
			final ElementalSpirit spirit = _player.getElementalSpirit(ElementalType.of(_type));
			if (spirit == null)
			{
				return;
			}
			packet.writeC(_type);
			writeSpiritInfo(packet, spirit);
		}
	}
}
