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
package org.l2jmobius.gameserver.network.serverpackets.ranking;

import java.util.Map;
import java.util.Optional;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 10.05.2021
 */
public class ExPvpRankingMyInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;
	
	public ExPvpRankingMyInfo(Player player)
	{
		_player = player;
		_playerList = RankManager.getInstance().getPvpRankList();
		_snapshotList = RankManager.getInstance().getSnapshotPvpRankList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PVP_RANKING_MY_INFO.writeId(packet);
		if (!_playerList.isEmpty())
		{
			boolean found = false;
			for (Integer id : _playerList.keySet())
			{
				final StatSet ss = _playerList.get(id);
				if (ss.getInt("charId") == _player.getObjectId())
				{
					final Optional<Map.Entry<Integer, StatSet>> snapshotValue = _snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("charId") == _player.getObjectId()).findFirst();
					found = true;
					packet.writeQ(ss.getInt("points")); // pvp points
					packet.writeD(id); // current rank
					packet.writeD(snapshotValue.isPresent() ? snapshotValue.get().getKey() : id); // ingame shown change in rank as this value - current rank value.
					packet.writeD(ss.getInt("kills")); // kills
					packet.writeD(ss.getInt("deaths")); // deaths
				}
			}
			if (!found)
			{
				packet.writeQ(0); // pvp points
				packet.writeD(0); // current rank
				packet.writeD(0); // ingame shown change in rank as this value - current rank value.
				packet.writeD(0); // kills
				packet.writeD(0); // deaths
			}
		}
		else
		{
			packet.writeQ(0); // pvp points
			packet.writeD(0); // current rank
			packet.writeD(0); // ingame shown change in rank as this value - current rank value.
			packet.writeD(0); // kills
			packet.writeD(0); // deaths
		}
		return true;
	}
}
