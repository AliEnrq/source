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
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ItemList extends AbstractItemPacket
{
	private final int _sendType;
	private final Player _player;
	private final List<Item> _items = new ArrayList<>();
	
	public ItemList(int sendType, Player player)
	{
		_sendType = sendType;
		_player = player;
		for (Item item : player.getInventory().getItems())
		{
			if (!item.isQuestItem())
			{
				_items.add(item);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ITEM_LIST.writeId(packet);
		if (_sendType == 2)
		{
			packet.writeC(_sendType);
			packet.writeD(_items.size());
			packet.writeD(_items.size());
			for (Item item : _items)
			{
				writeItem(packet, item);
			}
		}
		else
		{
			packet.writeC(1); // _showWindow ? 1 : 0
			packet.writeD(0);
			packet.writeD(_items.size());
		}
		writeInventoryBlock(packet, _player.getInventory());
		return true;
	}
}
