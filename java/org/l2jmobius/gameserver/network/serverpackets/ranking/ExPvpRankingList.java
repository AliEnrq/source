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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.enums.RankingCategory;
import org.l2jmobius.gameserver.enums.RankingScope;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 10.05.2021
 */
public class ExPvpRankingList implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _season;
	private final int _tabId;
	private final int _type;
	private final int _race;
	private final int _class;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;
	
	public ExPvpRankingList(Player player, int season, int tabId, int type, int race, int baseclass)
	{
		_player = player;
		_season = season;
		_tabId = tabId;
		_type = type;
		_race = race;
		_class = baseclass;
		_playerList = RankManager.getInstance().getPvpRankList();
		_snapshotList = RankManager.getInstance().getSnapshotPvpRankList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PVP_RANKING_LIST.writeId(packet);
		packet.writeC(_season);
		packet.writeC(_tabId);
		packet.writeC(_type);
		packet.writeD(_race);
		if (!_playerList.isEmpty() && (_type != 255) && (_race != 255))
		{
			final RankingCategory category = RankingCategory.values()[_tabId];
			writeFilteredRankingData(packet, category, category.getScopeByGroup(_type), Race.values()[_race], ClassId.values()[_class]);
		}
		else
		{
			packet.writeD(0);
		}
		return true;
	}
	
	private void writeFilteredRankingData(PacketWriter packet, RankingCategory category, RankingScope scope, Race race, ClassId baseclass)
	{
		switch (category)
		{
			case SERVER:
			{
				writeScopeData(packet, scope, new ArrayList<>(_playerList.entrySet()), new ArrayList<>(_snapshotList.entrySet()));
				break;
			}
			case RACE:
			{
				writeScopeData(packet, scope, _playerList.entrySet().stream().filter(it -> it.getValue().getInt("race") == race.ordinal()).collect(Collectors.toList()), _snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("race") == race.ordinal()).collect(Collectors.toList()));
				break;
			}
			case CLAN:
			{
				writeScopeData(packet, scope, _player.getClan() == null ? Collections.emptyList() : _playerList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(_player.getClan().getName())).collect(Collectors.toList()), _player.getClan() == null ? Collections.emptyList() : _snapshotList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(_player.getClan().getName())).collect(Collectors.toList()));
				break;
			}
			case FRIEND:
			{
				writeScopeData(packet, scope, _playerList.entrySet().stream().filter(it -> _player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()), _snapshotList.entrySet().stream().filter(it -> _player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()));
				break;
			}
			case CLASS:
			{
				writeScopeData(packet, scope, _playerList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == baseclass.ordinal()).collect(Collectors.toList()), _snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == baseclass.ordinal()).collect(Collectors.toList()));
				break;
			}
		}
	}
	
	private void writeScopeData(PacketWriter packet, RankingScope scope, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot)
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
			case TOP_150:
			{
				limited = list.stream().limit(150).collect(Collectors.toList());
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
			packet.writeString(player.getString("name"));
			packet.writeString(player.getString("clanName"));
			packet.writeD(player.getInt("level"));
			packet.writeD(player.getInt("race"));
			packet.writeD(player.getInt("classId"));
			packet.writeQ(player.getInt("points")); // server rank
			if (!snapshot.isEmpty())
			{
				for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
				{
					final StatSet snapshotData = ssData.getValue();
					if (player.getInt("charId") == snapshotData.getInt("charId"))
					{
						packet.writeD(scope == RankingScope.SELF ? ssData.getKey() : curRank); // server rank snapshot
						packet.writeD(snapshotData.getInt("raceRank", 0)); // race rank snapshot
						packet.writeD(player.getInt("kills"));
						packet.writeD(player.getInt("deaths"));
					}
				}
			}
			else
			{
				packet.writeD(scope == RankingScope.SELF ? data.getKey() : curRank);
				packet.writeD(0);
				packet.writeD(0);
				packet.writeD(0);
			}
		}
	}
}
