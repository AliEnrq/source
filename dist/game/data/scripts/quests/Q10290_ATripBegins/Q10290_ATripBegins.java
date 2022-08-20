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
package quests.Q10290_ATripBegins;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;

/**
 * A Trip Begins (10966)
 * @author Mobius
 */
public class Q10290_ATripBegins extends Quest
{
	// NPCs
	private static final int CAPTAIN_BATHIS = 30332;
	private static final int MATHORN = 34139;
	private static final int BELLA = 30256;
	private static final int EVIA = 34211;
	// Items
	private static final ItemHolder SOE_TO_CAPTAIN_BATHIS = new ItemHolder(91651, 1);
	private static final ItemHolder SOE_TO_RUIN_OF_AGONY = new ItemHolder(91727, 1);
	private static final ItemHolder BSOE_EVENT = new ItemHolder(91689, 10);
	private static final ItemHolder ADVENTURERS_TALISMAN = new ItemHolder(91937, 1);
	private static final ItemHolder SCROLL_OF_ENCHANT_ADVENTURERS_TALISMAN = new ItemHolder(95688, 1);
	private static final ItemHolder ADVENTURERS_BRACELET = new ItemHolder(91934, 1);
	private static final ItemHolder SCROLL_OF_ENCHANT_ADEN_WEAPON = new ItemHolder(93038, 2);
	// Monsters
	private static final int ARACHNID_PREDATOR = 20926;
	private static final int SKELETON_BOWMAN = 20051;
	private static final int RUIN_SPARTOI = 20054;
	private static final int RAGING_SPARTOI = 20060;
	private static final int TUMRAN_BUGBEAR = 20062;
	private static final int TUMRAN_BUGBEAR_WARRIOR = 20064;
	// Location
	private static final Location TELEPORT_GLUDIO = new Location(-14489, 123974, -3128);
	// Misc
	private static final int MIN_LEVEL = 20;
	private static final int MAX_LEVEL = 25;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10290_ATripBegins()
	{
		super(10290);
		addStartNpc(CAPTAIN_BATHIS, MATHORN, EVIA);
		addTalkId(CAPTAIN_BATHIS, MATHORN, EVIA, BELLA);
		addKillId(ARACHNID_PREDATOR, SKELETON_BOWMAN, RUIN_SPARTOI, RAGING_SPARTOI, RAGING_SPARTOI, TUMRAN_BUGBEAR, TUMRAN_BUGBEAR_WARRIOR);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_20_25_A_TRIP_BEGINS);
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
			case "34139-02.html":
			case "34139-04.html":
			case "34139-05.html":
			case "34211-02.html":
			case "34211-04.html":
			case "34211-05.html":
			case "AutomaticHunting.html":
			case "AutomaticHunting-01.html":
			case "AutomaticHunting-02.html":
			case "AutomaticHunting-03.html":
			case "30256-01.html":
			case "30256-02.html":
			case "30256-04.html":
			case "30332-01.html":
			case "30332.htm":
			case "30332-01.htm":
			case "30332-02.htm":
			{
				htmltext = event;
				break;
			}
			case "34139-01.html":
			case "34211-01.html":
			{
				showOnScreenMsg(player, NpcStringId.CHECK_YOUR_INVENTORY_AND_EQUIP_YOUR_WEAPON, ExShowScreenMessage.TOP_CENTER, 10000, player.getName());
				htmltext = event;
				break;
			}
			case "AutomaticHuntingSkip":
			{
				if (player.isDeathKnight())
				{
					htmltext = "34139-03.html";
				}
				else
				{
					htmltext = "34211-03.html";
				}
				qs.startQuest();
				npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, NpcStringId.USING_THE_GATEKEEPER));
				break;
			}
			case "30332-03.htm":
			{
				qs.startQuest();
				npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, NpcStringId.USING_THE_GATEKEEPER));
				htmltext = event;
				break;
			}
			case "30256-03.html":
			{
				qs.setCond(2, true);
				showOnScreenMsg(player, NpcStringId.BEFORE_YOU_GO_FOR_A_BATTLE_CHECK_THE_SKILL_WINDOW_ALT_K_NEW_SKILLS_WILL_HELP_YOU_TO_GET_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000, player.getName());
				giveItems(player, SOE_TO_RUIN_OF_AGONY);
				htmltext = event;
				break;
			}
			case "30332-02.html":
			{
				if (qs.isCond(3))
				{
					showOnScreenMsg(player, NpcStringId.YOU_VE_GOT_ADVENTURER_S_BRACELET_AND_ADVENTURER_S_TALISMAN_COMPLETE_THE_TUTORIAL_AND_TRY_TO_USE_THE_TALISMAN, ExShowScreenMessage.TOP_CENTER, 10000);
					addExpAndSp(player, (ExperienceData.getInstance().getExpForLevel(25) + 100) - player.getExp(), 42000);
					// TODO: find a better way to do this: Tempfix for not giving items when already have them in inventory (bugging abort and re-accepting).
					if (player.getInventory().getAllItemsByItemId(BSOE_EVENT.getId()).size() <= 20)
					{
						// 20 due other quest rewards? Need to see for a possible to add a variable here.
						giveItems(player, BSOE_EVENT);
					}
					if (player.getInventory().getAllItemsByItemId(ADVENTURERS_BRACELET.getId()).isEmpty())
					{
						giveItems(player, ADVENTURERS_BRACELET);
					}
					if (player.getInventory().getAllItemsByItemId(ADVENTURERS_TALISMAN.getId()).isEmpty())
					{
						giveItems(player, ADVENTURERS_TALISMAN);
					}
					if (player.getInventory().getAllItemsByItemId(SCROLL_OF_ENCHANT_ADVENTURERS_TALISMAN.getId()).isEmpty())
					{
						giveItems(player, SCROLL_OF_ENCHANT_ADVENTURERS_TALISMAN);
					}
					if (player.getInventory().getAllItemsByItemId(SCROLL_OF_ENCHANT_ADEN_WEAPON.getId()).isEmpty())
					{
						giveItems(player, SCROLL_OF_ENCHANT_ADEN_WEAPON);
					}
					qs.exitQuest(false, true);
					htmltext = "30332-03.html";
				}
				break;
			}
			case "TELEPORT_TO_GLUDIO":
			{
				if (qs.isCond(1))
				{
					player.teleToLocation(TELEPORT_GLUDIO);
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
			switch (npc.getId())
			{
				case MATHORN:
				{
					htmltext = "34139-01.html";
					break;
				}
				case EVIA:
				{
					htmltext = "34211-01.html";
					break;
				}
				case BELLA:
				{
					htmltext = "30256-01.html";
					break;
				}
				case CAPTAIN_BATHIS:
				{
					htmltext = "30332.htm";
					break;
				}
			}
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case MATHORN:
				{
					if (qs.isCond(1))
					{
						htmltext = "34139-05.html";
					}
					break;
				}
				case EVIA:
				{
					if (qs.isCond(1))
					{
						htmltext = "34211-05.html";
					}
					break;
				}
				case BELLA:
				{
					if (qs.isCond(1))
					{
						htmltext = "30256-01.html";
					}
					else if (qs.isCond(2))
					{
						htmltext = "30256-05.html";
					}
					break;
				}
				case CAPTAIN_BATHIS:
				{
					if (qs.isCond(1))
					{
						htmltext = "30332.htm";
					}
					else if (qs.isCond(2))
					{
						htmltext = "30332-04.htm";
					}
					else if (qs.isCond(3))
					{
						htmltext = "30332-01.html";
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
			if (killCount < 40)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				qs.setCond(3, true);
				qs.unset(KILL_COUNT_VAR);
				showOnScreenMsg(killer, NpcStringId.YOU_VE_KILLED_ALL_THE_MONSTERS_USE_THE_SCROLL_OF_ESCAPE_IN_YOUR_INVENTORY_TO_RETURN_TO_CAPTAIN_BATHIS_IN_GLUDIO, ExShowScreenMessage.TOP_CENTER, 10000, killer.getName());
				giveItems(killer, SOE_TO_CAPTAIN_BATHIS);
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
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_IN_THE_RUINS_OF_AGONY.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}