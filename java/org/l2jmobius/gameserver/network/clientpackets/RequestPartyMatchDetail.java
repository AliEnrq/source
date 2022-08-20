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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.MatchingRoomManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.matching.MatchingRoom;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author Gnacik
 */
public class RequestPartyMatchDetail implements IClientIncomingPacket
{
	private int _roomId;
	private int _location;
	private int _level;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_roomId = packet.readD();
		_location = packet.readD();
		_level = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isInMatchingRoom())
		{
			return;
		}
		
		final MatchingRoom room = _roomId > 0 ? MatchingRoomManager.getInstance().getPartyMathchingRoom(_roomId) : MatchingRoomManager.getInstance().getPartyMathchingRoom(_location, _level);
		if (room != null)
		{
			room.addMember(player);
		}
	}
}
