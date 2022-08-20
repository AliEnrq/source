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
package quests.Q10957_TheLifeOfADeathKnight;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Mobius
 */
public class Q10957_TheLifeOfADeathKnight extends Quest
{
	// NPCs
	private static final int KILREMANGE = 34138;
	private static final int TRAINING_DUMMY = 22183;
	// Items
	private static final ItemHolder SOULSHOT_REWARD = new ItemHolder(91927, 200);
	private static final ItemHolder SOE_REWARD = new ItemHolder(10650, 5);
	private static final ItemHolder WW_POTION_REWARD = new ItemHolder(49036, 5);
	private static final ItemHolder HP_POTION_REWARD = new ItemHolder(91912, 50);
	// Skill
	private static final SkillHolder DK_TRANSORMATION = new SkillHolder(48057, 1);
	// Misc
	private static final String REWARD_CHECK_VAR1 = "Q10957_REWARD_1";
	private static final String REWARD_CHECK_VAR2 = "Q10957_REWARD_2";
	
	public Q10957_TheLifeOfADeathKnight()
	{
		super(10957);
		addStartNpc(KILREMANGE);
		addTalkId(KILREMANGE);
		addKillId(TRAINING_DUMMY);
		setQuestNameNpcStringId(NpcStringId.LV_1_2_THE_LIFE_OF_A_DEATH_KNIGHT);
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
			case "34138-02.htm":
			case "34138-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34138-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34138-07.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3);
					if (!player.getVariables().getBoolean(REWARD_CHECK_VAR1, false))
					{
						player.getVariables().set(REWARD_CHECK_VAR1, true);
						giveItems(player, SOULSHOT_REWARD);
					}
				}
				htmltext = event;
				break;
			}
			case "34138-10.html":
			{
				if (!player.getVariables().getBoolean(REWARD_CHECK_VAR2, false))
				{
					player.getVariables().set(REWARD_CHECK_VAR2, true);
					giveItems(player, SOE_REWARD);
					giveItems(player, WW_POTION_REWARD);
					giveItems(player, HP_POTION_REWARD);
					giveItems(player, SOULSHOT_REWARD);
				}
				
				giveStoryBuffReward(npc, player);
				SkillCaster.triggerCast(player, player, DK_TRANSORMATION.getSkill());
				
				qs.exitQuest(false, true);
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
		
		// Death Knights.
		if (!player.isDeathKnight())
		{
			return htmltext;
		}
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "34138-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "34138-05.html";
						break;
					}
					case 2:
					{
						htmltext = "34138-06.html";
						break;
					}
					case 3:
					{
						htmltext = "34138-08.html";
						break;
					}
					case 4:
					{
						htmltext = "34138-09.html";
						break;
					}
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
		if (qs != null)
		{
			if (qs.isCond(1))
			{
				qs.setCond(2, true);
			}
			else if (qs.isCond(3))
			{
				qs.setCond(4, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Death Knights.
		if (!player.isDeathKnight())
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if ((qs == null) || (player.getLevel() < 20))
		{
			showOnScreenMsg(player, NpcStringId.SPEAK_TO_HEAD_TRAINER_KILREMANGE, ExShowScreenMessage.TOP_CENTER, 10000, player.getName());
			return;
		}
	}
}
