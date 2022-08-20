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
package org.l2jmobius.gameserver.instancemanager;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Brutallis
 */
public class HeavenlyRiftManager
{
	protected static final ZoneType ZONE = ZoneManager.getInstance().getZoneByName("heavenly_rift");
	
	public static ZoneType getZone()
	{
		return ZONE;
	}
	
	public static int getAliveNpcCount(int npcId)
	{
		int result = 0;
		for (Creature creature : ZONE.getCharactersInside())
		{
			if (creature.isMonster() && !creature.isDead() && (creature.getId() == npcId))
			{
				result++;
			}
		}
		return result;
	}
	
	public static void startEvent20Bomb(Player player)
	{
		ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.SET_OFF_BOMBS_AND_GET_TREASURES, 2, 5000));
		spawnMonster(18003, 113352, 12936, 10976, 1800000);
		spawnMonster(18003, 113592, 13272, 10976, 1800000);
		spawnMonster(18003, 113816, 13592, 10976, 1800000);
		spawnMonster(18003, 113080, 13192, 10976, 1800000);
		spawnMonster(18003, 113336, 13528, 10976, 1800000);
		spawnMonster(18003, 113560, 13832, 10976, 1800000);
		spawnMonster(18003, 112776, 13512, 10976, 1800000);
		spawnMonster(18003, 113064, 13784, 10976, 1800000);
		spawnMonster(18003, 112440, 13848, 10976, 1800000);
		spawnMonster(18003, 112728, 14104, 10976, 1800000);
		spawnMonster(18003, 112760, 14600, 10976, 1800000);
		spawnMonster(18003, 112392, 14456, 10976, 1800000);
		spawnMonster(18003, 112104, 14184, 10976, 1800000);
		spawnMonster(18003, 111816, 14488, 10976, 1800000);
		spawnMonster(18003, 112104, 14760, 10976, 1800000);
		spawnMonster(18003, 112392, 15032, 10976, 1800000);
		spawnMonster(18003, 112120, 15288, 10976, 1800000);
		spawnMonster(18003, 111784, 15064, 10976, 1800000);
		spawnMonster(18003, 111480, 14824, 10976, 1800000);
		spawnMonster(18003, 113144, 14216, 10976, 1800000);
	}
	
	public static void startEventTower(Player player)
	{
		ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.PROTECT_THE_CENTRAL_TOWER_FROM_DIVINE_ANGELS, 2, 5000));
		spawnMonster(18004, 112648, 14072, 10976, 1800000);
		ThreadPool.schedule(() ->
		{
			for (int i = 0; i < 20; ++i)
			{
				spawnMonster(20139, 112696, 13960, 10958, 1800000);
			}
		}, 10000);
	}
	
	public static void startEvent40Angels(Player player)
	{
		ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.DESTROY_WEAKENED_DIVINE_ANGELS, 2, 5000));
		for (int i = 0; i < 40; ++i)
		{
			spawnMonster(20139, 112696, 13960, 10958, 1800000);
		}
	}
	
	private static void spawnMonster(int npcId, int x, int y, int z, long despawnTime)
	{
		try
		{
			Spawn spawn = new Spawn(npcId);
			Location location = new Location(x, y, z);
			spawn.setLocation(location);
			Npc npc = spawn.doSpawn();
			npc.scheduleDespawn(despawnTime);
		}
		catch (Exception e)
		{
		}
	}
	
	public static class ClearZoneTask implements Runnable
	{
		private final Npc _npc;
		
		public ClearZoneTask(Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			for (Creature creature : ZONE.getCharactersInside())
			{
				if (creature.isPlayer())
				{
					creature.teleToLocation(114264, 13352, -5104);
				}
				else if (creature.isNpc() && (creature.getId() != 30401))
				{
					creature.decayMe();
				}
			}
			_npc.setBusy(false);
		}
	}
}
