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
package org.l2jmobius.gameserver.network.clientpackets.pledgebonus;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.ClanRewardType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.clan.ClanRewardBonus;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;

/**
 * @author UnAfraid
 */
public class RequestPledgeBonusReward implements IClientIncomingPacket
{
	private int _type;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_type = packet.readC();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if ((player == null) || (player.getClan() == null))
		{
			return;
		}
		
		if ((_type < 0) || (_type > ClanRewardType.values().length))
		{
			return;
		}
		
		final Clan clan = player.getClan();
		final ClanRewardType type = ClanRewardType.values()[_type];
		final ClanMember member = clan.getClanMember(player.getObjectId());
		if (clan.canClaimBonusReward(player, type))
		{
			final ClanRewardBonus bonus = type.getAvailableBonus(player.getClan());
			if (bonus != null)
			{
				final SkillHolder skillReward = bonus.getSkillReward();
				if (skillReward != null)
				{
					skillReward.getSkill().activateSkill(player, player);
				}
				member.setRewardClaimed(type);
			}
			else
			{
				PacketLogger.warning(player + " Attempting to claim reward but clan(" + clan + ") doesn't have such!");
			}
		}
	}
}
