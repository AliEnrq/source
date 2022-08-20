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
package org.l2jmobius.gameserver.network.serverpackets.pledgebonus;

import java.util.logging.Logger;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.ClanRewardData;
import org.l2jmobius.gameserver.enums.ClanRewardType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanRewardBonus;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author UnAfraid
 */
public class ExPledgeBonusOpen implements IClientOutgoingPacket
{
	private static final Logger LOGGER = Logger.getLogger(ExPledgeBonusOpen.class.getName());
	
	private final Player _player;
	
	public ExPledgeBonusOpen(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		final Clan clan = _player.getClan();
		if (clan == null)
		{
			LOGGER.warning("Player: " + _player + " attempting to write to a null clan!");
			return false;
		}
		final ClanRewardBonus highestMembersOnlineBonus = ClanRewardData.getInstance().getHighestReward(ClanRewardType.MEMBERS_ONLINE);
		final ClanRewardBonus highestHuntingBonus = ClanRewardData.getInstance().getHighestReward(ClanRewardType.HUNTING_MONSTERS);
		final ClanRewardBonus membersOnlineBonus = ClanRewardType.MEMBERS_ONLINE.getAvailableBonus(clan);
		final ClanRewardBonus huntingBonus = ClanRewardType.HUNTING_MONSTERS.getAvailableBonus(clan);
		if (highestMembersOnlineBonus == null)
		{
			LOGGER.warning("Couldn't find highest available clan members online bonus!!");
			return false;
		}
		else if (highestHuntingBonus == null)
		{
			LOGGER.warning("Couldn't find highest available clan hunting bonus!!");
			return false;
		}
		else if (highestMembersOnlineBonus.getSkillReward() == null)
		{
			LOGGER.warning("Couldn't find skill reward for highest available members online bonus!!");
			return false;
		}
		else if (highestHuntingBonus.getSkillReward() == null)
		{
			LOGGER.warning("Couldn't find skill reward for highest available hunting bonus!!");
			return false;
		}
		// General OP Code
		OutgoingPackets.EX_PLEDGE_BONUS_OPEN.writeId(packet);
		// Members online bonus
		packet.writeD(highestMembersOnlineBonus.getRequiredAmount());
		packet.writeD(clan.getMaxOnlineMembers());
		packet.writeC(2); // 140
		packet.writeD(membersOnlineBonus != null ? highestMembersOnlineBonus.getSkillReward().getSkillId() : 0);
		packet.writeC(membersOnlineBonus != null ? membersOnlineBonus.getLevel() : 0);
		packet.writeC(clan.canClaimBonusReward(_player, ClanRewardType.MEMBERS_ONLINE) ? 1 : 0);
		// Hunting bonus
		packet.writeD(highestHuntingBonus.getRequiredAmount());
		packet.writeD(clan.getHuntingPoints());
		packet.writeC(2); // 140
		packet.writeD(huntingBonus != null ? highestHuntingBonus.getSkillReward().getSkillId() : 0);
		packet.writeC(huntingBonus != null ? huntingBonus.getLevel() : 0);
		packet.writeC(clan.canClaimBonusReward(_player, ClanRewardType.HUNTING_MONSTERS) ? 1 : 0);
		return true;
	}
}
