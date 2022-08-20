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
package org.l2jmobius.gameserver.network.clientpackets.elementalspirits;

import java.util.stream.Collectors;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.ElementalType;
import org.l2jmobius.gameserver.enums.InventoryBlockType;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.enums.UserInfoType;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.elementalspirits.ElementalSpiritEvolution;

/**
 * @author JoeAlisson
 */
public class ExElementalSpiritEvolution implements IClientIncomingPacket
{
	private byte _type;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_type = (byte) packet.readC();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final ElementalSpirit spirit = player.getElementalSpirit(ElementalType.of(_type));
		if (spirit == null)
		{
			client.sendPacket(SystemMessageId.NO_SPIRITS_ARE_AVAILABLE);
			return;
		}
		
		final boolean canEvolve = checkConditions(player, spirit);
		if (canEvolve)
		{
			spirit.upgrade();
			client.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_EVOLVED_TO_LV_S2).addElementalSpirit(_type).addInt(spirit.getStage()));
			final UserInfo userInfo = new UserInfo(player);
			userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
			client.sendPacket(userInfo);
		}
		client.sendPacket(new ElementalSpiritEvolution(player, _type, canEvolve));
	}
	
	private boolean checkConditions(Player player, ElementalSpirit spirit)
	{
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.CANNOT_EVOLVE_ABSORB_EXTRACT_WHILE_USING_THE_PRIVATE_STORE_WORKSHOP);
			return false;
		}
		if (player.isInBattle())
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_EVOLVE_DURING_BATTLE);
			return false;
		}
		if (!spirit.canEvolve())
		{
			player.sendPacket(SystemMessageId.THIS_SPIRIT_CANNOT_EVOLVE);
			return false;
		}
		if (!consumeEvolveItems(player, spirit))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_INGREDIENTS_FOR_EVOLUTION);
			return false;
		}
		return true;
	}
	
	private boolean consumeEvolveItems(Player player, ElementalSpirit spirit)
	{
		final PlayerInventory inventory = player.getInventory();
		try
		{
			inventory.setInventoryBlock(spirit.getItemsToEvolve().stream().map(ItemHolder::getId).collect(Collectors.toList()), InventoryBlockType.BLACKLIST);
			for (ItemHolder itemHolder : spirit.getItemsToEvolve())
			{
				if (inventory.getInventoryItemCount(itemHolder.getId(), -1) < itemHolder.getCount())
				{
					return false;
				}
			}
			
			for (ItemHolder itemHolder : spirit.getItemsToEvolve())
			{
				player.destroyItemByItemId("Evolve", itemHolder.getId(), itemHolder.getCount(), player, true);
			}
			return true;
		}
		finally
		{
			inventory.unblock();
		}
	}
}
