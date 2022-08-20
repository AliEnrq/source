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

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PvpBookList implements IClientOutgoingPacket
{
	public PvpBookList()
	{
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PVPBOOK_LIST.writeId(packet);
		final int size = 1;
		packet.writeD(4); // show killer's location count
		packet.writeD(5); // teleport count
		packet.writeD(size); // killer count
		for (int i = 0; i < size; i++)
		{
			packet.writeString("killer" + i); // killer name
			packet.writeString("clanKiller" + i); // killer clan name
			packet.writeD(15); // killer level
			packet.writeD(2); // killer race
			packet.writeD(10); // killer class
			packet.writeD((int) LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()); // kill time
			packet.writeC(1); // is online
		}
		return true;
	}
}
