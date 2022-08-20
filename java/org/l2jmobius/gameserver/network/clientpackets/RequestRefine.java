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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.VariationData;
import org.l2jmobius.gameserver.model.VariationInstance;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.options.Variation;
import org.l2jmobius.gameserver.model.options.VariationFee;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExVariationResult;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
public class RequestRefine extends AbstractRefinePacket
{
	private int _targetItemObjId;
	private int _mineralItemObjId;
	private long _feeCount;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetItemObjId = packet.readD();
		_mineralItemObjId = packet.readD();
		packet.readD(); // _feeItemObjId
		_feeCount = packet.readQ();
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
			return;
		}
		
		final Item mineralItem = player.getInventory().getItemByObjectId(_mineralItemObjId);
		if (mineralItem == null)
		{
			return;
		}
		
		final VariationFee fee = VariationData.getInstance().getFee(targetItem.getId(), mineralItem.getId());
		if (fee == null)
		{
			return;
		}
		
		final Item feeItem = player.getInventory().getItemByItemId(fee.getItemId());
		if (feeItem == null)
		{
			return;
		}
		
		if (!isValid(player, targetItem, mineralItem, feeItem, fee))
		{
			player.sendPacket(new ExVariationResult(0, 0, false));
			player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		// TODO: Update XMLs.
		// if (_feeCount != fee.getItemCount())
		if (_feeCount <= 0)
		{
			player.sendPacket(new ExVariationResult(0, 0, false));
			player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		final long adenaFee = fee.getAdenaFee();
		if ((adenaFee > 0) && (player.getAdena() < adenaFee))
		{
			player.sendPacket(new ExVariationResult(0, 0, false));
			player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		final Variation variation = VariationData.getInstance().getVariation(mineralItem.getId());
		if (variation == null)
		{
			player.sendPacket(new ExVariationResult(0, 0, false));
			return;
		}
		
		VariationInstance augment = VariationData.getInstance().generateRandomVariation(variation, targetItem);
		if (augment == null)
		{
			player.sendPacket(new ExVariationResult(0, 0, false));
			return;
		}
		
		// Support for single slot augments.
		final int option1 = augment.getOption1Id();
		final int option2 = augment.getOption2Id();
		if ((option1 == -1) || (option2 == -1))
		{
			final VariationInstance oldAugment = targetItem.getAugmentation();
			if (oldAugment != null)
			{
				if (option1 == -1)
				{
					augment = new VariationInstance(augment.getMineralId(), oldAugment.getOption1Id(), option2);
				}
				else
				{
					augment = new VariationInstance(augment.getMineralId(), option1, oldAugment.getOption2Id());
				}
			}
		}
		
		// Unequip item.
		final InventoryUpdate iu = new InventoryUpdate();
		if (targetItem.isEquipped())
		{
			for (Item itm : player.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot()))
			{
				iu.addModifiedItem(itm);
			}
			player.broadcastUserInfo();
		}
		
		// Consume the life stone.
		if (!player.destroyItem("RequestRefine", mineralItem, 1, null, false))
		{
			return;
		}
		
		// Consume the gemstones.
		if (!player.destroyItem("RequestRefine", feeItem, _feeCount, null, false))
		{
			return;
		}
		
		// Consume Adena.
		if ((adenaFee > 0) && !player.reduceAdena("RequestRefine", adenaFee, player, false))
		{
			return;
		}
		
		// Remove the augmentation if any (286).
		if (targetItem.isAugmented())
		{
			targetItem.removeAugmentation();
		}
		
		targetItem.setAugmentation(augment, true);
		player.sendPacket(new ExVariationResult(augment.getOption1Id(), augment.getOption2Id(), true));
		iu.addModifiedItem(targetItem);
		player.sendInventoryUpdate(iu);
	}
}
