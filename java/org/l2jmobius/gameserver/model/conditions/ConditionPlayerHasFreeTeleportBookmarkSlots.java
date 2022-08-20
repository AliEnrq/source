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
package org.l2jmobius.gameserver.model.conditions;

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author Sdw
 */
public class ConditionPlayerHasFreeTeleportBookmarkSlots extends Condition
{
	private final int _teleportBookmarkSlots;
	
	public ConditionPlayerHasFreeTeleportBookmarkSlots(int teleportBookmarkSlots)
	{
		_teleportBookmarkSlots = teleportBookmarkSlots;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		final Player player = effector.getActingPlayer();
		if (player == null)
		{
			return false;
		}
		
		if ((player.getBookMarkSlot() + _teleportBookmarkSlots) > 18)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_REACHED_THE_MAXIMUM_NUMBER_OF_MY_TELEPORT_SLOTS_OR_USE_CONDITIONS_ARE_NOT_OBSERVED);
			return false;
		}
		
		return true;
	}
}
