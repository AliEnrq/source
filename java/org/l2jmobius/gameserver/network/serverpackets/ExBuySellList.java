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
import java.util.Collection;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author ShanSoft
 */
public class ExBuySellList extends AbstractItemPacket
{
	private final List<Item> _sellList = new ArrayList<>();
	private Collection<Item> _refundList = null;
	private final boolean _done;
	private final int _inventorySlots;
	
	public ExBuySellList(Player player, boolean done)
	{
		final Summon pet = player.getPet();
		for (Item item : player.getInventory().getItems())
		{
			if (!item.isEquipped() && item.isSellable() && ((pet == null) || (item.getObjectId() != pet.getControlObjectId())))
			{
				_sellList.add(item);
			}
		}
		_inventorySlots = player.getInventory().getNonQuestSize();
		if (player.hasRefund())
		{
			_refundList = player.getRefund().getItems();
		}
		_done = done;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_BUY_SELL_LIST.writeId(packet);
		packet.writeD(1); // Type SELL
		packet.writeD(_inventorySlots);
		if ((_sellList != null))
		{
			packet.writeH(_sellList.size());
			for (Item item : _sellList)
			{
				writeItem(packet, item);
				packet.writeQ(Config.MERCHANT_ZERO_SELL_PRICE ? 0 : item.getTemplate().getReferencePrice() / 2);
			}
		}
		else
		{
			packet.writeH(0);
		}
		if ((_refundList != null) && !_refundList.isEmpty())
		{
			packet.writeH(_refundList.size());
			int i = 0;
			for (Item item : _refundList)
			{
				writeItem(packet, item);
				packet.writeD(i++);
				packet.writeQ(Config.MERCHANT_ZERO_SELL_PRICE ? 0 : (item.getTemplate().getReferencePrice() / 2) * item.getCount());
			}
		}
		else
		{
			packet.writeH(0);
		}
		packet.writeC(_done ? 1 : 0);
		return true;
	}
}
