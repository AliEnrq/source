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
package org.l2jmobius.gameserver.network.clientpackets.newhenna;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.HennaData;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.henna.Henna;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.newhenna.NewHennaEquip;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author Index, Serenitty
 */
public class RequestNewHennaEquip implements IClientIncomingPacket
{
	private int _slotId;
	private int _symbolId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_slotId = packet.readC();
		_symbolId = packet.readD();
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
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			return;
		}
		
		if (player.getHennaEmptySlots() == 0)
		{
			PacketLogger.warning(player + ": Invalid Henna error 0 Id " + _symbolId + " " + _slotId);
			player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_A_PATTERN);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Item item = player.getInventory().getItemByObjectId(_symbolId);
		if (item == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendPacket(new NewHennaEquip(_slotId, 0, false));
			return;
		}
		
		final Henna henna = HennaData.getInstance().getHennaByItemId(item.getId());
		if (henna == null)
		{
			PacketLogger.warning(player + ": Invalid Henna SymbolId " + _symbolId + " " + _slotId + " " + item.getTemplate());
			client.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_A_PATTERN);
			return;
		}
		
		final long count = player.getInventory().getInventoryItemCount(henna.getDyeItemId(), -1);
		if (henna.isAllowedClass(player) && (count >= henna.getWearCount()) && (player.getAdena() >= henna.getWearFee()) && player.addHenna(_slotId, henna))
		{
			player.destroyItemByItemId("Henna", henna.getDyeItemId(), henna.getWearCount(), player, true);
			player.getInventory().reduceAdena("Henna", henna.getWearFee(), player, player.getLastFolkNPC());
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(player.getInventory().getAdenaInstance());
			player.sendInventoryUpdate(iu);
			player.sendPacket(new NewHennaEquip(_slotId, henna.getDyeId(), true));
			player.sendPacket(SystemMessageId.PATTERN_ADDED);
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_A_PATTERN);
			if (!player.canOverrideCond(PlayerCondOverride.ITEM_CONDITIONS) && !henna.isAllowedClass(player))
			{
				Util.handleIllegalPlayerAction(player, "Exploit attempt: Character " + player.getName() + " of account " + player.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
			PacketLogger.warning(player + ": Invalid Henna error 2 " + _symbolId + " " + _slotId + " " + item.getTemplate());
			client.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
