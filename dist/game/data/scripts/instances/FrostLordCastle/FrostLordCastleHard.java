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
package instances.FrostLordCastle;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;

/**
 * @author Serenitty
 * @URL https://4r4m.com/l2e-ice-lord-castle-en/
 */
public class FrostLordCastleHard extends AbstractInstance
{
	// NPCs
	private static final int GLAKIAS = 29139; // GrandBoss
	private static final int GLAKIAS_TANK = 22348; // Minions tank
	private static final int GLAKIAS_ARCHER = 22352; // Minions archer
	private static final int GLAKIAS_MAGE = 22349; // Minions mage
	private static final int SODIAN = 29140; // Minions mage
	private static final int TRIDIAN = 29141; // Minions mage
	private static final int BODIAN = 29142; // Minions mage
	private static final int AKADIAN = 29143; // Minions mage
	// Skills
	private static final SkillHolder SUMMON_GLAKIAS_LVL2 = new SkillHolder(48373, 2); // When spawn Minion range attack
	private static final SkillHolder ICE_STORM_LV_2 = new SkillHolder(48381, 2);
	private static final SkillHolder ICE_CHAIN_LV_2 = new SkillHolder(48374, 2);
	private static final SkillHolder GLAKIAS_ENCHANCEMENT_LV_2 = new SkillHolder(48372, 2); // glakias up stats test only
	// Misc
	private static final int TEMPLATE_ID = 1014;
	
	public FrostLordCastleHard()
	{
		super(TEMPLATE_ID);
		addStartNpc(GLAKIAS);
		addKillId(GLAKIAS, GLAKIAS_TANK, GLAKIAS_ARCHER);
		addAttackId(GLAKIAS, GLAKIAS_TANK, GLAKIAS_ARCHER);
		addInstanceEnterId(TEMPLATE_ID);
		addInstanceLeaveId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "ENTER":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				final Instance world = player.getInstanceWorld();
				if (world != null)
				{
					world.broadcastPacket(new ExShowScreenMessage(NpcStringId.GLAKIAS_LV_85, 2, 7000));
					addSpawn(GLAKIAS, 114700, -114792, -11207, 49151, false, 0, true, player.getInstanceId());
				}
				break;
			}
			case "GLAKIAS_MINION":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					if (!world.getParameters().getBoolean("spawnedMinions", false))
					{
						world.getParameters().set("spawnedMinions", true);
						world.getNpc(GLAKIAS).doCast(SUMMON_GLAKIAS_LVL2.getSkill());
						
						addSpawn(AKADIAN, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_TANK, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_TANK, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_TANK, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(BODIAN, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_ARCHER, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_ARCHER, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(SODIAN, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_MAGE, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_MAGE, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(GLAKIAS_MAGE, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						addSpawn(TRIDIAN, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
						
						npc.broadcastSay(ChatType.NPC_SHOUT, "Guards! To arms!");
						world.broadcastPacket(new ExShowScreenMessage(NpcStringId.GUARDS_TO_ARMS, 2, 7000));
					}
					break;
				}
			}
			case "ICE_CHAIN":
			{
				if (SkillCaster.checkUseConditions(npc, ICE_CHAIN_LV_2.getSkill()))
				{
					npc.doCast(ICE_CHAIN_LV_2.getSkill());
				}
				break;
			}
			case "ICE_STORM":
			{
				if (SkillCaster.checkUseConditions(npc, ICE_STORM_LV_2.getSkill()))
				{
					npc.doCast(ICE_STORM_LV_2.getSkill());
				}
				break;
			}
			case "GLAKIAS_ENCHANCEMENT":
			{
				if (SkillCaster.checkUseConditions(npc, GLAKIAS_ENCHANCEMENT_LV_2.getSkill()))
				{
					npc.doCast(GLAKIAS_ENCHANCEMENT_LV_2.getSkill());
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		if (npc.isAttackable() && (npc.getId() == GLAKIAS))
		{
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.80))
			{
				startQuestTimer("ICE_STORM", 30000, npc, attacker);
			}
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.50))
			{
				startQuestTimer("GLAKIAS_MINION", 1000, world.getNpc(GLAKIAS), null);
			}
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.45))
			{
				startQuestTimer("ICE_CHAIN", 30000, npc, attacker);
			}
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.40))
			{
				startQuestTimer("ICE_STORM", 30000, npc, attacker);
			}
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.30))
			{
				startQuestTimer("ICE_CHAIN", 20000, npc, attacker);
			}
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.05))
			{
				startQuestTimer("GLAKIAS_ENCHANCEMENT", 30000, npc, attacker);
			}
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.04))
			{
				startQuestTimer("ICE_STORM", 1000, npc, attacker);
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		if (npc.getId() == GLAKIAS)
		{
			cancelQuestTimer("GLAKIAS_MINION", npc, player);
			world.finishInstance();
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new FrostLordCastleHard();
	}
}