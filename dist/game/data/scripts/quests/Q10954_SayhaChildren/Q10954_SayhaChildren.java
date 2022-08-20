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
package quests.Q10954_SayhaChildren;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * @author quangnguyen
 */
public class Q10954_SayhaChildren extends Quest
{
	// NPCs
	private static final int ANDRA = 34209;
	private static final int KERKIN = 34210;
	// Monsters
	private static final int TRAINING_DUMMY = 22324;
	// Items
	private static final ItemHolder SOULSHOT_REWARD_1 = new ItemHolder(91927, 200);
	private static final ItemHolder SOULSHOT_REWARD_2 = new ItemHolder(91927, 400);
	private static final ItemHolder SOE_REWARD = new ItemHolder(10650, 5);
	private static final ItemHolder WW_POTION_REWARD = new ItemHolder(49036, 5);
	private static final ItemHolder HP_POTION_REWARD = new ItemHolder(91912, 50);
	// Misc
	private static final int MIN_LEVEL = 1;
	private static final int MAX_LEVEL = 2;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10954_SayhaChildren()
	{
		super(10954);
		addStartNpc(ANDRA);
		addTalkId(ANDRA, KERKIN);
		addKillId(TRAINING_DUMMY);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		addCondRace(Race.SYLPH, "no_race.html");
		setQuestNameNpcStringId(NpcStringId.LV_1_2_SAYHA_S_CHILDREN);
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
			case "TELEPORT_TO_HUNTING_GROUND":
			{
				player.teleToLocation(103133, 56163, -4048);
				break;
			}
			case "34209-02.htm":
			case "34209-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34209-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34210-05.html":
			{
				if (qs.isCond(4))
				{
					giveItems(player, SOE_REWARD);
					giveItems(player, WW_POTION_REWARD);
					giveItems(player, HP_POTION_REWARD);
					giveItems(player, SOULSHOT_REWARD_2);
					addExpAndSp(player, 224, 4);
					giveStoryBuffReward(npc, player);
					
					qs.exitQuest(false, true);
					htmltext = event;
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
		
		// Sylphs.
		if (player.getRace() != Race.SYLPH)
		{
			htmltext = "no_race.html";
		}
		
		if (qs.isCreated())
		{
			htmltext = "34209-01.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case ANDRA:
				{
					if (qs.isCond(1))
					{
						htmltext = "34209-04.htm";
					}
					break;
				}
				case KERKIN:
				{
					switch (qs.getCond())
					{
						case 1:
						{
							final int killCount = qs.getInt(KILL_COUNT_VAR);
							if (killCount < 1)
							{
								htmltext = "no_dummy-01.html";
							}
							break;
						}
						case 2:
						{
							qs.setCond(3);
							giveItems(player, SOULSHOT_REWARD_1);
							htmltext = "34210-02.html";
							break;
						}
						case 3:
						{
							final int killCount = qs.getInt(KILL_COUNT_VAR);
							if (killCount < 1)
							{
								htmltext = "no_dummy-02.html";
							}
							break;
						}
						case 4:
						{
							htmltext = "34210-03.html";
							break;
						}
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
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 1)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
				
			}
			else
			{
				qs.setCond(2, true);
				qs.unset(KILL_COUNT_VAR);
			}
		}
		else if ((qs != null) && qs.isCond(3))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 1)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
				
			}
			else
			{
				qs.setCond(4, true);
				qs.unset(KILL_COUNT_VAR);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.ATTACK_THE_TRAINING_DUMMY.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		
		if ((qs != null) && qs.isCond(3))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.ATTACK_THE_TRAINING_DUMMY.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
