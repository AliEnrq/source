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

import org.l2jmobius.gameserver.enums.ElementalType;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author Mobius
 */
public class AddSpiritExp implements IItemHandler
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
		
		ElementalSpirit spirit = null;
		switch (item.getId())
		{
			case 91999:
			case 91035:
			{
				spirit = player.getElementalSpirit(ElementalType.WATER);
				break;
			}
			case 92000:
			case 91036:
			{
				spirit = player.getElementalSpirit(ElementalType.FIRE);
				break;
			}
			case 92001:
			case 91037:
			{
				spirit = player.getElementalSpirit(ElementalType.WIND);
				break;
			}
			case 92002:
			case 91038:
			{
				spirit = player.getElementalSpirit(ElementalType.EARTH);
				break;
			}
		}
		
		if ((spirit != null) && checkConditions(player, spirit))
		{
			player.destroyItem("AddSpiritExp item", item, 1, player, true);
			spirit.addExperience(9300);
			return true;
		}
		
		return false;
	}
	
	private boolean checkConditions(Player player, ElementalSpirit spirit)
	{
		if (player.isInBattle())
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_ABSORB_DURING_BATTLE);
			return false;
		}
		if ((spirit.getLevel() == spirit.getMaxLevel()) && (spirit.getExperience() == spirit.getExperienceToNextLevel()))
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_ABSORB_BECAUSE_REACHED_MAXIMUM_LEVEL);
			return false;
		}
		return true;
	}
}