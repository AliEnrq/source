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
package org.l2jmobius.gameserver.network.clientpackets.ranking;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.ranking.ExPetRankingList;

/**
 * Written by Berezkin Nikolay, on 10.05.2021
 */
public class RequestPetRankingList implements IClientIncomingPacket
{
	private int _season;
	private int _tabId;
	private int _type;
	private int _race;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_season = packet.readC();
		_tabId = packet.readC();
		_type = packet.readC();
		_race = packet.readD();
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
		
		player.sendPacket(new ExPetRankingList(player, _season, _tabId, _type, _race));
	}
}
