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
package org.l2jmobius.gameserver.model.actor.tasks.player;

import org.l2jmobius.gameserver.model.actor.Player;

/**
 * Task dedicated to make damage to the player while drowning.
 * @author UnAfraid
 */
public class WaterTask implements Runnable
{
	private final Player _player;
	
	public WaterTask(Player player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (_player != null)
		{
			double reduceHp = _player.getMaxHp() / 100.0;
			if (reduceHp < 1)
			{
				reduceHp = 1;
			}
			
			_player.reduceCurrentHp(reduceHp, _player, null, false, true, false, false);
			_player.sendMessage("You have taken " + reduceHp + " damage because you were unable to breathe.");
		}
	}
}
