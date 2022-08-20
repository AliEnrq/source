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
package org.l2jmobius.gameserver.network.serverpackets.newhenna;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Index, Serenitty
 */
public class NewHennaPotenCompose implements IClientOutgoingPacket
{
	private final int _resultHennaId;
	private final int _resultItemId;
	private final boolean _success;
	
	public NewHennaPotenCompose(int resultHennaId, int resultItemId, boolean success)
	{
		_resultHennaId = resultHennaId;
		_resultItemId = resultItemId;
		_success = success;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_NEW_HENNA_COMPOSE.writeId(packet);
		packet.writeD(_resultHennaId);
		packet.writeD(_resultItemId);
		packet.writeC(_success ? 1 : 0);
		return true;
	}
}
