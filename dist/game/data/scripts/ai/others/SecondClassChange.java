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

import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

import ai.AbstractNpcAI;

/**
 * Second class change has only level requirement.
 * @author Mobius
 */
public class SecondClassChange extends AbstractNpcAI
{
	private static final int LEVEL_REQUIREMENT = 40;
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((player.getLevel() < LEVEL_REQUIREMENT) || !CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((player.getLevel() < LEVEL_REQUIREMENT) || !CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
	}
	
	public static void main(String[] args)
	{
		new SecondClassChange();
	}
}
