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
import org.l2jmobius.gameserver.data.xml.HennaData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.henna.Henna;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.HennaItemDrawInfo;

/**
 * @author Zoey76
 */
public class RequestHennaItemInfo implements IClientIncomingPacket
{
	private int _symbolId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_symbolId = packet.readD();
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
		
		final Henna henna = HennaData.getInstance().getHennaByDyeId(_symbolId);
		if (henna == null)
		{
			if (_symbolId != 0)
			{
				PacketLogger.warning(getClass().getSimpleName() + ": Invalid Henna Id: " + _symbolId + " from " + player);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.sendPacket(new HennaItemDrawInfo(henna, player));
	}
}
