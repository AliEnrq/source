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
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.ItemContainer;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.PacketLogger;

/**
 * @author Migi, DS
 */
public class ExReplySentPost extends AbstractItemPacket
{
	private final Message _msg;
	private Collection<Item> _items = null;
	
	public ExReplySentPost(Message msg)
	{
		_msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if ((attachments != null) && (attachments.getSize() > 0))
			{
				_items = attachments.getItems();
			}
			else
			{
				PacketLogger.warning("Message " + msg.getId() + " has attachments but itemcontainer is empty.");
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_REPLY_SENT_POST.writeId(packet);
		packet.writeD(0); // GOD
		packet.writeD(_msg.getId());
		packet.writeD(_msg.isLocked() ? 1 : 0);
		packet.writeS(_msg.getReceiverName());
		packet.writeS(_msg.getSubject());
		packet.writeS(_msg.getContent());
		if ((_items != null) && !_items.isEmpty())
		{
			packet.writeD(_items.size());
			for (Item item : _items)
			{
				writeItem(packet, item);
				packet.writeD(item.getObjectId());
			}
		}
		else
		{
			packet.writeD(0);
		}
		packet.writeQ(_msg.getReqAdena());
		packet.writeD(_msg.hasAttachments() ? 1 : 0);
		packet.writeD(_msg.isReturned() ? 1 : 0);
		return true;
	}
}
