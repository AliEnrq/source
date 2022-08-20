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

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.network.ConnectionState;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectionInfo;
import org.l2jmobius.gameserver.network.serverpackets.RestartResponse;
import org.l2jmobius.gameserver.util.OfflineTradeUtil;

/**
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestart implements IClientIncomingPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
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
		
		if (!player.canLogout())
		{
			player.sendPacket(RestartResponse.FALSE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Unregister from olympiad.
		if (OlympiadManager.getInstance().isRegistered(player))
		{
			OlympiadManager.getInstance().unRegisterNoble(player);
		}
		
		final Instance world = player.getInstanceWorld();
		if (world != null)
		{
			if (Config.RESTORE_PLAYER_INSTANCE)
			{
				player.getVariables().set("INSTANCE_RESTORE", world.getId());
			}
			else
			{
				final Location location = world.getExitLocation(player);
				if (location != null)
				{
					player.teleToLocation(location);
				}
				else
				{
					player.teleToLocation(TeleportWhereType.TOWN);
				}
				player.getSummonedNpcs().forEach(npc -> npc.teleToLocation(player, true));
			}
			world.onInstanceChange(player, false);
		}
		
		LOGGER_ACCOUNTING.info("Logged out, " + client);
		
		if (!OfflineTradeUtil.enteredOfflineMode(player))
		{
			Disconnection.of(client, player).storeMe().deleteMe();
		}
		
		// Return the client to the authenticated status.
		client.setConnectionState(ConnectionState.AUTHENTICATED);
		
		client.sendPacket(RestartResponse.TRUE);
		
		// Send character list.
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
