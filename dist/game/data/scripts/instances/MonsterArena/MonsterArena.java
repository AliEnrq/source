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
package instances.MonsterArena;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerClanLeft;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;

import instances.AbstractInstance;

/**
 * @author Mobius
 * @URL https://www.l2central.info/essence/articles/793.html?lang=en
 */
public class MonsterArena extends AbstractInstance
{
	// NPCs
	private static final int KOLDRUT_JAY = 34169;
	private static final int MACHINE = 30203;
	private static final int SUPPLIES = 30204;
	private static final int[] BOSSES =
	{
		25794, // Kutis
		25795, // Garan
		25796, // Batur
		25797, // Venir
		25798, // Oel
		25799, // Taranka
		25800, // Kasha
		25801, // Dorak
		25802, // Turan
		25803, // Varkan
		25804, // Ketran
		25805, // Death Lord Likan
		25806, // Anbarad
		25807, // Baranos
		25808, // Takuran
		25809, // Nast
		25810, // Keltar
		25811, // Satur
		25812, // Kosnak
		25813, // Garaki
		// TODO: 21-25 bosses
		// 25834, // Shadai
		// 25835, // Tyrobait
		// 25836, // Tier
		// 25837, // Cherkia
		// 25838, // Spicula
	};
	// Rewards
	private static final int BATTLE_BOX_1 = 90913;
	private static final int BATTLE_BOX_2 = 90913;
	private static final int BATTLE_BOX_3 = 90914;
	private static final int BATTLE_BOX_4 = 90914;
	private static final int VALOR_BOX = 90915;
	private static final int TICKET_M = 90946;
	private static final int TICKET_H = 90947;
	// Skill
	private static final int CLAN_EXUBERANCE = 1867;
	// Misc
	private static final Collection<Player> REWARDED_PLAYERS = ConcurrentHashMap.newKeySet();
	private static final int TEMPLATE_ID = 192;
	
	public MonsterArena()
	{
		super(TEMPLATE_ID);
		addStartNpc(KOLDRUT_JAY, MACHINE, SUPPLIES);
		addFirstTalkId(KOLDRUT_JAY, MACHINE, SUPPLIES);
		addTalkId(KOLDRUT_JAY, MACHINE, SUPPLIES);
		addKillId(BOSSES);
		addInstanceLeaveId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "34169.htm":
			case "34169-01.htm":
			case "34169-02.htm":
			case "34169-03.htm":
			case "34169-04.htm":
			case "30202-01.htm":
			case "30202-02.htm":
			case "30202-03.htm":
			case "30203-01.htm":
			{
				return event;
			}
			case "enter_monster_arena":
			{
				// If you died, you may return to the arena.
				if ((player.getClan() != null) && (player.getCommandChannel() != null))
				{
					for (Player member : player.getCommandChannel().getMembers())
					{
						final Instance world = member.getInstanceWorld();
						if ((world != null) && (world.getTemplateId() == TEMPLATE_ID) && (world.getPlayersCount() < 40) && (player.getClanId() == member.getClanId()))
						{
							player.teleToLocation(world.getNpc(MACHINE), true, world);
							if ((world.getStatus() > 0) && (world.getStatus() < 5)) // Show remaining countdown.
							{
								player.sendPacket(new ExSendUIEvent(player, false, false, (int) (world.getRemainingTime() / 1000), 0, NpcStringId.TIME_LEFT));
							}
							return null;
						}
					}
				}
				
				// Clan checks.
				if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId()) || (player.getCommandChannel() == null))
				{
					return "30202-03.htm";
				}
				if (player.getClan().getLevel() < 3)
				{
					player.sendMessage("Your clan must be at least level 3.");
					return null;
				}
				for (Player member : player.getCommandChannel().getMembers())
				{
					if ((member.getClan() == null) || (member.getClanId() != player.getClanId()))
					{
						player.sendMessage("Your command channel must be consisted only by clan members.");
						return null;
					}
				}
				
				enterInstance(player, npc, TEMPLATE_ID);
				
				final Instance world = player.getInstanceWorld();
				if (world != null)
				{
					final Npc machine = world.getNpc(MACHINE);
					machine.setScriptValue(player.getClanId());
					
					// Initialize progress if it does not exist.
					if (GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + machine.getScriptValue(), -1) == -1)
					{
						GlobalVariablesManager.getInstance().set(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + machine.getScriptValue(), 1);
					}
					
