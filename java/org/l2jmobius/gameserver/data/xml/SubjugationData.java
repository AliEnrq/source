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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.holders.SubjugationHolder;

/**
 * Written by Berezkin Nikolay, on 13.04.2021
 */
public class SubjugationData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SubjugationData.class.getName());
	
	private static final List<SubjugationHolder> _subjugations = new ArrayList<>();
	
	public SubjugationData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_subjugations.clear();
		parseDatapackFile("data/SubjugationData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _subjugations.size() + " data.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "purge", purgeNode ->
		{
			final StatSet set = new StatSet(parseAttributes(purgeNode));
			final int category = set.getInt("category");
			final List<int[]> hottimes = Arrays.stream(set.getString("hottimes").split(";")).map(it -> Arrays.stream(it.split("-")).mapToInt(Integer::parseInt).toArray()).collect(Collectors.toList());
			final Map<Integer, Integer> npcs = new HashMap<>();
			forEach(purgeNode, "npc", npcNode ->
			{
				final StatSet stats = new StatSet(parseAttributes(npcNode));
				final int npcId = stats.getInt("id");
				final int points = stats.getInt("points");
				npcs.put(npcId, points);
			});
			_subjugations.add(new SubjugationHolder(category, hottimes, npcs));
		}));
	}
	
	public SubjugationHolder getSubjugation(int category)
	{
		return _subjugations.stream().filter(it -> it.getCategory() == category).findFirst().orElse(null);
	}
	
	public static SubjugationData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SubjugationData INSTANCE = new SubjugationData();
	}
}
