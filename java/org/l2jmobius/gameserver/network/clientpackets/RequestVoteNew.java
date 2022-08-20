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
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestVoteNew implements IClientIncomingPacket
{
	private int _targetId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetId = packet.readD();
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
		
		final WorldObject object = player.getTarget();
		if (!(object instanceof Player))
		{
			if (object == null)
			{
				player.sendPacket(SystemMessageId.SELECT_TARGET);
			}
			else if (object.isFakePlayer() && FakePlayerData.getInstance().isTalkable(object.getName()))
			{
				if (player.getRecomLeft() <= 0)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_OUT_OF_RECOMMENDATIONS_TRY_AGAIN_LATER);
					return;
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
				sm.addString(FakePlayerData.getInstance().getProperName(object.getName()));
				sm.addInt(player.getRecomLeft());
				player.sendPacket(sm);
				
				player.setRecomLeft(player.getRecomLeft() - 1);
				player.sendPacket(new UserInfo(player));
				player.sendPacket(new ExVoteSystemInfo(player));
			}
			else
			{
				player.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			}
			return;
		}
		
		final Player target = (Player) object;
		if (target.getObjectId() != _targetId)
		{
			return;
		}
		
		if (target == player)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}
		
		if (player.getRecomLeft() <= 0)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_OUT_OF_RECOMMENDATIONS_TRY_AGAIN_LATER);
			return;
		}
		
		if (target.getRecomHave() >= 255)
		{
			player.sendPacket(SystemMessageId.YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}
		
		player.giveRecom(target);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addPcName(target);
		sm.addInt(player.getRecomLeft());
		player.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addPcName(player);
		target.sendPacket(sm);
		
		player.sendPacket(new UserInfo(player));
		target.broadcastUserInfo();
		
		player.sendPacket(new ExVoteSystemInfo(player));
		target.sendPacket(new ExVoteSystemInfo(target));
	}
}
