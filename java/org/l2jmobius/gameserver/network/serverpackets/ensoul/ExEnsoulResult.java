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
package org.l2jmobius.gameserver.network.serverpackets.ensoul;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ensoul.EnsoulOption;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author UnAfraid
 */
public class ExEnsoulResult implements IClientOutgoingPacket
{
	private final int _success;
	private final Item _item;
	
	public ExEnsoulResult(int success, Item item)
	{
		_success = success;
		_item = item;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENSOUL_RESULT.writeId(packet);
		packet.writeC(_success); // success / failure
		packet.writeC(_item.getSpecialAbilities().size());
		for (EnsoulOption option : _item.getSpecialAbilities())
		{
			packet.writeD(option.getId());
		}
		packet.writeC(_item.getAdditionalSpecialAbilities().size());
		for (EnsoulOption option : _item.getAdditionalSpecialAbilities())
		{
			packet.writeD(option.getId());
		}
		return true;
	}
}
