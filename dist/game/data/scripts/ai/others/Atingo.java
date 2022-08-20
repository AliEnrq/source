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

import java.time.Duration;
import java.util.function.Consumer;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureHpChange;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jmobius.gameserver.model.skill.AbnormalVisualEffect;

import ai.AbstractNpcAI;

/**
 * Written by Berezkin Nikolay, on 15.05.2021 Hawk 25923, dragon 25922, kukabarra 25921, wolf 25918, tiger 25920, buffalo 25919
 */
public class Atingo extends AbstractNpcAI
{
	// NPCs
	private static final int ATINGO = 25914;
	private static final int SIN_EATER = 25924;
	private static final int[] PETS = new int[]
	{
		25923,
		25922,
		25921,
		25918,
		25920,
		25919
	};
	// Locations
	private static final Location[] SPAWNS =
	{
		new Location(83928, 94232, -3453, 41157), // Primeval Isle
		new Location(83928, 94232, -3453, 41157), // Plains of the Lizardmen
		new Location(113906, 14873, -3612, 49656), // Tower of Insolence
		new Location(171896, 20824, -3334, 16115), // Orc Barracks
	};
	// Misc
	private static final Duration ATINGO_RESPAWN_DURATION = Duration.ofMinutes(10);
	private static final double ATINGO_PET_SPAWN_RATE = 10;
	
	public Atingo()
	{
		addSpawnId(ATINGO);
		addKillId(ATINGO);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.addListener(new ConsumerEventListener(npc, EventType.ON_CREATURE_HP_CHANGE, (Consumer<OnCreatureHpChange>) this::onHpChange, this));
		return super.onSpawn(npc);
	}
	
	@Override
	protected void onLoad()
	{
		ThreadPool.schedule(() ->
		{
			if (World.getInstance().getVisibleObjects().stream().noneMatch(it -> it.getId() == ATINGO))
			{
				addSpawn(ATINGO, getRandomEntry(SPAWNS));
			}
		}, ATINGO_RESPAWN_DURATION.toMillis());
		
		super.onLoad();
	}
	
	private void onHpChange(OnCreatureHpChange hpChangeEvent)
	{
		final Npc creature = (Npc) hpChangeEvent.getCreature();
		if ((creature.getScriptValue() == 0) && !creature.isDead() && creature.isInCombat())
		{
			final Npc pet = addSpawn(getRandom(100) <= ATINGO_PET_SPAWN_RATE ? getRandomEntry(PETS) : SIN_EATER, GeoEngine.getInstance().getValidLocation(creature.getX(), creature.getY(), creature.getZ(), creature.getX() + 50, creature.getY() + 50, creature.getZ(), null));
			creature.setScriptValue(pet.getObjectId());
			pet.setInvul(true);
			pet.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.H_ULTIMATE_DEFENCE_B_AVE);
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final int petObjId = npc.getScriptValue();
		if (petObjId > 0)
		{
			final Npc pet = (Npc) World.getInstance().findObject(petObjId);
			if (pet != null)
			{
				pet.setInvul(false);
				pet.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.H_ULTIMATE_DEFENCE_B_AVE);
			}
		}
		
		ThreadPool.schedule(() ->
		{
			if (World.getInstance().getVisibleObjects().stream().noneMatch(it -> it.getId() == ATINGO))
			{
				addSpawn(ATINGO, getRandomEntry(SPAWNS));
			}
		}, ATINGO_RESPAWN_DURATION.toMillis());
		
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Atingo();
	}
}
