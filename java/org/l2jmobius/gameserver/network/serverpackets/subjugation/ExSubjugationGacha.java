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
package org.l2jmobius.gameserver.network.serverpackets.subjugation;

import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 15.04.2021
 */
public class ExSubjugationGacha implements IClientOutgoingPacket
{
	private final Map<Integer, Integer> _rewards;
	
	public ExSubjugationGacha(Map<Integer, Integer> rewards)
	{
		_rewards = rewards;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SUBJUGATION_GACHA.writeId(packet);
		packet.writeD(_rewards.size());
		for (Entry<Integer, Integer> entry : _rewards.entrySet())
		{
			packet.writeD(entry.getKey());
			packet.writeD(entry.getValue());
		}
		return true;
	}
}
