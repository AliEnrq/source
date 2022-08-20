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
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.holders.CollectionDataHolder;
import org.l2jmobius.gameserver.model.holders.ItemCollectionData;
import org.l2jmobius.gameserver.model.item.ItemTemplate;

/**
 * Written by Berezkin Nikolay, on 04.05.2021
 */
public class CollectionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(CollectionData.class.getName());
	
	private static final Map<Integer, CollectionDataHolder> _collections = new HashMap<>();
	
	protected CollectionData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_collections.clear();
		parseDatapackFile("data/CollectionData.xml");
		
		if (!_collections.isEmpty())
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _collections.size() + " collections.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": System is disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("collection".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						Node att;
						final StatSet set = new StatSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						final int id = parseInteger(attrs, "id");
						final int optionId = parseInteger(attrs, "optionId");
						final int category = parseInteger(attrs, "category");
						List<ItemCollectionData> items = new ArrayList<>();
						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							attrs = b.getAttributes();
							if ("item".equalsIgnoreCase(b.getNodeName()))
							{
								final int itemId = parseInteger(attrs, "id");
								final long itemCount = parseLong(attrs, "count", 1L);
								final int itemEnchantLevel = parseInteger(attrs, "enchant_level", 0);
								final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
								if (item == null)
								{
									LOGGER.severe(getClass().getSimpleName() + ": Item template null for itemId: " + itemId + " collection item: " + id);
									continue;
								}
								items.add(new ItemCollectionData(itemId, itemCount, itemEnchantLevel));
							}
						}
						
						_collections.put(id, new CollectionDataHolder(id, optionId, category, items));
					}
				}
			}
		}
	}
	
	public CollectionDataHolder getCollection(int id)
	{
		return _collections.get(id);
	}
	
	public Collection<CollectionDataHolder> getCollections()
	{
		return _collections.values();
	}
	
	public static CollectionData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CollectionData INSTANCE = new CollectionData();
	}
}
