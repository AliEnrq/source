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
package ai.areas.PlainsOfGlory;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Plains of Glory AI.
 * @author quangnguyen
 */
public class PlainsOfGlory extends AbstractNpcAI
{
	// Monsters
	private static final int VANOR_SILENOS = 20681;
	private static final int VANOR_SILENOS_SOLDIER = 20682;
	private static final int VANOR_SILENOS_SCOUT = 20683;
	private static final int VANOR_SILENOS_WARRIOR = 20684;
	private static final int VANOR_SILENOS_SHAMAN = 20685;
	private static final int VANOR_SILENOS_CHIEFTAIN = 20686;
	private static final int VANOR_MERCENARY_OF_GLORY = 24014;
	// Guard
	private static final int GUARD_OF_HONOR = 22102;
	
	private PlainsOfGlory()
	{
		addKillId(VANOR_SILENOS, VANOR_SILENOS_SOLDIER, VANOR_SILENOS_SCOUT, VANOR_SILENOS_WARRIOR, VANOR_SILENOS_SHAMAN, VANOR_SILENOS_CHIEFTAIN, VANOR_MERCENARY_OF_GLORY);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (getRandom(100) < 50)
		{
			final Npc spawnBanshee = addSpawn(GUARD_OF_HONOR, npc, false, 300000);
			final Playable attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
			addAttackPlayerDesire(spawnBanshee, attacker);
			npc.deleteMe();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new PlainsOfGlory();
	}
}