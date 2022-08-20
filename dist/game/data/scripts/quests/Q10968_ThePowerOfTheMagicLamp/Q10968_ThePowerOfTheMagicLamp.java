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
package quests.Q10968_ThePowerOfTheMagicLamp;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.npc.OnAttackableKill;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

/**
 * @author quangnguyen, Mobius
 */
public class Q10968_ThePowerOfTheMagicLamp extends Quest
{
	// NPC
	private static final int MAXIMILLIAN = 30120;
	// Items
	// private static final int BLUE_LANTERN = 93074;
	private static final ItemHolder MAGIC_FIRE = new ItemHolder(92033, 1);
	// Misc
	private static final int MIN_LEVEL = 35;
	
	public Q10968_ThePowerOfTheMagicLamp()
	{
		super(10968);
		addStartNpc(MAXIMILLIAN);
		addTalkId(MAXIMILLIAN);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_40_THE_MAGIC_LANTERN_POWER);
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
			case "30120.htm":
			case "30120-01.htm":
			case "30120-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30120-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30120-05.html":
			{
				if (player.getLampCount() >= 0)
				{
					giveItems(player, MAGIC_FIRE);
					qs.exitQuest(false, true);
					break;
				}
				htmltext = "no-refill.html";
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
			htmltext = "30120.htm";
		}
		else if (qs.isStarted())
		{
			htmltext = "30120-04.html";
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_ATTACKABLE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL_MONSTERS)
	public void onAttackableKill(OnAttackableKill event)
	{
		final Player player = event.getAttacker();
		if (player == null)
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if ((qs == null) || !qs.isCond(1))
		{
			return;
		}
		
		if (player.getLampExp() >= Config.MAGIC_LAMP_MAX_LEVEL_EXP)
		{
			qs.setCond(2, true);
		}
	}
}
