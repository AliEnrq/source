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
import org.l2jmobius.gameserver.data.xml.CollectionData;
import org.l2jmobius.gameserver.data.xml.OptionData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.CollectionDataHolder;
import org.l2jmobius.gameserver.model.holders.ItemCollectionData;
import org.l2jmobius.gameserver.model.holders.PlayerCollectionData;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.options.Options;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.collection.ExCollectionComplete;
import org.l2jmobius.gameserver.network.serverpackets.collection.ExCollectionRegister;

/**
 * @author Berezkin Nikolay, Mobius
 */
public class RequestCollectionRegister implements IClientIncomingPacket
{
	private int _collectionId;
	private int _index;
	private int _itemObjId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_collectionId = packet.readH();
		_index = packet.readD();
		_itemObjId = packet.readD();
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
		
		final Item item = player.getInventory().getItemByObjectId(_itemObjId);
		if (item == null)
		{
			player.sendMessage("Item not found.");
			return;
		}
		
		final CollectionDataHolder collection = CollectionData.getInstance().getCollection(_collectionId);
		if (collection == null)
		{
			player.sendMessage("Could not find collection.");
			return;
		}
		
		long count = 0;
		for (ItemCollectionData data : collection.getItems())
		{
			if ((data.getItemId() == item.getId()) && ((data.getEnchantLevel() == 0) || (data.getEnchantLevel() == item.getEnchantLevel())))
			{
				count = data.getCount();
				break;
			}
		}
		if ((count == 0) || (item.getCount() < count) || item.isEquipped())
		{
			player.sendMessage("Incorrect item count.");
			return;
		}
		
		player.destroyItem("Collection", item, count, player, true);
		
		player.sendPacket(new ExCollectionRegister(_collectionId, _index, item));
		
		player.getCollections().add(new PlayerCollectionData(_collectionId, item.getId(), _index));
		
		if (player.getCollections().stream().filter(it -> it.getCollectionId() == _collectionId).count() == collection.getItems().size())
		{
			player.sendPacket(new ExCollectionComplete(_collectionId));
			
			// Apply collection option if all requirements are met.
			final Options options = OptionData.getInstance().getOptions(collection.getOptionId());
			if (options != null)
			{
				options.apply(player);
			}
		}
	}
}
