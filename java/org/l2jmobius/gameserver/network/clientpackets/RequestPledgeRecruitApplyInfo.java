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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.ClanEntryStatus;
import org.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeRecruitApplyInfo;

/**
 * @author Sdw
 */
public class RequestPledgeRecruitApplyInfo implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final ClanEntryStatus status;
		if ((player.getClan() != null) && player.isClanLeader() && ClanEntryManager.getInstance().isClanRegistred(player.getClanId()))
		{
			status = ClanEntryStatus.ORDERED;
		}
		else if ((player.getClan() == null) && (ClanEntryManager.getInstance().isPlayerRegistred(player.getObjectId())))
		{
			status = ClanEntryStatus.WAITING;
		}
		else
		{
			status = ClanEntryStatus.DEFAULT;
		}
		
		player.sendPacket(new ExPledgeRecruitApplyInfo(status));
	}
}
