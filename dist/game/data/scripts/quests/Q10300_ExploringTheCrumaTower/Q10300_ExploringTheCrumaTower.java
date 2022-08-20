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
package quests.Q10300_ExploringTheCrumaTower;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
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
public class Q10300_ExploringTheCrumaTower extends Quest
{
	// NPCs
	private static final int ORVEN = 30857;
	private static final int CARSUS = 30483;
	// Monsters
	private static final int PORTA = 22200;
	private static final int EXCURO = 22201;
	private static final int MORDEO = 22202;
	private static final int RICENSEO = 22203;
	private static final int KRATOR = 22204;
	private static final int CATHEROK = 22205;
	private static final int PREMO = 22206;
	private static final int VALIDUS = 22207;
	private static final int DICOR = 22208;
	private static final int PERUM = 22209;
	private static final int TORFE = 22210;
	// Items
	private static final ItemHolder SAYHA_COOKIE = new ItemHolder(93274, 5);
	private static final ItemHolder SAYHA_STORM = new ItemHolder(91712, 2);
	private static final ItemHolder MAGIC_LAMP_CHARGING_POTION = new ItemHolder(91757, 3);
	
	// Misc
	private static final int MIN_LEVEL = 65;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10300_ExploringTheCrumaTower()
	{
		super(10300);
		addStartNpc(ORVEN);
		addTalkId(ORVEN, CARSUS);
		addKillId(PORTA, EXCURO, MORDEO, RICENSEO, KRATOR, CATHEROK, PREMO, VALIDUS, DICOR, PERUM, TORFE);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_65_EXPLORING_THE_CRUMA_TOWER);
	}
	
	@Override
	public boolean checkPartyMember(Player member, Npc npc)
	{
		final QuestState qs = getQuestState(member, false);
		return ((qs != null) && qs.isStarted());
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
			case "30857.htm":
			case "30857-01.htm":
			case "30857-02.htm":
			case "30483-01.html":
			case "30483-02.html":
			case "30483-04.html":
			{
				htmltext = event;
				break;
			}
			case "30857-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "reward":
			{
				if (qs.isCond(3))
				{
					addExpAndSp(player, 50000000, 1350000);
					giveItems(player, SAYHA_COOKIE);
					giveItems(player, SAYHA_STORM);
					giveItems(player, MAGIC_LAMP_CHARGING_POTION);
					htmltext = "30857-05.html";
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
			htmltext = "30857.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case ORVEN:
				{
					if (qs.isCond(1))
					{
						htmltext = "30857-03.html";
					}
					else if (qs.isCond(3))
					{
						htmltext = "30857-04.html";
					}
					break;
				}
				case CARSUS:
				{
					if (qs.isCond(1))
					{
						qs.setCond(2);
						htmltext = "30483-01.html";
					}
					else if (qs.isCond(2))
					{
						htmltext = "30483-02.html";
					}
					else if (qs.isCond(2))
					{
						final int killCount = qs.getInt(KILL_COUNT_VAR);
						if (killCount < 1000)
						{
							htmltext = "30483-02.html";
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
		if ((qs != null) && qs.isCond(2))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 1000)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(3, true);
				qs.unset(KILL_COUNT_VAR);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_MONSTERS_IN_THE_CRUMA_TOWER.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}
