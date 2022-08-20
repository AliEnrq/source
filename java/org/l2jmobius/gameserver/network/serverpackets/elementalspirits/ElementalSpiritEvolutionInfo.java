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

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ElementalType;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author JoeAlisson
 */
public class ElementalSpiritEvolutionInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final byte _type;
	
	public ElementalSpiritEvolutionInfo(Player player, byte type)
	{
		_player = player;
		_type = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ELEMENTAL_SPIRIT_EVOLUTION_INFO.writeId(packet);
		final ElementalSpirit spirit = _player.getElementalSpirit(ElementalType.of(_type));
		if (spirit == null)
		{
			packet.writeC(0);
			packet.writeD(0);
			return true;
		}
		packet.writeC(_type);
		packet.writeD(spirit.getNpcId());
		packet.writeD(1); // unk
		packet.writeD(spirit.getStage());
		packet.writeF(100); // chance ??
		final List<ItemHolder> items = spirit.getItemsToEvolve();
		packet.writeD(items.size());
		for (ItemHolder item : items)
		{
			packet.writeD(item.getId());
			packet.writeQ(item.getCount());
		}
		return true;
	}
}
