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
package quests.Q10972_CombiningGems;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExTutorialShowId;

/**
 * @author Mobius, quangnguyen
 */
public class Q10972_CombiningGems extends Quest
{
	// NPC
	private static final int CAPTAIN_BATHIS = 30332;
	// Items
	private static final int ADVENTURE_ROUGH_JEWEL_LV1 = 91936;
	private static final int ADVENTURE_ROUGH_JEWEL_LV2 = 93065;
	// Misc
	private static final int MIN_LEVEL = 30;
	
	public Q10972_CombiningGems()
	{
		super(10972);
		addStartNpc(CAPTAIN_BATHIS);
		addTalkId(CAPTAIN_BATHIS);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_30_JEWEL_COMPOUNDING);
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
				giveItems(player, ADVENTURE_ROUGH_JEWEL_LV1, 1);
				htmltext = event;
				break;
			}
			case "30332-05.html":
			{
				if (qs.isStarted())
				{
					if (getQuestItemsCount(player, ADVENTURE_ROUGH_JEWEL_LV2) > 0)
					{
						addExpAndSp(player, 100000, 0);
						qs.exitQuest(false, true);
						htmltext = event;
						break;
					}
					
					htmltext = "no_items.html";
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
			htmltext = "30332.htm";
		}
		else if (qs.isStarted())
		{
			htmltext = "30332-04.html";
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
}
