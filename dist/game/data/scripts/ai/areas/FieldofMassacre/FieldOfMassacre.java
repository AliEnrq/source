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
package ai.areas.FieldofMassacre;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Field of Massacre AI.
 * @author quangnguyen
 */
public class FieldOfMassacre extends AbstractNpcAI
{
	// Monsters
	private static final int ACHER_OF_DESTRUCTION = 21001;
	private static final int GRAVEYARD_LICH = 21003;
	private static final int DISMAL_POLE = 21004;
	private static final int GRAVEYARD_PREDATOR = 21005;
	private static final int DOOM_KNIGHT = 20674;
	private static final int DOOM_SCOUT = 21002;
	private static final int DOOM_SERVANT = 21006;
	private static final int DOOM_GUARD = 21007;
	private static final int DOOM_ARCHER = 21008;
	private static final int DOOM_TROOPER = 21009;
	private static final int DOOM_WARRIOR = 21010;
	// Guard
	private static final int GUARD_BUTCHER = 22101;
	
	private FieldOfMassacre()
	{
		addKillId(ACHER_OF_DESTRUCTION, GRAVEYARD_LICH, DISMAL_POLE, GRAVEYARD_PREDATOR, DOOM_KNIGHT, DOOM_SCOUT, DOOM_SERVANT, DOOM_GUARD, DOOM_ARCHER, DOOM_TROOPER, DOOM_WARRIOR);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (getRandom(100) < 50)
		{
			final Npc spawnBanshee = addSpawn(GUARD_BUTCHER, npc, false, 300000);
			final Playable attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
			addAttackPlayerDesire(spawnBanshee, attacker);
			npc.deleteMe();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new FieldOfMassacre();
	}
}