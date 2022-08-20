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
package ai.areas.OrcBarracks.Kerr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Kerr extends AbstractNpcAI
{
	// NPC
	private static final int KERR = 22140;
	// Locations
	private static final Location[] SPAWNS =
	{
		new Location(-89895, 108555, -3530),
		new Location(-88925, 112500, -3414),
		new Location(-92690, 112605, -3728),
		new Location(-95168, 110316, -3823),
		new Location(-95823, 114893, -3528),
		new Location(-93044, 117007, -3315),
		new Location(-96494, 119720, -3196),
		new Location(-96553, 106922, -3729),
		new Location(-93522, 105608, -3491),
		new Location(-96148, 102058, -3496),
		new Location(-93228, 100642, -3551),
		new Location(-91038, 102344, -3418),
		new Location(-89841, 100158, -3612),
		new Location(-88155, 103068, -3385),
	};
	// Misc
	private static final int SPAWN_COUNT = 3;
	private static final int RESPAWN_DELAY = 60000; // 1 minute.
	private static final Map<Npc, Location> KERR_SPAWN_LOCATIONS = new ConcurrentHashMap<>(SPAWN_COUNT);
	
	private Kerr()
	{
		addKillId(KERR);
		for (int i = 0; i < SPAWN_COUNT; i++)
		{
			spawnKerr();
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		ThreadPool.schedule(() ->
		{
			KERR_SPAWN_LOCATIONS.remove(npc);
			spawnKerr();
		}, RESPAWN_DELAY);
		
		return super.onKill(npc, killer, isSummon);
	}
	
	private void spawnKerr()
	{
		while (true)
		{
			final Location location = getRandomEntry(SPAWNS);
			if (KERR_SPAWN_LOCATIONS.containsValue(location))
			{
				continue;
			}
			
			KERR_SPAWN_LOCATIONS.put(addSpawn(KERR, location), location);
			break;
		}
	}
	
	public static void main(String[] args)
	{
		new Kerr();
	}
}