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

import java.util.Collection;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.CharNameTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.Clan.SubPledge;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PledgeShowMemberListAll implements IClientOutgoingPacket
{
	private final Clan _clan;
	private final SubPledge _pledge;
	private final String _name;
	private final String _leaderName;
	private final Collection<ClanMember> _members;
	private final int _pledgeId;
	private final boolean _isSubPledge;
	
	private PledgeShowMemberListAll(Clan clan, SubPledge pledge, boolean isSubPledge)
	{
		_clan = clan;
		_pledge = pledge;
		_pledgeId = _pledge == null ? 0 : _pledge.getId();
		_leaderName = pledge == null ? clan.getLeaderName() : CharNameTable.getInstance().getNameById(pledge.getLeaderId());
		_name = pledge == null ? clan.getName() : pledge.getName();
		_members = _clan.getMembers();
		_isSubPledge = isSubPledge;
	}
	
	public static void sendAllTo(Player player)
	{
		final Clan clan = player.getClan();
		if (clan != null)
		{
			for (SubPledge subPledge : clan.getAllSubPledges())
			{
				player.sendPacket(new PledgeShowMemberListAll(clan, subPledge, false));
			}
			player.sendPacket(new PledgeShowMemberListAll(clan, null, true));
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(packet);
		packet.writeD(_isSubPledge ? 0 : 1);
		packet.writeD(_clan.getId());
		packet.writeD(Config.SERVER_ID);
		packet.writeD(_pledgeId);
		packet.writeS(_name);
		packet.writeS(_leaderName);
		packet.writeD(_clan.getCrestId()); // crest id .. is used again
		packet.writeD(_clan.getLevel());
		packet.writeD(_clan.getCastleId());
		packet.writeD(0);
		packet.writeD(_clan.getHideoutId());
		packet.writeD(_clan.getFortId());
		packet.writeD(_clan.getRank());
		packet.writeD(_clan.getReputationScore());
		packet.writeD(0); // 0
		packet.writeD(0); // 0
		packet.writeD(_clan.getAllyId());
		packet.writeS(_clan.getAllyName());
		packet.writeD(_clan.getAllyCrestId());
		packet.writeD(_clan.isAtWar() ? 1 : 0); // new c3
		packet.writeD(0); // Territory castle ID
		packet.writeD(_clan.getSubPledgeMembersCount(_pledgeId));
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeId)
			{
				continue;
			}
			packet.writeS(m.getName());
			packet.writeD(m.getLevel());
			packet.writeD(m.getClassId());
			final Player player = m.getPlayer();
			if (player != null)
			{
				packet.writeD(player.getAppearance().isFemale() ? 1 : 0); // no visible effect
				packet.writeD(player.getRace().ordinal()); // packet.writeD(1);
			}
			else
			{
				packet.writeD(1); // no visible effect
				packet.writeD(1); // packet.writeD(1);
			}
			packet.writeD(m.isOnline() ? m.getObjectId() : 0); // objectId = online 0 = offline
			packet.writeD(m.getSponsor() != 0 ? 1 : 0);
			packet.writeC(m.getOnlineStatus());
		}
		return true;
	}
}
