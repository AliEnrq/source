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
package org.l2jmobius.gameserver.network.serverpackets.castlewar;

import java.util.Calendar;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.TaxType;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Serenitty
 */
public class ExMercenaryCastleWarCastleInfo implements IClientOutgoingPacket
{
	private final Castle _castle;
	
	public ExMercenaryCastleWarCastleInfo(Castle castle)
	{
		_castle = castle;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_MERCENARY_CASTLEWAR_CASTLE_INFO.writeId(packet);
		packet.writeD(_castle.getResidenceId());
		final var clan = _castle.getOwner();
		if (clan != null)
		{
			packet.writeD(clan.getId());
			packet.writeD(clan.getCrestId());
			packet.writeString(clan.getName());
			packet.writeString(clan.getLeaderName());
		}
		else
		{
			packet.writeD(0);
			packet.writeD(0);
			packet.writeString("");
			packet.writeString("");
		}
		packet.writeD(_castle.getTaxPercent(TaxType.BUY));
		packet.writeQ((long) (_castle.getTreasury() * _castle.getTaxRate(TaxType.BUY)));
		packet.writeQ((long) (_castle.getTreasury() + (_castle.getTreasury() * _castle.getTaxRate(TaxType.BUY))));
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(_castle.getSiegeDate().getTimeInMillis());
		packet.writeD((int) (cal.getTimeInMillis() / 1000));
		return true;
	}
}
