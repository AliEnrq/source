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

import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.HeavenlyRiftManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.AbstractScript;
import org.l2jmobius.gameserver.network.serverpackets.ExChangeNpcState;

import ai.AbstractNpcAI;

/**
 * @author Brutallis
 */
public class Bomb extends AbstractNpcAI
{
	// NPCs
	private static final int BOMB = 18003;
	private static final int DIVINE_ANGEL = 20139;
	// Items
	private static final int[] ITEM_DROP_1 = new int[]
	{
		49756,
		49762,
		49763
	};
	private static final int[] ITEM_DROP_2 = new int[]
	{
		49760,
		49761
	};
	
	public Bomb()
	{
		addKillId(BOMB);
		addSpawnId(BOMB);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.broadcastPacket(new ExChangeNpcState(npc.getObjectId(), 1));
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (getRandom(100) < 33)
		{
			AbstractScript.addSpawn(DIVINE_ANGEL, npc, false, 1800000);
		}
		else
		{
			World.getInstance().forEachVisibleObjectInRange((WorldObject) npc, Playable.class, 200, creature ->
			{
				if ((creature != null) && !creature.isDead())
				{
					creature.reduceCurrentHp(getRandom(300, 400), npc, null);
				}
			});
			if (getRandom(100) < 50)
			{
				if (getRandom(100) < 90)
				{
					npc.dropItem(killer.getActingPlayer(), ITEM_DROP_1[getRandom(ITEM_DROP_1.length)], 1);
				}
				else
				{
					npc.dropItem(killer.getActingPlayer(), ITEM_DROP_2[getRandom(ITEM_DROP_2.length)], 1);
				}
			}
		}
		if (HeavenlyRiftManager.getAliveNpcCount(npc.getId()) == 0)
		{
			GlobalVariablesManager.getInstance().set("heavenly_rift_complete", GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0));
			GlobalVariablesManager.getInstance().set("heavenly_rift_level", 0);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Bomb();
	}
}
