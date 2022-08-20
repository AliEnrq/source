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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.ElementalSpiritData;
import org.l2jmobius.gameserver.enums.ElementalType;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.enums.UserInfoType;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.elementalspirits.ElementalSpiritExtract;

/**
 * @author JoeAlisson
 */
public class ExElementalSpiritExtract implements IClientIncomingPacket
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
		
		final boolean canExtract = checkConditions(player, spirit);
		if (canExtract)
		{
			final int amount = spirit.getExtractAmount();
			client.sendPacket(new SystemMessage(SystemMessageId.EXTRACTED_S1_S2_SUCCESSFULLY).addItemName(spirit.getExtractItem()).addInt(amount));
			spirit.reduceLevel();
			player.addItem("ElementalSpiritExtract", spirit.getExtractItem(), amount, player, true);
			
			final UserInfo userInfo = new UserInfo(player);
			userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
			client.sendPacket(userInfo);
		}
		
		client.sendPacket(new ElementalSpiritExtract(player, _type, canExtract));
	}
	
	private boolean checkConditions(Player player, ElementalSpirit spirit)
	{
		if ((spirit.getLevel() < 2) || (spirit.getExtractAmount() < 1))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ATTRIBUTE_XP_FOR_EXTRACTION);
			return false;
		}
		if (!player.getInventory().validateCapacity(1))
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_EXTRACT_BECAUSE_INVENTORY_IS_FULL);
			return false;
		}
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
		if (!player.reduceAdena("ElementalSpiritExtract", ElementalSpiritData.EXTRACT_FEES[spirit.getStage() - 1], player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_INGREDIENTS_TO_EXTRACT);
			return false;
		}
		return true;
	}
}