					// On max progress, set last four bosses.
					final int progress = GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + machine.getScriptValue());
					if (progress > 17) // TODO: 22 for 25 total bosses.
					{
						GlobalVariablesManager.getInstance().set(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + machine.getScriptValue(), 17); // TODO: 22 for 25 total bosses.
					}
					
					startQuestTimer("machine_talk", 10000, machine, null);
					startQuestTimer("start_countdown", 60000, machine, null);
					startQuestTimer("next_spawn", 60000, machine, null);
				}
				break;
			}
			case "machine_talk":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WELCOME_TO_THE_ARENA_TEST_YOUR_CLAN_S_STRENGTH);
				}
				break;
			}
			case "start_countdown":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					world.setStatus(1);
					for (Player plr : world.getPlayers())
					{
						plr.sendPacket(new ExSendUIEvent(plr, false, false, 1800, 0, NpcStringId.TIME_LEFT));
					}
				}
				break;
			}
			case "next_spawn":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					world.spawnGroup("boss_" + GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + npc.getScriptValue()));
				}
				break;
			}
			case "supply_reward":
			{
				final Instance world = npc.getInstanceWorld();
				if ((world != null) && (npc.getId() == SUPPLIES) && (player.getLevel() > 39) && !REWARDED_PLAYERS.contains(player) && npc.isScriptValue(0))
				{
					npc.setScriptValue(1);
					npc.doDie(npc);
					REWARDED_PLAYERS.add(player);
					ThreadPool.schedule(() ->
					{
						REWARDED_PLAYERS.remove(player);
					}, 60000);
					
					// Mandatory reward.
					final Npc machine = world.getNpc(MACHINE);
					final int progress = GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + machine.getScriptValue());
					if (progress > 16)
					{
						giveItems(player, BATTLE_BOX_4, 1);
					}
					else if (progress > 11)
					{
						giveItems(player, BATTLE_BOX_3, 1);
					}
					else if (progress > 6)
					{
						giveItems(player, BATTLE_BOX_2, 1);
					}
					else
					{
						giveItems(player, BATTLE_BOX_1, 1);
					}
					
					// Rare reward.
					if (getRandom(100) < 1) // 1% chance.
					{
						giveItems(player, VALOR_BOX, 1);
					}
					else if (getRandom(100) < 1) // 1% chance.
					{
						giveItems(player, TICKET_M, 1);
					}
					else if (getRandom(100) < 1) // 1% chance.
					{
						giveItems(player, TICKET_H, 1);
					}
				}
				break;
			}
			case "remove_supplies":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					for (Npc aliveNpc : world.getAliveNpcs())
					{
						if ((aliveNpc != null) && (aliveNpc.getId() == SUPPLIES))
						{
							aliveNpc.deleteMe();
						}
					}
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public void onInstanceLeave(Player player, Instance instance)
	{
		player.sendPacket(new ExSendUIEvent(player, false, false, 0, 0, NpcStringId.TIME_LEFT));
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			// Change world status.
			world.incStatus();
			
			// Make machine talk.
			final Npc machine = world.getNpc(MACHINE);
			machine.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.HA_NOT_BAD);
			
			// Save progress to global variables.
			GlobalVariablesManager.getInstance().increaseInt(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + machine.getScriptValue(), 1);
			
			// Spawn reward chests.
			world.spawnGroup("supplies");
			startQuestTimer("remove_supplies", 60000, machine, null);
			
			// Next boss spawn.
			if (world.getStatus() < 5)
			{
				startQuestTimer("next_spawn", 60000, machine, null);
			}
			else // Finish.
			{
				for (Player plr : world.getPlayers())
				{
					plr.sendPacket(new ExSendUIEvent(plr, false, false, 0, 0, NpcStringId.TIME_LEFT));
				}
				world.finishInstance();
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + "-01.htm";
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
		
		final Clan clan = player.getClan();
		if (clan == null)
		{
			// Should never happen.
			final Skill knownSkill = player.getKnownSkill(CLAN_EXUBERANCE);
			if (knownSkill != null)
			{
				player.removeSkill(knownSkill, true);
			}
			return;
		}
		
		final int stage = GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + clan.getId(), 0);
		if (stage > 4)
		{
			player.addSkill(SkillData.getInstance().getSkill(CLAN_EXUBERANCE, stage / 5), false);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_CLAN_LEFT)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerClanLeft(OnPlayerClanLeft event)
	{
		final ClanMember member = event.getClanMember();
		if ((member == null) || !member.isOnline())
		{
			return;
		}
		
		member.getPlayer().removeSkill(CLAN_EXUBERANCE, true);
	}
	
	public static void main(String[] args)
	{
		new MonsterArena();
	}
}
