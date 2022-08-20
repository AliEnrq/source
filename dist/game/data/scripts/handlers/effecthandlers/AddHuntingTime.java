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
package handlers.effecthandlers;

import org.l2jmobius.gameserver.data.xml.TimedHuntingZoneData;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.holders.TimedHuntingZoneHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneEnter;
import org.l2jmobius.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneList;

/**
 * @author Mobius
 */
public class AddHuntingTime extends AbstractEffect
{
	private final int _zoneId;
	private final long _time;
	
	public AddHuntingTime(StatSet params)
	{
		_zoneId = params.getInt("zoneId", 0);
		_time = params.getLong("time", 3600000);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final Player player = effected.getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		final TimedHuntingZoneHolder holder = TimedHuntingZoneData.getInstance().getHuntingZone(_zoneId);
		if (holder == null)
		{
			return;
		}
		
		final long currentTime = System.currentTimeMillis();
		final long endTime = currentTime + player.getTimedHuntingZoneRemainingTime(_zoneId);
		if ((endTime > currentTime) && (((endTime - currentTime) + _time) >= holder.getMaximumAddedTime()))
		{
			player.getInventory().addItem("AddHuntingTime effect refund", item.getId(), 1, player, player);
			player.sendMessage("You cannot exceed the time zone limit.");
			return;
		}
		
		final long remainRefill = player.getVariables().getInt(PlayerVariables.HUNTING_ZONE_REMAIN_REFILL_ + _zoneId, holder.getRemainRefillTime());
		if ((_time < remainRefill) || (remainRefill == 0))
		{
			player.getInventory().addItem("AddHuntingTime effect refund", item.getId(), 1, player, player);
			player.sendMessage("You cannot exceed the time zone limit.");
			return;
		}
		
		final long remainTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_TIME + _zoneId, holder.getInitialTime());
		player.getVariables().set(PlayerVariables.HUNTING_ZONE_TIME + _zoneId, remainTime + _time);
		player.getVariables().set(PlayerVariables.HUNTING_ZONE_REMAIN_REFILL_ + _zoneId, remainRefill - (_time / 1000));
		
		if (player.isInTimedHuntingZone(_zoneId))
		{
			player.startTimedHuntingZone(_zoneId, endTime);
			player.sendPacket(new TimedHuntingZoneEnter(player, _zoneId));
		}
		
		player.sendPacket(new TimedHuntingZoneList(player));
	}
}
