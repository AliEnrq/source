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

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author Mode
 */
public class LimitedSayha implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = playable.getActingPlayer();
		long time = 0;
		switch (item.getId())
		{
			case 71899:
			{
				time = 86400000L * 30;
				break;
			}
			case 71900:
			{
				time = 86400000L * 1;
				break;
			}
			case 71901:
			{
				time = 86400000L * 7;
				break;
			}
			default:
			{
				time = 0;
				break;
			}
		}
		if ((time > 0) && player.setLimitedSayhaGraceEndTime(System.currentTimeMillis() + time))
		{
			player.destroyItem("LimitedSayha potion", item, 1, player, true);
		}
		else
		{
			player.sendMessage("Your Limited Sayha's Grace remaining time is greater than item's.");
			return false;
		}
		return true;
	}
}