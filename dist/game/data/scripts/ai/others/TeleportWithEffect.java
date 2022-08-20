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

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureSkillFinishCast;
import org.l2jmobius.gameserver.model.skill.CommonSkill;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class TeleportWithEffect extends AbstractNpcAI
{
	public TeleportWithEffect()
	{
	}
	
	@RegisterEvent(EventType.ON_CREATURE_SKILL_FINISH_CAST)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnCreatureSkillFinishCast(OnCreatureSkillFinishCast event)
	{
		if (event.getSkill().getId() != CommonSkill.TELEPORT.getId())
		{
			return;
		}
		
		final Player player = event.getCaster().getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		final Location location = player.getTeleportLocation();
		if (location != null)
		{
			player.teleToLocation(location);
			player.setTeleportLocation(null);
		}
	}
	
	public static void main(String[] args)
	{
		new TeleportWithEffect();
	}
}
