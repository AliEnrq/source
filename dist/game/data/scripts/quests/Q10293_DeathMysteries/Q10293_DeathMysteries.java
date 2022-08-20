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
package quests.Q10293_DeathMysteries;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Death Mysteries (10965)
 * @author RobikBobik
 */
public class Q10293_DeathMysteries extends Quest
{
	// NPC
	private static final int RAYMOND = 30289;
	private static final int MAXIMILLIAN = 30120;
	// Monsters
	private static final int WYRM = 20176;
	private static final int GUARDIAN_BASILISK = 20550;
	private static final int ROAD_SCAVENGER = 20551;
	private static final int FETTERED_SOUL = 20552;
	private static final int WINDUS = 20553;
	private static final int GRANDIS = 20554;
	// Items
	private static final ItemHolder SOE_DEATH_PASS = new ItemHolder(95589, 1);
	private static final ItemHolder SOE_HIGH_PRIEST_MAXIMILIAN = new ItemHolder(95595, 1);
	private static final ItemHolder SAYHA_GUST = new ItemHolder(91776, 9);
	private static final ItemHolder MAGIC_LAMP_CHARGING_POTION = new ItemHolder(91757, 1);
	private static final ItemHolder SCROLL_OF_ENCHANT_ADEN_WEAPON = new ItemHolder(93038, 2);
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final int MIN_LEVEL = 35;
	private static final int MAX_LEVEL = 40;
	
	public Q10293_DeathMysteries()
	{
		super(10293);
		addStartNpc(RAYMOND);
		addTalkId(RAYMOND, MAXIMILLIAN);
		addKillId(WYRM, GUARDIAN_BASILISK, ROAD_SCAVENGER, FETTERED_SOUL, WINDUS, GRANDIS);
		setQuestNameNpcStringId(NpcStringId.LV_35_40_DEATH_MYSTERIES);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
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
			case "TELEPORT_TO_MAXIMILLIAN":
			{
				player.teleToLocation(86845, 148626, -3402);
				break;
			}
			case "30289-01.htm":
			{
				htmltext = event;
				break;
			}
			case "30289-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30289-03.htm":
			{
				qs.startQuest();
				showOnScreenMsg(player, NpcStringId.BEFORE_YOU_GO_FOR_A_BATTLE_CHECK_THE_SKILL_WINDOW_ALT_K_NEW_SKILLS_WILL_HELP_YOU_TO_GET_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000, player.getName());
				htmltext = event;
				break;
			}
			case "30120-01.html":
			{
				htmltext = event;
				break;
			}
			case "30120-02.html":
			{
				htmltext = event;
				break;
			}
			case "30120-03.html":
			{
				qs.setCond(2, true);
				giveItems(player, SOE_DEATH_PASS);
				htmltext = event;
				break;
			}
			case "30120-05.html":
			{
				if (qs.isStarted())
				{
					addExpAndSp(player, (ExperienceData.getInstance().getExpForLevel(40) + 100) - player.getExp(), 160000);
					giveItems(player, SAYHA_GUST);
					giveItems(player, MAGIC_LAMP_CHARGING_POTION);
					giveItems(player, SCROLL_OF_ENCHANT_ADEN_WEAPON);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
			}
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
			if (killCount < 100)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(3, true);
				qs.unset(KILL_COUNT_VAR);
				giveItems(killer, SOE_HIGH_PRIEST_MAXIMILIAN);
				killer.sendPacket(new ExShowScreenMessage(NpcStringId.MONSTERS_OF_THE_DEATH_PASS_ARE_KILLED_USE_THE_TELEPORT_OR_THE_SCROLL_OF_ESCAPE_TO_GET_TO_HIGH_PRIEST_MAXIMILIAN_IN_GIRAN, 2, 5000));
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
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_DEATH_PASS.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			htmltext = "30289-01.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case RAYMOND:
				{
					if (qs.isCond(1))
					{
						htmltext = "30289-01.htm";
					}
					break;
				}
				case MAXIMILLIAN:
				{
					if (qs.isCond(1))
					{
						htmltext = "30120.html";
					}
					else if (qs.isCond(3))
					{
						htmltext = "30120-04.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == RAYMOND)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
}