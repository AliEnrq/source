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
package org.l2jmobius.gameserver.network.clientpackets.collection;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.collection.ExCollectionUpdateFavorite;

/**
 * Written by Berezkin Nikolay, on 12.04.2021
 */
public class RequestCollectionUpdateFavorite implements IClientIncomingPacket
{
	private int _isAdd;
	private int _collectionId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_isAdd = packet.readC();
		_collectionId = packet.readH();
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
		if (_isAdd == 1)
		{
			player.addCollectionFavorite(_collectionId);
		}
		else
		{
			player.removeCollectionFavorite(_collectionId);
		}
		player.sendPacket(new ExCollectionUpdateFavorite(_isAdd, _collectionId));
	}
}
