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
package org.l2jmobius.gameserver.network.serverpackets.pledgeV3;

import java.util.List;
import java.util.stream.Collectors;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ClanWarState;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanWar;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 04.05.2021
 */
public class ExPledgeEnemyInfoList implements IClientOutgoingPacket
{
	private final Clan _playerClan;
	private final List<ClanWar> _warList;
	
	public ExPledgeEnemyInfoList(Clan playerClan)
	{
		_playerClan = playerClan;
		_warList = playerClan.getWarList().values().stream().filter(it -> (it.getClanWarState(playerClan) == ClanWarState.MUTUAL) || (it.getAttackerClanId() == playerClan.getId())).collect(Collectors.toList());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_ENEMY_INFO_LIST.writeId(packet);
		packet.writeD(_warList.size());
		for (ClanWar war : _warList)
		{
			final Clan clan = war.getOpposingClan(_playerClan);
			packet.writeD(clan.getRank());
			packet.writeD(clan.getId());
			packet.writeString(clan.getName());
			packet.writeString(clan.getLeaderName());
		}
		return true;
	}
}
