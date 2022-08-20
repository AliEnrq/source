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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author NviX
 */
public class ExRankingCharInfo implements IClientOutgoingPacket
{
	@SuppressWarnings("unused")
	private final short _unk;
	private final Player _player;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;
	
	public ExRankingCharInfo(Player player, short unk)
	{
		_unk = unk;
		_player = player;
		_playerList = RankManager.getInstance().getRankList();
		_snapshotList = RankManager.getInstance().getSnapshotList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RANKING_CHAR_INFO.writeId(packet);
		if (!_playerList.isEmpty())
		{
			for (Integer id : _playerList.keySet())
			{
				final StatSet player = _playerList.get(id);
				if (player.getInt("charId") == _player.getObjectId())
				{
					packet.writeD(id); // server rank
					packet.writeD(player.getInt("raceRank")); // race rank
					packet.writeD(player.getInt("classRank")); // class rank
					for (Integer id2 : _snapshotList.keySet())
					{
						final StatSet snapshot = _snapshotList.get(id2);
						if (player.getInt("charId") == snapshot.getInt("charId"))
						{
							packet.writeD(id2); // server rank snapshot
							packet.writeD(snapshot.getInt("classRank")); // class rank snapshot
							packet.writeD(player.getInt("classRank")); // class rank snapshot
							packet.writeD(0);
							packet.writeD(0);
							packet.writeD(0);
							return true;
						}
					}
				}
			}
			packet.writeD(0); // server rank
			packet.writeD(0); // race rank
			packet.writeD(0); // server rank snapshot
			packet.writeD(0); // race rank snapshot
			packet.writeD(0); // nClassRank
			packet.writeD(0); // nClassRank_Snapshot snapshot
		}
		else
		{
			packet.writeD(0); // server rank
			packet.writeD(0); // race rank
			packet.writeD(0); // server rank snapshot
			packet.writeD(0); // race rank snapshot
			packet.writeD(0); // nClassRank
			packet.writeD(0); // nClassRank_Snapshot snapshot
		}
		return true;
	}
}
