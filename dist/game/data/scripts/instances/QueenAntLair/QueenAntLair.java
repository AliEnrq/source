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
package instances.QueenAntLair;

import java.util.List;

import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;

/**
 * @author Serenitty
 * @URL https://l2central.info/essence/events_and_promos/832.html
 */
public class QueenAntLair extends AbstractInstance
{
	// NPCs
	private static final int QUEEN = 18020;
	private static final int ESCORT = 18023;
	private static final int NURSE = 18022;
	private static final int JIO = 34185;
	// Skills
	private static final SkillHolder RAIN_OF_STONES_LV_1 = new SkillHolder(48254, 1); // When player in Radius target boss attack.
	private static final SkillHolder HEAL = new SkillHolder(4020, 1);
	// Misc
	private static final int TEMPLATE_ID = 217;
	
	public QueenAntLair()
	{
		super(TEMPLATE_ID);
		addKillId(QUEEN, ESCORT, NURSE);
		addAttackId(QUEEN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "ENTER":
			{
				if (player.isInParty())
				{
					final Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<Player> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
					for (Player member : members)
					{
						if (!member.isInsideRadius3D(npc, 1000))
						{
							player.sendMessage("Player " + member.getName() + " must go closer to Jio.");
						}
						enterInstance(member, npc, TEMPLATE_ID);
					}
				}
				else if (player.isGM())
				{
					enterInstance(player, npc, TEMPLATE_ID);
					player.sendMessage("SYS: You have entered as GM/Admin to Queen Ant's Lair.");
				}
				else
				{
					if (!player.isInsideRadius3D(npc, 1000))
					{
						player.sendMessage("You must go closer to Jio.");
					}
					enterInstance(player, npc, TEMPLATE_ID);
				}
				break;
			}
			case "SPAWN_MINION":
			{
				final Instance world = npc.getInstanceWorld();
				if (world == null)
				{
					return null;
				}
				
				if (!world.getParameters().getBoolean("spawnedMinions", false))
				{
					world.getParameters().set("spawnedMinions", true);
					
					final int stage = world.getParameters().getInt("stage", 0);
					world.getParameters().set("stage", stage + 1); // +1= -10% BOSS HP
					
					addSpawn(ESCORT, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(ESCORT, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(ESCORT, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId());
					world.setParameter("minion1", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion2", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion3", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion4", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion5", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion6", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					startQuestTimer("SUPPORT_QUEEN", 5000, npc, null);
				}
				break;
			}
			case "SUPPORT_QUEEN":
			{
				final Instance world = npc.getInstanceWorld();
				if (world == null)
				{
					return null;
				}
				
				final Npc nurse1 = world.getParameters().getObject("minion1", Npc.class);
				final Npc nurse2 = world.getParameters().getObject("minion2", Npc.class);
				final Npc nurse3 = world.getParameters().getObject("minion3", Npc.class);
				final Npc nurse4 = world.getParameters().getObject("minion4", Npc.class);
				final Npc nurse5 = world.getParameters().getObject("minion5", Npc.class);
				final Npc nurse6 = world.getParameters().getObject("minion6", Npc.class);
				if (!nurse1.isDead())
				{
					nurse1.setTarget(world.getNpc(QUEEN));
					nurse1.doCast(HEAL.getSkill());
				}
				if (!nurse2.isDead())
				{
					nurse2.setTarget(world.getNpc(QUEEN));
					nurse2.doCast(HEAL.getSkill());
				}
				if (!nurse3.isDead())
				{
					nurse3.setTarget(world.getNpc(QUEEN));
					nurse3.doCast(HEAL.getSkill());
				}
				if (!nurse4.isDead())
				{
					nurse4.setTarget(world.getNpc(QUEEN));
					nurse4.doCast(HEAL.getSkill());
				}
				if (!nurse5.isDead())
				{
					nurse5.setTarget(world.getNpc(QUEEN));
					nurse5.doCast(HEAL.getSkill());
				}
				if (!nurse6.isDead())
				{
					nurse6.setTarget(world.getNpc(QUEEN));
					nurse6.doCast(HEAL.getSkill());
				}
				
				startQuestTimer("SUPPORT_QUEEN", 5000, npc, null);
				break;
			}
			case "RAINOF_STONES":
			{
				if (SkillCaster.checkUseConditions(npc, RAIN_OF_STONES_LV_1.getSkill()))
				{
					npc.doCast(RAIN_OF_STONES_LV_1.getSkill());
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		if (npc.getId() == QUEEN)
		{
			if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.90)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.81)))
			{
				startQuestTimer("RAINOF_STONES", 10000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 0)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.80)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.71)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 1)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.70)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.61)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 2)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.60)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.51)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 3)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.50)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.41)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 7000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 4)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.40)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.31)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 6000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 5)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.20)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.09)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 4000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 6)
				{
					startQuestTimer("RAINOF_STONES", 2000, npc, null);
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		if (npc.getId() == QUEEN)
		{
			for (Npc spawn : world.getNpcs())
			{
				spawn.deleteMe();
			}
			
			cancelQuestTimer("SPAWN_MINION", npc, player);
			cancelQuestTimer("SUPPORT_QUEEN", npc, player);
			
			addSpawn(JIO, -22130, 182482, -5720, 49151, false, 0, true, player.getInstanceId());
			world.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_HAVE_SUCCEEDED_IN_DEFEATING_QUEEN_ANT_RECEIVE_YOUR_REWARD_FROM_JIO, 2, 9000));
			
			for (Player gamer : world.getPlayers())
			{
				gamer.sendPacket(new ExSendUIEvent(gamer, false, false, 0, 0, NpcStringId.TIME_LEFT));
			}
			world.finishInstance();
		}
		
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new QueenAntLair();
	}
}