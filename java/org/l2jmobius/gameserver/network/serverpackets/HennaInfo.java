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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.henna.Henna;
import org.l2jmobius.gameserver.model.item.henna.HennaPoten;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * This server packet sends the player's henna information.
 * @author Zoey76
 */
public class HennaInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final List<Henna> _hennas = new ArrayList<>();
	
	public HennaInfo(Player player)
	{
		_player = player;
		for (HennaPoten hennaPoten : _player.getHennaPotenList())
		{
			final Henna henna = hennaPoten.getHenna();
			if (henna != null)
			{
				_hennas.add(henna);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.HENNA_INFO.writeId(packet);
		packet.writeH(_player.getHennaValue(BaseStat.INT)); // equip INT
		packet.writeH(_player.getHennaValue(BaseStat.STR)); // equip STR
		packet.writeH(_player.getHennaValue(BaseStat.CON)); // equip CON
		packet.writeH(_player.getHennaValue(BaseStat.MEN)); // equip MEN
		packet.writeH(_player.getHennaValue(BaseStat.DEX)); // equip DEX
		packet.writeH(_player.getHennaValue(BaseStat.WIT)); // equip WIT
		packet.writeH(0); // equip LUC
		packet.writeH(0); // equip CHA
		packet.writeD(3 - _player.getHennaEmptySlots()); // Slots
		packet.writeD(_hennas.size()); // Size
		for (Henna henna : _hennas)
		{
			packet.writeD(henna.getDyeId());
			packet.writeD(henna.isAllowedClass(_player) ? 1 : 0);
		}
		packet.writeD(0); // Premium Slot Dye ID
		packet.writeD(0); // Premium Slot Dye Time Left
		packet.writeD(0); // Premium Slot Dye ID isValid
		return true;
	}
}
