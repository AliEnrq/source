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
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.elementalspirits.ElementalSpiritSetTalent;

/**
 * @author JoeAlisson
 */
public class ExElementalInitTalent implements IClientIncomingPacket
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
		
		if (player.isInBattle())
		{
			client.sendPacket(new SystemMessage(SystemMessageId.UNABLE_TO_RESET_THE_SPIRIT_ATTRIBUTES_WHILE_IN_BATTLE));
			client.sendPacket(new ElementalSpiritSetTalent(player, _type, false));
			return;
		}
		
		if (player.reduceAdena("Talent", ElementalSpiritData.TALENT_INIT_FEE, player, true))
		{
			spirit.resetCharacteristics();
			client.sendPacket(new SystemMessage(SystemMessageId.RESET_THE_SELECTED_SPIRIT_S_CHARACTERISTICS_SUCCESSFULLY));
			client.sendPacket(new ElementalSpiritSetTalent(player, _type, true));
		}
		else
		{
			client.sendPacket(new ElementalSpiritSetTalent(player, _type, false));
		}
	}
}
