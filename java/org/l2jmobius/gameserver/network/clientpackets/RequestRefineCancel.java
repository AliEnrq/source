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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.VariationData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExVariationCancelResult;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.util.Util;

/**
 * Format(ch) d
 * @author -Wooden-
 */
public class RequestRefineCancel implements IClientIncomingPacket
{
	private int _targetItemObjId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetItemObjId = packet.readD();
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
		
		final Item targetItem = player.getInventory().getItemByObjectId(_targetItemObjId);
		if (targetItem == null)
		{
			player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
			return;
		}
		
		if (targetItem.getOwnerId() != player.getObjectId())
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tryied to augment item that doesn't own.", Config.DEFAULT_PUNISH);
			return;
		}
		
		// cannot remove augmentation from a not augmented item
		if (!targetItem.isAugmented())
		{
			player.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
			return;
		}
		
		// get the price
		final long price = VariationData.getInstance().getCancelFee(targetItem.getId(), targetItem.getAugmentation().getMineralId());
		if (price < 0)
		{
			player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
			return;
		}
		
		// try to reduce the players adena
		if (!player.reduceAdena("RequestRefineCancel", price, targetItem, true))
		{
			player.sendPacket(ExVariationCancelResult.STATIC_PACKET_FAILURE);
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
			return;
		}
		
		// unequip item
		final InventoryUpdate iu = new InventoryUpdate();
		if (targetItem.isEquipped())
		{
			for (Item itm : player.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot()))
			{
				iu.addModifiedItem(itm);
			}
		}
		
		// remove the augmentation
		targetItem.removeAugmentation();
		
		// send ExVariationCancelResult
		player.sendPacket(ExVariationCancelResult.STATIC_PACKET_SUCCESS);
		
		// send inventory update
		iu.addModifiedItem(targetItem);
		player.sendInventoryUpdate(iu);
	}
}
