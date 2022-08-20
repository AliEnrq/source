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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.RankingOlympiadCategory;
import org.l2jmobius.gameserver.enums.RankingOlympiadScope;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Berezkin Nikolay
 */
public class ExOlympiadRankingInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _tabId;
	private final int _rankingType;
	private final int _unk;
	private final int _classId;
	private final int _serverId;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;
	
	public ExOlympiadRankingInfo(Player player, int tabId, int rankingType, int unk, int classId, int serverId)
	{
		_player = player;
		_tabId = tabId;
		_rankingType = rankingType;
		_unk = unk;
		_classId = classId;
		_serverId = serverId;
		_playerList = RankManager.getInstance().getOlyRankList();
		_snapshotList = RankManager.getInstance().getSnapshotOlyList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_OLYMPIAD_RANKING_INFO.writeId(packet);
		packet.writeC(_tabId); // Tab id
		packet.writeC(_rankingType); // ranking type
		packet.writeC(_unk); // unk, shows 1 all time
		packet.writeD(_classId); // class id (default 148) or caller class id for personal rank
		packet.writeD(_serverId); // 0 - all servers, server id - for caller server
		packet.writeD(933); // unk, 933 all time
		if (!_playerList.isEmpty())
		{
			final RankingOlympiadCategory category = RankingOlympiadCategory.values()[_tabId];
			writeFilteredRankingData(packet, category, category.getScopeByGroup(_rankingType), ClassId.getClassId(_classId));
		}
		return true;
	}
	
	private void writeFilteredRankingData(PacketWriter packet, RankingOlympiadCategory category, RankingOlympiadScope scope, ClassId classId)
	{
		switch (category)
		{
			case SERVER:
			{
				writeScopeData(packet, scope, new ArrayList<>(_playerList.entrySet()), new ArrayList<>(_snapshotList.entrySet()));
				break;
			}
			case CLASS:
			{
				writeScopeData(packet, scope, _playerList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == classId.getId()).collect(Collectors.toList()), _snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == classId.getId()).collect(Collectors.toList()));
				break;
			}
		}
	}
	
	private void writeScopeData(PacketWriter packet, RankingOlympiadScope scope, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot)
	{
		Entry<Integer, StatSet> playerData = list.stream().filter(it -> it.getValue().getInt("charId", 0) == _player.getObjectId()).findFirst().orElse(null);
		final int indexOf = list.indexOf(playerData);
		final List<Entry<Integer, StatSet>> limited;
		switch (scope)
		{
			case TOP_100:
			{
				limited = list.stream().limit(100).collect(Collectors.toList());
				break;
			}
			case ALL:
			{
				limited = list;
				break;
			}
			case TOP_50:
			{
				limited = list.stream().limit(50).collect(Collectors.toList());
				break;
			}
			case SELF:
			{
				limited = playerData == null ? Collections.emptyList() : list.subList(Math.max(0, indexOf - 10), Math.min(list.size(), indexOf + 10));
				break;
			}
			default:
			{
				limited = Collections.emptyList();
			}
		}
		packet.writeD(limited.size());
		int rank = 1;
		for (Entry<Integer, StatSet> data : limited.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
		{
			int curRank = rank++;
			final StatSet player = data.getValue();
			packet.writeString(player.getString("name")); // name
			packet.writeString(player.getString("clanName")); // clan name
			packet.writeD(scope == RankingOlympiadScope.SELF ? data.getKey() : curRank); // rank
			if (!snapshot.isEmpty())
			{
				int snapshotRank = 1;
				for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
				{
					final StatSet snapshotData = ssData.getValue();
					if (player.getInt("charId") == snapshotData.getInt("charId"))
					{
						packet.writeD(scope == RankingOlympiadScope.SELF ? ssData.getKey() : snapshotRank++); // previous rank
					}
				}
			}
			else
			{
				packet.writeD(scope == RankingOlympiadScope.SELF ? data.getKey() : curRank);
			}
			packet.writeD(Config.SERVER_ID); // server id
			packet.writeD(player.getInt("level")); // level
			packet.writeD(player.getInt("classId")); // class id
			packet.writeD(player.getInt("clanLevel")); // clan level
			packet.writeD(player.getInt("competitions_won")); // win count
			packet.writeD(player.getInt("competitions_lost")); // lose count
			packet.writeD(player.getInt("olympiad_points")); // points
			packet.writeD(player.getInt("count")); // hero counts
			packet.writeD(player.getInt("legend_count")); // legend counts
		}
	}
}
