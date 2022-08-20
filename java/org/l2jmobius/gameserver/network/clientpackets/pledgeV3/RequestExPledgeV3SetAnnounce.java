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
package org.l2jmobius.gameserver.network.clientpackets.pledgeV3;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.pledgeV3.ExPledgeV3Info;

/**
 * Written by Berezkin Nikolay, on 04.05.2021
 */
public class RequestExPledgeV3SetAnnounce implements IClientIncomingPacket
{
	private String _announce;
	private boolean _enterWorldShow;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_announce = packet.readString();
		_enterWorldShow = packet.readC() == 1;
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player activeChar = client.getPlayer();
		if (activeChar == null)
		{
			return;
		}
		
		final Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		clan.setNotice(_announce);
		clan.setNoticeEnabled(_enterWorldShow);
		clan.broadcastToOnlineMembers(new ExPledgeV3Info(clan.getExp(), clan.getRank(), clan.getNotice(), clan.isNoticeEnabled()));
	}
}
