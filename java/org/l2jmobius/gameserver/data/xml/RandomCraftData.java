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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.holders.RandomCraftExtractDataHolder;
import org.l2jmobius.gameserver.model.holders.RandomCraftRewardDataHolder;
import org.l2jmobius.gameserver.model.holders.RandomCraftRewardItemHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;

/**
 * @author Mode, Mobius
 */
public class RandomCraftData implements IXmlReader
{
	private static final Map<Integer, RandomCraftExtractDataHolder> EXTRACT_DATA = new HashMap<>();
	private static final Map<Integer, RandomCraftRewardDataHolder> REWARD_DATA = new HashMap<>();
	
	protected RandomCraftData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		EXTRACT_DATA.clear();
		parseDatapackFile("data/RandomCraftExtractData.xml");
		final int extractCount = EXTRACT_DATA.size();
		if (extractCount > 0)
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + extractCount + " extraction data.");
		}
		
		REWARD_DATA.clear();
		parseDatapackFile("data/RandomCraftRewardData.xml");
		final int rewardCount = REWARD_DATA.size();
		if (rewardCount > 4)
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + rewardCount + " rewards.");
		}
		else if (rewardCount > 0)
		{
			LOGGER.info(getClass().getSimpleName() + ": Random craft rewards should be more than " + rewardCount + ".");
			REWARD_DATA.clear();
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "extract", extractNode ->
		{
			forEach(extractNode, "item", itemNode ->
			{
				final StatSet stats = new StatSet(parseAttributes(itemNode));
				final int itemId = stats.getInt("id");
				final long points = stats.getLong("points");
				final long fee = stats.getLong("fee");
				EXTRACT_DATA.put(itemId, new RandomCraftExtractDataHolder(points, fee));
			});
		}));
		
		forEach(doc, "list", listNode -> forEach(listNode, "rewards", rewardNode ->
		{
			forEach(rewardNode, "item", itemNode ->
			{
				final StatSet stats = new StatSet(parseAttributes(itemNode));
				final int itemId = stats.getInt("id");
				final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
				if (item == null)
				{
					LOGGER.warning(getClass().getSimpleName() + " unexisting item reward: " + itemId);
				}
				else
				{
					REWARD_DATA.put(itemId, new RandomCraftRewardDataHolder(stats.getInt("id"), stats.getLong("count", 1), Math.min(100, Math.max(0.00000000000001, stats.getDouble("chance", 100))), stats.getBoolean("announce", false)));
				}
			});
		}));
	}
	
	public boolean isEmpty()
	{
		return REWARD_DATA.isEmpty();
	}
	
	public RandomCraftRewardItemHolder getNewReward()
	{
		final List<RandomCraftRewardDataHolder> rewards = new ArrayList<>(REWARD_DATA.values());
		Collections.shuffle(rewards);
		
		RandomCraftRewardItemHolder result = null;
		while (result == null)
		{
			SEARCH: for (RandomCraftRewardDataHolder reward : rewards)
			{
				if (Rnd.get(100d) < reward.getChance())
				{
					result = new RandomCraftRewardItemHolder(reward.getItemId(), reward.getCount(), false, 20);
					break SEARCH;
				}
			}
		}
		return result;
	}
	
	public boolean isAnnounce(int id)
	{
		final RandomCraftRewardDataHolder holder = REWARD_DATA.get(id);
		if (holder == null)
		{
			return false;
		}
		return holder.isAnnounce();
	}
	
	public long getPoints(int id)
	{
		final RandomCraftExtractDataHolder holder = EXTRACT_DATA.get(id);
		if (holder == null)
		{
			return 0;
		}
		return holder.getPoints();
	}
	
	public long getFee(int id)
	{
		final RandomCraftExtractDataHolder holder = EXTRACT_DATA.get(id);
		if (holder == null)
		{
			return 0;
		}
		return holder.getFee();
	}
	
	public static RandomCraftData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RandomCraftData INSTANCE = new RandomCraftData();
	}
}
