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
package quests.Q10978_MissingPets;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * @author quangnguyen
 */
public class Q10978_MissingPets extends Quest
{
	// NPCs
	private static final int LEMPER = 30869;
	private static final int COOPER = 30829;
	// Items
	private static final ItemHolder SOULSHOT_TICKET = new ItemHolder(90907, 100);
	private static final ItemHolder PET_GUIDE = new ItemHolder(94118, 1);
	// Misc
	private static final int MIN_LEVEL = 76;
	
	public Q10978_MissingPets()
	{
		super(10978);
		addStartNpc(LEMPER);
		addTalkId(LEMPER, COOPER);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_76_MISSING_PETS);
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
			case "30869.htm":
			case "30869-01.htm":
			case "30869-02.htm":
			case "30829.html":
			case "30829-01.html":
			{
				htmltext = event;
				break;
			}
			case "30869-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30829-02.html":
			{
				htmltext = event;
				break;
			}
			case "reward":
			{
				if (qs.isCond(1))
				{
					giveItems(player, SOULSHOT_TICKET);
					giveItems(player, PET_GUIDE);
					htmltext = "30829-03.htm";
					qs.exitQuest(false, true);
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
			htmltext = "30869.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case LEMPER:
				{
					if (qs.isCond(1))
					{
						htmltext = "30869-01.htm";
					}
					break;
				}
				case COOPER:
				{
					if (qs.isCond(1))
					{
						htmltext = "30829.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == LEMPER)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
}
