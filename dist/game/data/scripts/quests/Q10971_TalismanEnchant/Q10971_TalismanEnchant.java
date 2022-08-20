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
package quests.Q10971_TalismanEnchant;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;

/**
 * @author Mobius, quangnguyen
 */
public class Q10971_TalismanEnchant extends Quest
{
	// NPC
	private static final int CAPTAIN_BATHIS = 30332;
	// Item
	private static final ItemHolder ADVENTURERS_TALISMAN = new ItemHolder(91937, 1);
	private static final ItemHolder SCROLL_OF_ENCHANT_ADVENTURERS_TALISMAN = new ItemHolder(95688, 1);
	
	// Misc
	private static final int MIN_LEVEL = 25;
	
	public Q10971_TalismanEnchant()
	{
		super(10971);
		addStartNpc(CAPTAIN_BATHIS);
		addTalkId(CAPTAIN_BATHIS);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_25_ENCHANT_TALISMAN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "30332.htm":
			case "30332-00.html":
			case "30332-01.htm":
			case "30332-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30332-03.htm":
			{
				qs.startQuest();
				player.sendPacket(new ExTutorialShowId(47));
				// TODO: Find a better way to do this: Tempfix for not giving items when already have them in inventory (bugging abort and re-accepting).
				if (player.getInventory().getAllItemsByItemId(ADVENTURERS_TALISMAN.getId()).isEmpty())
				{
					giveItems(player, ADVENTURERS_TALISMAN);
				}
				if (player.getInventory().getAllItemsByItemId(SCROLL_OF_ENCHANT_ADVENTURERS_TALISMAN.getId()).isEmpty())
				{
					giveItems(player, SCROLL_OF_ENCHANT_ADVENTURERS_TALISMAN);
				}
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			htmltext = "30332.htm";
		}
		else if (qs.isStarted())
		{
			boolean foundEnchant = false;
			SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(ADVENTURERS_TALISMAN.getId()))
			{
				if (item.getEnchantLevel() > 0)
				{
					foundEnchant = true;
					break SEARCH;
				}
			}
			if (foundEnchant)
			{
				addExpAndSp(player, 100000, 0);
				qs.exitQuest(false, true);
				htmltext = "30332-04.html";
			}
			else
			{
				htmltext = "30332-03.htm";
				player.sendPacket(new ExTutorialShowId(47));
			}
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
}
