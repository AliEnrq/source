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
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.StatSet;

/**
 * Written by Berezkin Nikolay, on 11.04.2021
 */
public class PetTypesListData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(PetTypesListData.class.getName());
	
	private final Map<Integer, Entry<Integer, Integer>> _types = new HashMap<>();
	
	protected PetTypesListData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_types.clear();
		parseDatapackFile("data/PetTypes.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _types.size() + " pet types.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "pet", teleportNode ->
		{
			final StatSet set = new StatSet(parseAttributes(teleportNode));
			final int petType = set.getInt("id");
			final int skillId = set.getInt("skillId");
			final int skillLvl = set.getInt("skillLvl");
			_types.put(petType, new SimpleEntry<>(skillId, skillLvl));
		}));
	}
	
	public Map<Integer, Entry<Integer, Integer>> getTypes()
	{
		return _types;
	}
	
	public Entry<Integer, Integer> getType(int type)
	{
		return _types.get(type);
	}
	
	public static PetTypesListData getInstance()
	{
		return PetTypesListData.SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PetTypesListData INSTANCE = new PetTypesListData();
	}
}
