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
package quests.Q10990_PoisonExtraction;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

/**
 * Poison Extraction (10990)
 * @author RobikBobik, Mobius
 */
public class Q10990_PoisonExtraction extends Quest
{
	// NPCs
	private static final int GERALD = 30650;
	private static final int CAPTAIN_BATHIS = 30332;
	// Monsters
	private static final int HUNTER_TARANTULA = 20403;
	private static final int PLUNDER_TARANTULA = 20508;
	// Items
	private static final int VENOM_SAC = 91653;
	private static final ItemHolder SOE_TO_CAPTAIN_BATHIS = new ItemHolder(91651, 1);
	private static final ItemHolder SOE_NOVICE = new ItemHolder(10650, 20);
	private static final ItemHolder SPIRIT_ORE = new ItemHolder(3031, 50);
	private static final ItemHolder HP_POTS = new ItemHolder(91912, 50);
	private static final ItemHolder XP_GROWTH_SCROLL = new ItemHolder(49674, 1);
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10990_PoisonExtraction()
	{
		super(10990);
		addStartNpc(GERALD);
		addTalkId(GERALD, CAPTAIN_BATHIS);
		addKillId(HUNTER_TARANTULA, PLUNDER_TARANTULA);
		registerQuestItems(VENOM_SAC);
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_15_20_POISON_EXTRACTION);
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
			case "30650-01.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30332.html":
			{
				htmltext = event;
				break;
			}
			case "TELEPORT_TO_HUNTING_GROUND":
			{
				giveStoryBuffReward(npc, player);
				player.teleToLocation(135382, -207694, -3704);
				break;
			}
			case "NEXT_QUEST":
			{
				htmltext = "30650.htm";
				break;
			}
			case "reward":
			{
				if (qs.isStarted())
				{
					addExpAndSp(player, 600000, 13500);
					giveItems(player, SOE_NOVICE);
					giveItems(player, SPIRIT_ORE);
					giveItems(player, HP_POTS);
					giveItems(player, XP_GROWTH_SCROLL);
					giveStoryBuffReward(npc, player);
					if (CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
					{
						showOnScreenMsg(player, NpcStringId.YOU_VE_FINISHED_THE_TUTORIAL_TAKE_YOUR_1ST_CLASS_TRANSFER_AND_COMPLETE_YOUR_TRAINING_WITH_BATHIS_TO_BECOME_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
					}
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
			htmltext = "30650.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case GERALD:
				{
					if (qs.isCond(1))
					{
						htmltext = "30650-01.html";
					}
					break;
				}
				case CAPTAIN_BATHIS:
				{
					if (qs.isCond(2))
					{
						htmltext = "30332.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == GERALD)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			if (giveItemRandomly(killer, VENOM_SAC, 1, 30, 100, true))
			{
				qs.setCond(2, true);
				giveItems(killer, SOE_TO_CAPTAIN_BATHIS);
				showOnScreenMsg(killer, NpcStringId.YOU_VE_KILLED_ALL_THE_MONSTERS_USE_THE_SCROLL_OF_ESCAPE_IN_YOUR_INVENTORY_TO_RETURN_TO_CAPTAIN_BATHIS_IN_GLUDIO, ExShowScreenMessage.TOP_CENTER, 10000);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if (Config.DISABLE_TUTORIAL || ((qs != null) && qs.isCompleted()))
		{
			player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
		}
	}
}