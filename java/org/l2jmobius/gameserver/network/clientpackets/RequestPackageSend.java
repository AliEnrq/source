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
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.itemcontainer.ItemContainer;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerFreight;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author -Wooden-
 * @author UnAfraid Thanks mrTJO
 */
public class RequestPackageSend implements IClientIncomingPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item
	
	private ItemHolder[] _items = null;
	private int _objectId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		
		final int count = packet.readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != packet.getReadableBytes()))
		{
			return false;
		}
		
		_items = new ItemHolder[count];
		for (int i = 0; i < count; i++)
		{
			final int objId = packet.readD();
			final long cnt = packet.readQ();
			if ((objId < 1) || (cnt < 0))
			{
				_items = null;
				return false;
			}
			
			_items[i] = new ItemHolder(objId, cnt);
		}
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if ((_items == null) || (player == null) || !player.getAccountChars().containsKey(_objectId))
		{
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		if (player.hasItemRequest())
		{
			Util.handleIllegalPlayerAction(player, player + " tried to use enchant exploit!", Config.DEFAULT_PUNISH);
			return;
		}
		
		// get current tradelist if any
		if (player.getActiveTradeList() != null)
		{
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getReputation() < 0))
		{
			return;
		}
		
		// Freight price from config per item slot.
		final int fee = _items.length * Config.ALT_FREIGHT_PRICE;
		long currentAdena = player.getAdena();
		int slots = 0;
		
		final ItemContainer warehouse = new PlayerFreight(_objectId);
		for (ItemHolder i : _items)
		{
			// Check validity of requested item
			final Item item = player.checkItemManipulation(i.getId(), i.getCount(), "freight");
			if (item == null)
			{
				PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				warehouse.deleteMe();
				return;
			}
			else if (!item.isFreightable())
			{
				warehouse.deleteMe();
				return;
			}
			
			// Calculate needed adena and slots
			if (item.getId() == Inventory.ADENA_ID)
			{
				currentAdena -= i.getCount();
			}
			else if (!item.isStackable())
			{
				slots += i.getCount();
			}
			else if (warehouse.getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}
		
		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			warehouse.deleteMe();
			return;
		}
		
		// Check if enough adena and charge the fee
		if ((currentAdena < fee) || !player.reduceAdena(warehouse.getName(), fee, player, false))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
			warehouse.deleteMe();
			return;
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (ItemHolder i : _items)
		{
			// Check validity of requested item
			final Item oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
			if (oldItem == null)
			{
				PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				warehouse.deleteMe();
				return;
			}
			
			final Item newItem = player.getInventory().transferItem("Trade", i.getId(), i.getCount(), warehouse, player, null);
			if (newItem == null)
			{
				PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}
			
			if (playerIU != null)
			{
				if ((oldItem.getCount() > 0) && (oldItem != newItem))
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
			
			// Remove item objects from the world.
			World.getInstance().removeObject(oldItem);
			World.getInstance().removeObject(newItem);
		}
		
		warehouse.deleteMe();
		
		// Send updated item list to the player
		if (playerIU != null)
		{
			player.sendInventoryUpdate(playerIU);
		}
		else
		{
			player.sendItemList();
		}
	}
}
