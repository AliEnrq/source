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
package quests.Q10292_SecretGarden;

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
 * Secret Garden (10964)
 * @author RobikBobik
 */
public class Q10292_SecretGarden extends Quest
{
	// NPC
	private static final int CAPTAIN_BATHIS = 30332;
	private static final int RAYMOND = 30289;
	// Monsters
	private static final int HARPY = 20145;
	private static final int MEDUSA = 20158;
	private static final int WYRM = 20176;
	private static final int TURAK_BUGBEAR = 20248;
	private static final int TURAK_BUGBEAR_WARRIOR = 20249;
	// Items
	private static final ItemHolder SOE_GORGON_FLOWER_GARDEN = new ItemHolder(95588, 1);
	private static final ItemHolder SOE_HIGH_PRIEST_RAYMOND = new ItemHolder(91736, 1);
	private static final ItemHolder SCROLL_ENCHANT_ADEN_WEAPON = new ItemHolder(93038, 2);
	private static final ItemHolder TRAVELER_AGATHION_SUMMON_BRACELET = new ItemHolder(91933, 1);
	private static final ItemHolder TRAVELER_AGATHION_GRIFFIN = new ItemHolder(91935, 1);
	// Misc
	private static final String KILL_COUNT_VAR = "KillCount";
	private static final int MIN_LEVEL = 30;
	private static final int MAX_LEVEL = 35;
	
	public Q10292_SecretGarden()
	{
		super(10292);
		addStartNpc(CAPTAIN_BATHIS);
		addTalkId(CAPTAIN_BATHIS, RAYMOND);
		addKillId(HARPY, MEDUSA, WYRM, TURAK_BUGBEAR, TURAK_BUGBEAR_WARRIOR);
		setQuestNameNpcStringId(NpcStringId.LV_30_35_SECRET_GARDEN);
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
			case "30332-01.htm":
			{
				qs.startQuest();
				showOnScreenMsg(player, NpcStringId.BEFORE_YOU_GO_FOR_A_BATTLE_CHECK_THE_SKILL_WINDOW_ALT_K_NEW_SKILLS_WILL_HELP_YOU_TO_GET_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000, player.getName());
				htmltext = event;
				break;
			}
			case "Nod":
			{
				break;
			}
			case "30289-01.html":
			{
				htmltext = event;
				break;
			}
			case "30289-02.html":
			{
				qs.setCond(2, true);
				giveItems(player, SOE_GORGON_FLOWER_GARDEN);
				htmltext = event;
				break;
			}
			case "30289-03.html":
			case "30289-04.html":
			{
				htmltext = event;
				break;
			}
			case "30289-05.html":
			{
				if (qs.isStarted())
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.YOU_VE_GOT_ADVENTURER_S_AGATHION_BRACELET_AND_ADVENTURER_S_AGATHION_GRIFFIN_COMPLETE_THE_TUTORIAL_AND_TRY_TO_USE_THE_AGATHION, 2, 5000));
					addExpAndSp(player, (ExperienceData.getInstance().getExpForLevel(35) + 100) - player.getExp(), 135000);
					giveItems(player, TRAVELER_AGATHION_SUMMON_BRACELET);
					giveItems(player, TRAVELER_AGATHION_GRIFFIN);
					giveItems(player, SCROLL_ENCHANT_ADEN_WEAPON);
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
			if (killCount < 70)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(3, true);
				qs.unset(KILL_COUNT_VAR);
				giveItems(killer, SOE_HIGH_PRIEST_RAYMOND);
				killer.sendPacket(new ExShowScreenMessage(NpcStringId.MONSTERS_OF_THE_GORGON_FLOWER_GARDEN_ARE_KILLED_USE_THE_TELEPORT_TO_GET_TO_HIGH_PRIEST_RAYMOND_IN_GLUDIO, 2, 5000));
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
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_GORGON_FLOWER_GARDEN.getId(), true, qs.getInt(KILL_COUNT_VAR)));
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
			htmltext = "30332.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case CAPTAIN_BATHIS:
				{
					if (qs.isCond(1))
					{
						htmltext = "30332-01.htm";
					}
					break;
				}
				case RAYMOND:
				{
					if (qs.isCond(1))
					{
						htmltext = "30289.html";
					}
					else if (qs.isCond(3))
					{
						htmltext = "30289-04.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == CAPTAIN_BATHIS)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
}