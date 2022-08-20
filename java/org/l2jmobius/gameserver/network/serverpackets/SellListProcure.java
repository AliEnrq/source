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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.model.CropProcure;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class SellListProcure implements IClientOutgoingPacket
{
	private final long _money;
	private final Map<Item, Long> _sellList = new HashMap<>();
	
	public SellListProcure(Player player, int castleId)
	{
		_money = player.getAdena();
		for (CropProcure c : CastleManorManager.getInstance().getCropProcure(castleId, false))
		{
			final Item item = player.getInventory().getItemByItemId(c.getId());
			if ((item != null) && (c.getAmount() > 0))
			{
				_sellList.put(item, c.getAmount());
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SELL_LIST_PROCURE.writeId(packet);
		packet.writeQ(_money); // money
		packet.writeD(0); // lease ?
		packet.writeH(_sellList.size()); // list size
		for (Entry<Item, Long> entry : _sellList.entrySet())
		{
			final Item item = entry.getKey();
			packet.writeH(item.getTemplate().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getDisplayId());
			packet.writeQ(entry.getValue()); // count
			packet.writeH(item.getTemplate().getType2());
			packet.writeH(0); // unknown
			packet.writeQ(0); // price, you shouldnt get any adena for crops, only raw materials
		}
		return true;
	}
}
