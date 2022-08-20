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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Mobius
 */
public class PackageSendableList extends AbstractItemPacket
{
	private final Collection<Item> _items;
	private final int _objectId;
	private final long _adena;
	private final int _sendType;
	
	public PackageSendableList(int sendType, Player player, int objectId)
	{
		_sendType = sendType;
		_items = player.getInventory().getAvailableItems(true, true, true);
		_objectId = objectId;
		_adena = player.getAdena();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PACKAGE_SENDABLE_LIST.writeId(packet);
		packet.writeC(_sendType);
		if (_sendType == 2)
		{
			packet.writeD(_items.size());
			packet.writeD(_items.size());
			for (Item item : _items)
			{
				writeItem(packet, item);
				packet.writeD(item.getObjectId());
			}
		}
		else
		{
			packet.writeD(_objectId);
			packet.writeQ(_adena);
			packet.writeD(_items.size());
		}
		return true;
	}
}
