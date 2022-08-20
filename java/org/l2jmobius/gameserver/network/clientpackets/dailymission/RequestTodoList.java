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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;

/**
 * @author UnAfraid
 */
public class RequestTodoList implements IClientIncomingPacket
{
	private int _tab;
	@SuppressWarnings("unused")
	private boolean _showAllLevels;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_tab = packet.readC(); // Daily Reward = 9, Event = 1, Instance Zone = 2
		_showAllLevels = packet.readC() == 1; // Disabled = 0, Enabled = 1
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
		
		switch (_tab)
		{
			// case 1:
			// {
			// player.sendPacket(new ExTodoListInzone());
			// break;
			// }
			// case 2:
			// {
			// player.sendPacket(new ExTodoListInzone());
			// break;
			// }
			case 9: // Daily Rewards
			{
				// Initial EW request should be false
				player.sendPacket(new ExOneDayReceiveRewardList(player, true));
				break;
			}
		}
	}
}
