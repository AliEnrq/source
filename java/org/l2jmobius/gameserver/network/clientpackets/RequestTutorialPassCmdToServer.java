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
import org.l2jmobius.gameserver.handler.AdminCommandHandler;
import org.l2jmobius.gameserver.handler.BypassHandler;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBypass;
import org.l2jmobius.gameserver.network.GameClient;

public class RequestTutorialPassCmdToServer implements IClientIncomingPacket
{
	private String _bypass = null;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_bypass = packet.readS();
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
		
		if (_bypass.startsWith("admin_"))
		{
			AdminCommandHandler.getInstance().useAdminCommand(player, _bypass, true);
		}
		else
		{
			final IBypassHandler handler = BypassHandler.getInstance().getHandler(_bypass);
			if (handler != null)
			{
				handler.useBypass(_bypass, player, null);
			}
		}
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerBypass(player, _bypass), player);
	}
}
