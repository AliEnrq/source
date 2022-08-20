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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author mrTJO
 */
public class Ex2ndPasswordAck implements IClientOutgoingPacket
{
	// TODO: Enum
	public static final int SUCCESS = 0;
	public static final int WRONG_PATTERN = 1;
	
	private final int _status;
	private final int _response;
	
	public Ex2ndPasswordAck(int status, int response)
	{
		_status = status;
		_response = response;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_2ND_PASSWORD_ACK.writeId(packet);
		packet.writeC(_status);
		packet.writeD(_response == WRONG_PATTERN ? 1 : 0);
		packet.writeD(0);
		return true;
	}
}
