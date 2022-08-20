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
package org.l2jmobius.gameserver.network.clientpackets.dailymission;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.DailyMissionData;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.dailymission.ExConnectedTimeAndGettableReward;
import org.l2jmobius.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;

/**
 * @author Sdw
 */
public class RequestOneDayRewardReceive implements IClientIncomingPacket
{
	private int _id;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_id = packet.readH();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!client.getFloodProtectors().canPerformPlayerAction())
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Collection<DailyMissionDataHolder> rewards = DailyMissionData.getInstance().getDailyMissionData(_id);
		if ((rewards == null) || rewards.isEmpty())
		{
			return;
		}
		
		for (DailyMissionDataHolder holder : rewards)
		{
			if (holder.isDisplayable(player))
			{
				holder.requestReward(player);
			}
		}
		
		player.sendPacket(new ExOneDayReceiveRewardList(player, true));
		player.sendPacket(new ExConnectedTimeAndGettableReward(player));
	}
}
