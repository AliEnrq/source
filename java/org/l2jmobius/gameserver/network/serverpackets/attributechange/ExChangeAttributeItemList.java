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
package org.l2jmobius.gameserver.network.serverpackets.attributechange;

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ItemInfo;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.AbstractItemPacket;

/**
 * @author Mobius
 */
public class ExChangeAttributeItemList extends AbstractItemPacket
{
	private final List<ItemInfo> _itemsList;
	private final int _itemId;
	
	public ExChangeAttributeItemList(int itemId, List<ItemInfo> itemList)
	{
		_itemId = itemId;
		_itemsList = itemList;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CHANGE_ATTRIBUTE_ITEM_LIST.writeId(packet);
		packet.writeD(_itemId);
		packet.writeD(_itemsList.size());
		for (ItemInfo item : _itemsList)
		{
			writeItem(packet, item);
		}
		return true;
	}
}
