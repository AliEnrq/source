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
package org.l2jmobius.gameserver.network.serverpackets;

import java.time.Instant;
import java.time.ZoneId;
import java.time.zone.ZoneRules;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Mobius
 */
public class ExEnterWorld implements IClientOutgoingPacket
{
	private final int _zoneIdOffsetSeconds;
	private final int _epochInSeconds;
	private final int _daylight;
	
	public ExEnterWorld()
	{
		Instant now = Instant.now();
		_epochInSeconds = (int) now.getEpochSecond();
		ZoneRules rules = ZoneId.systemDefault().getRules();
		_zoneIdOffsetSeconds = rules.getStandardOffset(now).getTotalSeconds();
		_daylight = (int) (rules.getDaylightSavings(now).toMillis() / 1000);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENTER_WORLD.writeId(packet);
		packet.writeD(_epochInSeconds);
		packet.writeD(-_zoneIdOffsetSeconds);
		packet.writeD(_daylight);
		packet.writeD(Config.SERVER_ID);
		return true;
	}
}