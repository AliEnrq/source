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
package org.l2jmobius.gameserver.network.serverpackets.raidbossinfo;

import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExRaidBossSpawnInfo implements IClientOutgoingPacket
{
	private static final int BAIUM = 29020;
	
	private final Map<Integer, Integer> _statuses;
	
	public ExRaidBossSpawnInfo(Map<Integer, Integer> statuses)
	{
		_statuses = statuses;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RAID_BOSS_SPAWN_INFO.writeId(packet);
		packet.writeD(_statuses.size()); // count
		for (Entry<Integer, Integer> entry : _statuses.entrySet())
		{
			packet.writeD(entry.getKey());
			packet.writeD((entry.getKey() == BAIUM) && (GrandBossManager.getInstance().getBossStatus(BAIUM) == 0) ? 1 : entry.getValue());
			packet.writeD(0);
		}
		return true;
	}
}
