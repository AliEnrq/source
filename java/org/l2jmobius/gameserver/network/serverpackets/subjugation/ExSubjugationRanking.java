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
import org.l2jmobius.gameserver.instancemanager.PurgeRankingManager;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 13.04.2021
 */
public class ExSubjugationRanking implements IClientOutgoingPacket
{
	private final Map<String, Integer> _ranking;
	private final int _category;
	private final int _objectId;
	
	public ExSubjugationRanking(int category, int objectId)
	{
		_ranking = PurgeRankingManager.getInstance().getTop5(category);
		_category = category;
		_objectId = objectId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SUBJUGATION_RANKING.writeId(packet);
		packet.writeD(_ranking.entrySet().size());
		int counter = 1;
		for (Entry<String, Integer> data : _ranking.entrySet())
		{
			packet.writeString(data.getKey());
			packet.writeD(data.getValue());
			packet.writeD(counter++);
		}
		packet.writeD(_category);
		packet.writeD(PurgeRankingManager.getInstance().getPlayerRating(_category, _objectId).getValue());
		packet.writeD(PurgeRankingManager.getInstance().getPlayerRating(_category, _objectId).getKey());
		return true;
	}
}
