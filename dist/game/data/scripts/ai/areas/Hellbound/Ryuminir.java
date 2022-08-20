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
package ai.areas.Hellbound;

import java.time.Duration;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Ryuminir extends AbstractNpcAI
{
	// NPC
	private static final int RYUMINIR = 25936;
	// Locations
	private static final Location[] SPAWNS =
	{
		new Location(20970, 256781, -1350),
		new Location(21537, 251284, -1458),
	};
	// Misc
	private static final Duration RESPAWN_DELAY = Duration.ofMinutes(60);
	
	private Ryuminir()
	{
		addKillId(RYUMINIR);
		addSpawn(RYUMINIR, getRandomEntry(SPAWNS));
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		ThreadPool.schedule(() -> addSpawn(RYUMINIR, getRandomEntry(SPAWNS)), RESPAWN_DELAY.toMillis());
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Ryuminir();
	}
}