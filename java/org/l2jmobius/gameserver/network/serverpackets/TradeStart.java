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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.instancemanager.MentorManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class TradeStart extends AbstractItemPacket
{
	private final int _sendType;
	private final Player _player;
	private final Player _partner;
	private final Collection<Item> _itemList;
	private int _mask = 0;
	
	public TradeStart(int sendType, Player player)
	{
		_sendType = sendType;
		_player = player;
		_partner = player.getActiveTradeList().getPartner();
		_itemList = _player.getInventory().getAvailableItems(true, (_player.canOverrideCond(PlayerCondOverride.ITEM_CONDITIONS) && Config.GM_TRADE_RESTRICTED_ITEMS), false);
		if (_partner != null)
		{
			if (player.getFriendList().contains(_partner.getObjectId()))
			{
				_mask |= 0x01;
			}
			if ((player.getClanId() > 0) && (_partner.getClanId() == _partner.getClanId()))
			{
				_mask |= 0x02;
			}
			if ((MentorManager.getInstance().getMentee(player.getObjectId(), _partner.getObjectId()) != null) || (MentorManager.getInstance().getMentee(_partner.getObjectId(), player.getObjectId()) != null))
			{
				_mask |= 0x04;
			}
			if ((player.getAllyId() > 0) && (player.getAllyId() == _partner.getAllyId()))
			{
				_mask |= 0x08;
			}
			// Does not shows level
			if (_partner.isGM())
			{
				_mask |= 0x10;
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if ((_player.getActiveTradeList() == null) || (_partner == null))
		{
			return false;
		}
		
		OutgoingPackets.TRADE_START.writeId(packet);
		packet.writeC(_sendType);
		if (_sendType == 2)
		{
			packet.writeD(_itemList.size());
			packet.writeD(_itemList.size());
			for (Item item : _itemList)
			{
				writeItem(packet, item);
			}
		}
		else
		{
			packet.writeD(_partner.getObjectId());
			packet.writeC(_mask); // some kind of mask
			if ((_mask & 0x10) == 0)
			{
				packet.writeC(_partner.getLevel());
			}
		}
		return true;
	}
}
