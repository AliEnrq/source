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
package quests.Q10956_WeSylphs;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

/**
 * @author quangnguyen
 */
public class Q10956_WeSylphs extends Quest
{
	// NPCs
	private static final int KERKIN = 34210;
	private static final int EVIA = 34211;
	// Monsters
	private static final int VOLATU = 22328;
	private static final int PHOTORONI = 22329;
	private static final int ALES = 22330;
	// Items
	private static final ItemHolder SOE_EVIA = new ItemHolder(95587, 1);
	private static final ItemHolder SOE_NOVICE = new ItemHolder(10650, 20);
	private static final ItemHolder SPIRIT_ORE = new ItemHolder(3031, 50);
	private static final ItemHolder XP_GROWTH_SCROLL = new ItemHolder(49674, 1);
	private static final ItemHolder HP_POTION_REWARD = new ItemHolder(91912, 50);
	private static final ItemHolder SOE_TO_CAPTAIN_BATHIS = new ItemHolder(91651, 1);
	// Location
	private static final Location WHISPERING_WOODS = new Location(117678, 55348, -4560);
	// Misc
	private static final int MIN_LEVEL = 15;
	private static final int MAX_LEVEL = 20;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10956_WeSylphs()
	{
		super(10956);
		addStartNpc(KERKIN);
		addTalkId(KERKIN, EVIA);
		addKillId(VOLATU, PHOTORONI, ALES);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		addCondRace(Race.SYLPH, "no_race.html");
		setQuestNameNpcStringId(NpcStringId.LV_15_20_WE_SYLPHS);
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
			case "34210-01.html":
			case "34210-02.html":
			case "34210-04.html":
			case "34211-03.html":
			{
				htmltext = event;
				break;
			}
			case "34210-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(WHISPERING_WOODS);
				}
				break;
			}
			case "34211-02.html":
			{
				if (qs.isCond(2))
				{
					giveItems(player, SPIRIT_ORE);
					giveItems(player, SOE_NOVICE);
					giveItems(player, XP_GROWTH_SCROLL);
					giveItems(player, HP_POTION_REWARD);
					giveItems(player, SOE_TO_CAPTAIN_BATHIS);
					addExpAndSp(player, 600000, 13500);
					if (CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
					{
						showOnScreenMsg(player, NpcStringId.YOU_VE_FINISHED_THE_TUTORIAL_TAKE_YOUR_FIRST_CLASS_TRANSFER_AND_COMPLETE_YOUR_TRAINING_WITH_MATHORN_TO_BECOME_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
					}
					qs.exitQuest(false, true);
				}
				htmltext = event;
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
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "34210-01.html";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					final int killCount = qs.getInt(KILL_COUNT_VAR);
					if (killCount < 30)
					{
						htmltext = "34210-04.html";
					}
					htmltext = "34210-01.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "34211-01.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
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
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 30)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(2, true);
				qs.unset(KILL_COUNT_VAR);
				giveItems(killer, SOE_EVIA);
				showOnScreenMsg(killer, NpcStringId.THE_TRAINING_IN_OVER_USE_A_SCROLL_OF_ESCAPE_IN_YOUR_INVENTORY_TO_GO_BACK_TO_GROCER_EVIA, ExShowScreenMessage.TOP_CENTER, 10000);
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
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_WHISPERING_WOODS.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
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
