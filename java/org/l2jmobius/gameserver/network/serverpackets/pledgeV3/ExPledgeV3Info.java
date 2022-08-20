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
package org.l2jmobius.gameserver.network.serverpackets.pledgeV3;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 04.05.2021 22 C0 00 00 00 00 00 00 00 00 00
 */
public class ExPledgeV3Info implements IClientOutgoingPacket
{
	private final int _points;
	private final int _rank;
	private final String _announce;
	private final boolean _isShowOnEnter;
	
	public ExPledgeV3Info(int points, int rank, String announce, boolean isShowOnEnter)
	{
		_points = points;
		_rank = rank;
		_announce = announce;
		_isShowOnEnter = isShowOnEnter;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_V3_INFO.writeId(packet);
		packet.writeD(_points);
		packet.writeD(_rank);
		packet.writeString(_announce);
		packet.writeC(_isShowOnEnter ? 1 : 0);
		return true;
	}
}
