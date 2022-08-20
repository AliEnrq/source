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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.MatchingRoomManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Gnacik
 */
public class ExListPartyMatchingWaitingRoom implements IClientOutgoingPacket
{
	private static final int NUM_PER_PAGE = 64;
	
	private final int _size;
	private final List<Player> _players = new LinkedList<>();
	
	public ExListPartyMatchingWaitingRoom(int page, int minLevel, int maxLevel, List<ClassId> classIds, String query)
	{
		final List<Player> players = MatchingRoomManager.getInstance().getPlayerInWaitingList(minLevel, maxLevel, classIds, query);
		_size = players.size();
		final int startIndex = (page - 1) * NUM_PER_PAGE;
		int chunkSize = _size - startIndex;
		if (chunkSize > NUM_PER_PAGE)
		{
			chunkSize = NUM_PER_PAGE;
		}
		for (int i = startIndex; i < (startIndex + chunkSize); i++)
		{
			_players.add(players.get(i));
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_LIST_PARTY_MATCHING_WAITING_ROOM.writeId(packet);
		packet.writeD(_size);
		packet.writeD(_players.size());
		for (Player player : _players)
		{
			packet.writeS(player.getName());
			packet.writeD(player.getClassId().getId());
			packet.writeD(player.getLevel());
			final Instance instance = InstanceManager.getInstance().getPlayerInstance(player, false);
			packet.writeD((instance != null) && (instance.getTemplateId() >= 0) ? instance.getTemplateId() : -1);
			final Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(player);
			packet.writeD(instanceTimes.size());
			for (Entry<Integer, Long> entry : instanceTimes.entrySet())
			{
				final long instanceTime = TimeUnit.MILLISECONDS.toSeconds(entry.getValue() - System.currentTimeMillis());
				packet.writeD(entry.getKey());
				packet.writeD((int) instanceTime);
			}
		}
		return true;
	}
}
