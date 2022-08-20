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
package ai.areas.FrostCastleZone;

import java.time.Duration;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Tiron extends AbstractNpcAI
{
	// NPC
	private static final int TIRON = 29135;
	private static final int CHARGED_CRYSTAL = 34232;
	// Locations
	private static final Location[] SPAWNS =
	{
		new Location(148623, 142688, -12198),
		new Location(150075, 144636, -12209),
	};
	// Misc
	private static final Duration RESPAWN_DELAY = Duration.ofMinutes(120);
	
	private Tiron()
	{
		addKillId(TIRON);
		addSpawn(TIRON, getRandomEntry(SPAWNS));
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		addSpawn(CHARGED_CRYSTAL, 149217, 143818, -12206, 49151, false, 0, true, killer.getInstanceId());
		ThreadPool.schedule(() -> addSpawn(TIRON, getRandomEntry(SPAWNS)), RESPAWN_DELAY.toMillis());
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Tiron();
	}
}