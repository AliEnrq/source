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
package org.l2jmobius.gameserver.network.serverpackets.pledgedonation;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 08.05.2021 00 01 00 00 00 00 00 03 00 00 00 0E 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00
 */
public class ExPledgeDonationInfo implements IClientOutgoingPacket
{
	private final int _curPoints;
	private final boolean _accepted;
	
	public ExPledgeDonationInfo(int curPoints, boolean accepted)
	{
		_curPoints = curPoints;
		_accepted = accepted;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_DONATION_INFO.writeId(packet);
		packet.writeD(_curPoints);
		packet.writeC(_accepted ? 0 : 1);
		return true;
	}
}
