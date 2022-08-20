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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class GMViewItemList extends AbstractItemPacket
{
	private final int _sendType;
	private final List<Item> _items = new ArrayList<>();
	private final int _limit;
	private final String _playerName;
	
	public GMViewItemList(int sendType, Player player)
	{
		_sendType = sendType;
		_playerName = player.getName();
		_limit = player.getInventoryLimit();
		for (Item item : player.getInventory().getItems())
		{
			_items.add(item);
		}
	}
	
	public GMViewItemList(int sendType, Pet cha)
	{
		_sendType = sendType;
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
		for (Item item : cha.getInventory().getItems())
		{
			_items.add(item);
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GM_VIEW_ITEM_LIST.writeId(packet);
		packet.writeC(_sendType);
		if (_sendType == 2)
		{
			packet.writeD(_items.size());
		}
		else
		{
			packet.writeS(_playerName);
			packet.writeD(_limit); // inventory limit
		}
		packet.writeD(_items.size());
		for (Item item : _items)
		{
			writeItem(packet, item);
		}
		return true;
	}
}
