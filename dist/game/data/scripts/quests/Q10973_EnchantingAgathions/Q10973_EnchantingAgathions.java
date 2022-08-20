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
package quests.Q10973_EnchantingAgathions;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;

import quests.Q10292_SecretGarden.Q10292_SecretGarden;

/**
 * @author Mobius, quangnguyen
 */
public class Q10973_EnchantingAgathions extends Quest
{
	// NPC
	private static final int RAYMOND = 30289;
	// Item
	private static final int TRAVELER_AGATHION_GRIFFIN = 91935;
	private static final int ENCHANT_SCROLL_AGATHION_GRIFFIN = 93040;
	// Misc
	private static final int MIN_LEVEL = 35;
	
	public Q10973_EnchantingAgathions()
	{
		super(10973);
		addStartNpc(RAYMOND);
		addTalkId(RAYMOND);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondCompletedQuest(Q10292_SecretGarden.class.getSimpleName(), "30289-02.html");
		setQuestNameNpcStringId(NpcStringId.LV_35_ENCHANT_AGATHION);
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
			case "30289.htm":
			case "30289-00.htm":
			case "30289-01.htm":
			{
				htmltext = event;
				break;
			}
			case "30289-02.htm":
			{
				qs.startQuest();
				giveItems(player, ENCHANT_SCROLL_AGATHION_GRIFFIN, 1);
				player.sendPacket(new ExTutorialShowId(47));
				htmltext = event;
				break;
			}
			case "30289-05.html":
			{
				if (qs.isStarted())
				{
					boolean foundEnchant = false;
					SEARCH: for (Item item : player.getInventory().getAllItemsByItemId(TRAVELER_AGATHION_GRIFFIN))
					{
						if (item.getEnchantLevel() > 0)
						{
							foundEnchant = true;
							break SEARCH;
						}
					}
					if (foundEnchant)
					{
						addExpAndSp(player, 0, 10000);
						qs.exitQuest(false, true);
						htmltext = event;
						break;
					}
					htmltext = "30289-03.htm";
					player.sendPacket(new ExTutorialShowId(47));
					break;
				}
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
			htmltext = "30289.htm";
		}
		else if (qs.isStarted())
		{
			if (qs.isCond(1))
			{
				htmltext = "30289-04.html";
			}
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
}
