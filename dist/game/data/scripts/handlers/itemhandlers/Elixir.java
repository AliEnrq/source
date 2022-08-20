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
package handlers.itemhandlers;

import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Elixir extends ItemSkills
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final int elixirsAvailable = playable.getActingPlayer().getVariables().getInt(PlayerVariables.ELIXIRS_AVAILABLE, 0);
		final int elixirsUsed = playable.getActingPlayer().getVariables().getInt(PlayerVariables.ELIXIRS_USED, 0) + elixirsAvailable;
		if ((playable.getLevel() < 76) || //
			((playable.getLevel() < 88) && (elixirsUsed >= 5)) || //
			((playable.getLevel() < 91) && (elixirsUsed >= 10)) || //
			((playable.getLevel() < 92) && (elixirsUsed >= 11)) || //
			((playable.getLevel() < 93) && (elixirsUsed >= 12)) || //
			((playable.getLevel() < 94) && (elixirsUsed >= 13)) || //
			((playable.getLevel() < 95) && (elixirsUsed >= 14)) || //
			((playable.getLevel() < 96) && (elixirsUsed >= 15)))
		{
			playable.sendPacket(SystemMessageId.THE_ELIXIR_UNAVAILABLE);
			return false;
		}
		
		playable.getActingPlayer().getVariables().set(PlayerVariables.ELIXIRS_AVAILABLE, elixirsAvailable + 1);
		playable.sendPacket(new SystemMessage(SystemMessageId.THANKS_TO_THE_ELIXIR_CHARACTER_S_STAT_POINTS_S1).addInt(1));
		playable.getActingPlayer().broadcastUserInfo();
		return super.useItem(playable, item, forceUse);
	}
}
