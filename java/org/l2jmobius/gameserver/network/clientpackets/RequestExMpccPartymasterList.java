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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.MatchingRoomType;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.matching.MatchingRoom;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExMPCCPartymasterList;

/**
 * @author Sdw
 */
public class RequestExMpccPartymasterList implements IClientIncomingPacket
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
		if ((room != null) && (room.getRoomType() == MatchingRoomType.COMMAND_CHANNEL))
		{
			final Set<String> leadersName = room.getMembers().stream().map(Player::getParty).filter(Objects::nonNull).map(Party::getLeader).map(Player::getName).collect(Collectors.toSet());
			player.sendPacket(new ExMPCCPartymasterList(leadersName));
		}
	}
}
