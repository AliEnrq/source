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
package ai.others;

import org.l2jmobius.gameserver.model.actor.Npc;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class TrainingDummy extends AbstractNpcAI
{
	// NPCs
	private static final int[] TRAINING_DUMMIES =
	{
		22183,
		22324
	};
	
	private TrainingDummy()
	{
		addSpawnId(TRAINING_DUMMIES);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.disableCoreAI(true);
		npc.setImmobilized(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new TrainingDummy();
	}
}
