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
package quests.Q10296_LetsPayRespectsToOurFallenBrethren;

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
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10295_RespectforGraves.Q10295_RespectforGraves;

/**
 * @author quangnguyen
 */
public class Q10296_LetsPayRespectsToOurFallenBrethren extends Quest
{
	// NPCs
	private static final int ORVEN = 30857;
	// Monsters
	private static final int ACHER_OF_DESTRUCTION = 21001;
	private static final int GRAVEYARD_LICH = 21003;
	private static final int DISMAL_POLE = 21004;
	private static final int GRAVEYARD_PREDATOR = 21005;
	private static final int DOOM_KNIGHT = 20674;
	private static final int DOOM_SCOUT = 21002;
	private static final int DOOM_SERVANT = 21006;
	private static final int DOOM_GUARD = 21007;
	private static final int DOOM_ARCHER = 21008;
	private static final int DOOM_TROOPER = 21009;
	private static final int DOOM_WARRIOR = 21010;
	private static final int GUARD_BUTCHER = 22101;
	// Items
	private static final ItemHolder SOE_FIELDS_OF_MASSACRE = new ItemHolder(95592, 1);
	private static final ItemHolder SOE_HIGH_PRIEST_OVEN = new ItemHolder(91768, 1);
	private static final ItemHolder MAGIC_LAMP_CHARGING_POTION = new ItemHolder(91757, 3);
	private static final ItemHolder SOULSHOT_TICKET = new ItemHolder(90907, 10);
	private static final ItemHolder SAYHA_GUST = new ItemHolder(91776, 9);
	private static final ItemHolder SPIRIT_ORE = new ItemHolder(3031, 450);
	// Misc
	private static final int MIN_LEVEL = 52;
	private static final int MAX_LEVEL = 58;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10296_LetsPayRespectsToOurFallenBrethren()
	{
		super(10296);
		addStartNpc(ORVEN);
		addTalkId(ORVEN);
		addKillId(ACHER_OF_DESTRUCTION, GRAVEYARD_LICH, DISMAL_POLE, GRAVEYARD_PREDATOR, DOOM_KNIGHT, DOOM_SCOUT, DOOM_SERVANT, DOOM_GUARD, DOOM_ARCHER, DOOM_TROOPER, DOOM_WARRIOR, GUARD_BUTCHER);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		addCondCompletedQuest(Q10295_RespectforGraves.class.getSimpleName(), "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_52_58_PAY_RESPECTS_TO_FALLEN_BRETHREN);
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
			{
				htmltext = event;
				break;
			}
			case "30857-03.htm":
			{
				qs.startQuest();
				giveItems(player, SOE_FIELDS_OF_MASSACRE);
				htmltext = event;
				break;
			}
			case "reward":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 30000000, 810000);
					giveItems(player, MAGIC_LAMP_CHARGING_POTION);
					giveItems(player, SOULSHOT_TICKET);
					giveItems(player, SAYHA_GUST);
					giveItems(player, SPIRIT_ORE);
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
			if (qs.isCond(1))
			{
				final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
				if ((killCount < 400) && (player.getLevel() < 58))
				{
					htmltext = "30857-03.html";
				}
				else
				{
					htmltext = "30857-01.htm";
				}
			}
			else if (qs.isCond(2))
			{
				htmltext = "30857-04.html";
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
			if (killCount < 400)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(2, true);
				showOnScreenMsg(killer, NpcStringId.ALL_MISSIONS_ARE_COMPLETED_USE_SCROLL_OF_ESCAPE_HIGH_PRIEST_ORVEN_TO_GET_TO_HIGH_PRIEST_ORVEN_IN_ADEN, ExShowScreenMessage.TOP_CENTER, 10000);
				giveItems(killer, SOE_HIGH_PRIEST_OVEN);
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
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_FIELDS_OF_MASSACRE_2.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			holder.add(new NpcLogListHolder(NpcStringId.LEVEL_58_ACCOMPLISHED, player.getLevel() > 57 ? 1 : 0));
			return holder;
		}
		return super.getNpcLogList(player);
	}
	
}
