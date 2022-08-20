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
package org.l2jmobius.gameserver.network.serverpackets.subjugation;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.holders.PurgePlayerHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 13.04.2021 01 00 00 00 19 01 00 00 0000 0000
 */
public class ExSubjugationSidebar implements IClientOutgoingPacket
{
	private final PurgePlayerHolder _purgeData;
	private final int _category;
	
	public ExSubjugationSidebar(int category, PurgePlayerHolder purgeData)
	{
		_category = category;
		_purgeData = purgeData;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SUBJUGATION_SIDEBAR.writeId(packet);
		packet.writeD(_category); // key size
		packet.writeD(_purgeData != null ? _purgeData.getPoints() : 0); // 1000000 = 100 percent
		packet.writeD(_purgeData != null ? _purgeData.getKeys() : 0);
		packet.writeD(0);
		return true;
	}
}
