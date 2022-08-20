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
package org.l2jmobius.gameserver.network.serverpackets.huntingzones;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.TimedHuntingZoneData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.TimedHuntingZoneHolder;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class TimedHuntingZoneList implements IClientOutgoingPacket
{
	private final Player _player;
	private final boolean _isInTimedHuntingZone;
	
	public TimedHuntingZoneList(Player player)
	{
		_player = player;
		_isInTimedHuntingZone = player.isInsideZone(ZoneId.TIMED_HUNTING);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_TIME_RESTRICT_FIELD_LIST.writeId(packet);
		final long currentTime = System.currentTimeMillis();
		packet.writeD(TimedHuntingZoneData.getInstance().getSize()); // zone count
		for (TimedHuntingZoneHolder holder : TimedHuntingZoneData.getInstance().getAllHuntingZones())
		{
			packet.writeD(holder.getEntryFee() == 0 ? 0 : 1); // required item count
			packet.writeD(holder.getEntryItemId());
			packet.writeQ(holder.getEntryFee());
			packet.writeD(holder.isWeekly() ? 0 : 1); // reset cycle
			packet.writeD(holder.getZoneId());
			packet.writeD(holder.getMinLevel());
			packet.writeD(holder.getMaxLevel());
			packet.writeD(holder.getInitialTime() / 1000); // remain time base
			int remainingTime = _player.getTimedHuntingZoneRemainingTime(holder.getZoneId());
			if ((remainingTime == 0) && ((_player.getTimedHuntingZoneInitialEntry(holder.getZoneId()) + holder.getResetDelay()) < currentTime))
			{
				remainingTime = holder.getInitialTime();
			}
			packet.writeD(remainingTime / 1000); // remain time
			packet.writeD(holder.getMaximumAddedTime() / 1000);
			packet.writeD(_player.getVariables().getInt(PlayerVariables.HUNTING_ZONE_REMAIN_REFILL_ + holder.getZoneId(), holder.getRemainRefillTime()));
			packet.writeD(holder.getRefillTimeMax());
			packet.writeC(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
			packet.writeC(0); // bUserBound
			packet.writeC(0); // bCanReEnter
			packet.writeC(0); // bIsInZonePCCafeUserOnly
			packet.writeC(0); // bIsPCCafeUser
			packet.writeC(holder.useWorldPrefix() ? 1 : 0); // bWorldInZone
		}
		return true;
	}
}