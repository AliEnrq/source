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
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.AskJoinAlly;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestJoinAlly implements IClientIncomingPacket
{
	private int _objectId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
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
		
		final Player target = World.getInstance().getPlayer(_objectId);
		if (target == null)
		{
			player.sendPacket(SystemMessageId.THE_TARGET_CANNOT_BE_INVITED);
			return;
		}
		
		final Clan clan = player.getClan();
		if (clan == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION);
			return;
		}
		
		if (!clan.checkAllyJoinCondition(player, target))
		{
			return;
		}
		if (!player.getRequest().setRequest(target, this))
		{
			return;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_LEADER_S2_HAS_REQUESTED_AN_ALLIANCE);
		sm.addString(player.getClan().getAllyName());
		sm.addString(player.getName());
		target.sendPacket(sm);
		target.sendPacket(new AskJoinAlly(player.getObjectId(), player.getClan().getAllyName()));
	}
}
