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
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 16.05.2021
 */
public class ExPledgeRankingList implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _category;
	private final Map<Integer, StatSet> _rankingClanList;
	private final Map<Integer, StatSet> _snapshotClanList;
	
	public ExPledgeRankingList(Player player, int category)
	{
		_player = player;
		_category = category;
		_rankingClanList = RankManager.getInstance().getClanRankList();
		_snapshotClanList = RankManager.getInstance().getSnapshotClanRankList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_RANKING_LIST.writeId(packet);
		packet.writeC(_category);
		if (!_rankingClanList.isEmpty())
		{
			writeScopeData(packet, _category == 0, new ArrayList<>(_rankingClanList.entrySet()), new ArrayList<>(_snapshotClanList.entrySet()));
		}
		else
		{
			packet.writeD(0);
		}
		return true;
	}
	
	private void writeScopeData(PacketWriter packet, boolean isTop150, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot)
	{
		Entry<Integer, StatSet> playerData = list.stream().filter(it -> it.getValue().getInt("clan_id", 0) == _player.getClanId()).findFirst().orElse(null);
		final int indexOf = list.indexOf(playerData);
		final List<Entry<Integer, StatSet>> limited = isTop150 ? list.stream().limit(150).collect(Collectors.toList()) : playerData == null ? Collections.emptyList() : list.subList(Math.max(0, indexOf - 10), Math.min(list.size(), indexOf + 10));
		packet.writeD(limited.size());
		int rank = 1;
		for (Entry<Integer, StatSet> data : limited.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
		{
			int curRank = rank++;
			final StatSet player = data.getValue();
			packet.writeD(!isTop150 ? data.getKey() : curRank);
			for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
			{
				final StatSet snapshotData = ssData.getValue();
				if (player.getInt("clan_id") == snapshotData.getInt("clan_id"))
				{
					packet.writeD(!isTop150 ? ssData.getKey() : curRank); // server rank snapshot
				}
			}
			packet.writeString(player.getString("clan_name"));
			packet.writeD(player.getInt("clan_level"));
			packet.writeString(player.getString("char_name"));
			packet.writeD(player.getInt("level"));
			packet.writeD(ClanTable.getInstance().getClan(player.getInt("clan_id")) != null ? ClanTable.getInstance().getClan(player.getInt("clan_id")).getMembersCount() : 0);
			packet.writeD((int) Math.min(Integer.MAX_VALUE, player.getLong("exp")));
		}
	}
}
