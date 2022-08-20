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
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.PledgeReceiveWarList;
import org.l2jmobius.gameserver.network.serverpackets.pledgeV3.ExPledgeClassicRaidInfo;
import org.l2jmobius.gameserver.network.serverpackets.pledgeV3.ExPledgeV3Info;

/**
 * Written by Berezkin Nikolay, on 04.05.2021
 */
public class RequestExPledgeV3Info implements IClientIncomingPacket
{
	private int _page;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_page = packet.readC();
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
		
		if (player.getClan() == null)
		{
			return;
		}
		
		player.sendPacket(new ExPledgeV3Info(player.getClan().getExp(), player.getClan().getRank(), player.getClan().getNotice(), player.getClan().isNoticeEnabled()));
		player.sendPacket(new PledgeReceiveWarList(player.getClan(), _page));
		player.sendPacket(new ExPledgeClassicRaidInfo(player));
	}
}
