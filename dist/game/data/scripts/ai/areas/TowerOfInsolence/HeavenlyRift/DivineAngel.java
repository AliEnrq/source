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
package ai.areas.TowerOfInsolence.HeavenlyRift;

import org.l2jmobius.gameserver.ai.AttackableAI;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.HeavenlyRiftManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * @author Brutallis
 */
public class DivineAngel extends AbstractNpcAI
{
	// NPCs
	private static final int TOWER = 18004;
	private static final int DIVINE_ANGEL = 20139;
	
	public DivineAngel()
	{
		addSpawnId(DIVINE_ANGEL);
		addKillId(DIVINE_ANGEL);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if ((GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0) > 1) && (HeavenlyRiftManager.getAliveNpcCount(npc.getId()) == 0))
		{
			GlobalVariablesManager.getInstance().set("heavenly_rift_complete", GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0));
			GlobalVariablesManager.getInstance().set("heavenly_rift_level", 0);
			GlobalVariablesManager.getInstance().set("heavenly_rift_reward", 1);
			
			for (Creature creature : HeavenlyRiftManager.getZone().getCharactersInside())
			{
				if (creature.isMonster() && (creature.getId() == TOWER) && !creature.isDead())
				{
					((Npc) creature).broadcastSay(ChatType.NPC_SHOUT, NpcStringId.DIVINE_ANGELS_ARE_NOWHERE_TO_BE_SEEN_I_WANT_TO_TALK_TO_THE_PARTY_LEADER);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0) == 2)
		{
			((AttackableAI) npc.getAI()).setGlobalAggro(0);
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new DivineAngel();
	}
}
