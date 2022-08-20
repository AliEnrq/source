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
package org.l2jmobius.gameserver.network.serverpackets.collection;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.CollectionData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.PlayerCollectionData;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 12.04.2021
 */
public class ExCollectionInfo implements IClientOutgoingPacket
{
	final Player _Player;
	final List<PlayerCollectionData> _categoryList;
	final Set<Integer> _collections;
	final List<Integer> _favoriteList;
	final int _category;
	
	public ExCollectionInfo(Player player, int category)
	{
		_Player = player;
		_categoryList = player.getCollections().stream().filter(it -> CollectionData.getInstance().getCollection(it.getCollectionId()).getCategory() == category).collect(Collectors.toList());
		_collections = _categoryList.stream().map(PlayerCollectionData::getCollectionId).collect(Collectors.toSet());
		_favoriteList = player.getCollectionFavorites();
		_category = category;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_COLLECTION_INFO.writeId(packet);
		packet.writeD(_collections.size()); // size
		for (Integer collection : _collections)
		{
			final List<PlayerCollectionData> collectionCurrent = _categoryList.stream().filter(it -> it.getCollectionId() == collection).collect(Collectors.toList());
			packet.writeD(collectionCurrent.size());
			for (PlayerCollectionData current : collectionCurrent)
			{
				packet.writeC(current.getIndex());
				packet.writeD(current.getItemId());
				packet.writeH(CollectionData.getInstance().getCollection(collection).getItems().get(current.getIndex()).getEnchantLevel()); // enchant level
				packet.writeC(0); // unk flag for item
				packet.writeD(1); // count
			}
			packet.writeH(collection);
		}
		packet.writeD(_favoriteList.size()); // favourite size
		for (int favoriteCollection : _favoriteList)
		{
			packet.writeH(favoriteCollection);
		}
		packet.writeD(0);
		// loop unk
		// 1 h
		// d
		// h
		// loop end
		packet.writeC(_category);
		packet.writeH(0);
		return true;
	}
}
