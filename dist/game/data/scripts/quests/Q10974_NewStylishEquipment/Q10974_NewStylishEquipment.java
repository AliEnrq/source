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
package quests.Q10974_NewStylishEquipment;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * @author quangnguyen, Mobius
 */
public class Q10974_NewStylishEquipment extends Quest
{
	// NPC
	private static final int ORVEN = 30857;
	// Items
	private static final ItemHolder ADVENTURER_SHEEP_HAT = new ItemHolder(93044, 1);
	private static final ItemHolder ENCHANT_SCROLL_ADVENTURER_SHEEP_HAT = new ItemHolder(93043, 1);
	
	private static final ItemHolder ADVENTURER_BELT = new ItemHolder(93042, 1);
	private static final ItemHolder ENCHANT_SCROLL_ADVENTURER_BELT = new ItemHolder(93046, 1);
	
	private static final ItemHolder ADVENTURER_CLOAK = new ItemHolder(93041, 1);
	private static final ItemHolder ENCHANT_SCROLL_ADVENTURER_CLOAK = new ItemHolder(93045, 1);
	
	// Reward
	private static final ItemHolder ADVENTURER_PENDANT = new ItemHolder(95690, 1);
	private static final ItemHolder SAYHA_GUST = new ItemHolder(91776, 2);
	// Misc
	private static final int MIN_LEVEL = 40;
	
	public Q10974_NewStylishEquipment()
	{
		super(10974);
		addStartNpc(ORVEN);
		addTalkId(ORVEN);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_40_NEW_STYLISH_EQUIPMENT);
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
			case "30857-00.htm":
			case "30857-01.htm":
			case "30857-02.htm":
			case "30857-09.html":
			{
				htmltext = event;
				break;
			}
			case "30857-03.htm":
			{
				qs.startQuest();
				// TODO: Find a better way to do this: Tempfix for not giving items when already have them in inventory (bugging abort and re-accepting).
				if (player.getInventory().getAllItemsByItemId(ADVENTURER_SHEEP_HAT.getId()).isEmpty())
				{
					giveItems(player, ADVENTURER_SHEEP_HAT);
				}
				if (player.getInventory().getAllItemsByItemId(ENCHANT_SCROLL_ADVENTURER_SHEEP_HAT.getId()).isEmpty())
				{
					giveItems(player, ENCHANT_SCROLL_ADVENTURER_SHEEP_HAT);
				}
				htmltext = event;
				break;
			}
			case "30857-04.html":
			{
				if (qs.isCond(1))
				{
					boolean foundEnchant = false;
					SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(ADVENTURER_SHEEP_HAT.getId()))
					{
						if (item.getEnchantLevel() > 0)
						{
							foundEnchant = true;
							break SEARCH;
						}
					}
					if (foundEnchant)
					{
						qs.setCond(2);
						htmltext = event;
						break;
					}
					htmltext = "no_sheep_hat.html";
				}
				break;
			}
			case "30857-05.html":
			{
				qs.startQuest();
				// TODO: Find a better way to do this: Tempfix for not giving items when already have them in inventory (bugging abort and re-accepting).
				if (player.getInventory().getAllItemsByItemId(ADVENTURER_BELT.getId()).isEmpty())
				{
					giveItems(player, ADVENTURER_BELT);
				}
				if (player.getInventory().getAllItemsByItemId(ENCHANT_SCROLL_ADVENTURER_BELT.getId()).isEmpty())
				{
					giveItems(player, ENCHANT_SCROLL_ADVENTURER_BELT);
				}
				htmltext = event;
				break;
			}
			case "30857-06.html":
			{
				if (qs.isCond(2))
				{
					boolean foundEnchant = false;
					SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(ADVENTURER_BELT.getId()))
					{
						if (item.getEnchantLevel() > 0)
						{
							foundEnchant = true;
							break SEARCH;
						}
					}
					if (foundEnchant)
					{
						qs.setCond(3);
						htmltext = event;
						break;
					}
					htmltext = "no_belt.html";
				}
				break;
			}
			case "30857-07.html":
			{
				qs.startQuest();
				// TODO: Find a better way to do this: Tempfix for not giving items when already have them in inventory (bugging abort and re-accepting).
				if (player.getInventory().getAllItemsByItemId(ADVENTURER_CLOAK.getId()).isEmpty())
				{
					giveItems(player, ADVENTURER_CLOAK);
				}
				if (player.getInventory().getAllItemsByItemId(ENCHANT_SCROLL_ADVENTURER_CLOAK.getId()).isEmpty())
				{
					giveItems(player, ENCHANT_SCROLL_ADVENTURER_CLOAK);
				}
				htmltext = event;
				break;
			}
			case "30857-08.html":
			{
				if (qs.isCond(3))
				{
					boolean foundEnchant = false;
					SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(ADVENTURER_CLOAK.getId()))
					{
						if (item.getEnchantLevel() > 0)
						{
							foundEnchant = true;
							break SEARCH;
						}
					}
					if (foundEnchant)
					{
						qs.setCond(4);
						htmltext = event;
						break;
					}
					htmltext = "no_cloak.html";
				}
				break;
			}
			case "reward":
			{
				if (qs.isCond(4))
				{
					giveItems(player, ADVENTURER_PENDANT);
					giveItems(player, SAYHA_GUST);
					qs.exitQuest(false, true);
					htmltext = "30857-10.html";
				}
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
			htmltext = "30857-00.htm";
		}
		else if (qs.isStarted())
		{
			switch (qs.getCond())
			{
				case 1:
				{
					boolean foundEnchant = false;
					SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(ADVENTURER_SHEEP_HAT.getId()))
					{
						if (item.getEnchantLevel() > 0)
						{
							foundEnchant = true;
							break SEARCH;
						}
					}
					if (foundEnchant)
					{
						qs.setCond(2);
						htmltext = "30857-04.html";
					}
					else
					{
						htmltext = "no_sheep_hat.html";
					}
					break;
				}
				case 2:
				{
					boolean foundEnchant = false;
					SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(ADVENTURER_BELT.getId()))
					{
						if (item.getEnchantLevel() > 0)
						{
							foundEnchant = true;
							break SEARCH;
						}
					}
					if (foundEnchant)
					{
						qs.setCond(3);
						htmltext = "30857-06.html";
					}
					else
					{
						htmltext = "no_belt.html";
					}
					break;
				}
				case 3:
				{
					boolean foundEnchant = false;
					SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(ADVENTURER_CLOAK.getId()))
					{
						if (item.getEnchantLevel() > 0)
						{
							foundEnchant = true;
							break SEARCH;
						}
					}
					if (foundEnchant)
					{
						qs.setCond(4);
						htmltext = "30857-08.html";
					}
					else
					{
						htmltext = "no_cloak.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
}
