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
import org.l2jmobius.gameserver.enums.MatchingRoomType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.matching.MatchingRoom;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author Sdw
 */
public class RequestExDismissMpccRoom implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
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
		
		final MatchingRoom room = player.getMatchingRoom();
		if ((room != null) && (room.getLeader() == player) && (room.getRoomType() == MatchingRoomType.COMMAND_CHANNEL))
		{
			room.disbandRoom();
		}
	}
}
