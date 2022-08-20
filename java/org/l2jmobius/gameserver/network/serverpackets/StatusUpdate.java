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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.StatusUpdateType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class StatusUpdate implements IClientOutgoingPacket
{
	private final int _objectId;
	private int _casterObjectId = 0;
	private final boolean _isPlayable;
	private boolean _isVisible = false;
	private final Map<StatusUpdateType, Integer> _updates = new LinkedHashMap<>();
	
	/**
	 * Create {@link StatusUpdate} packet for given {@link WorldObject}.
	 * @param object
	 */
	public StatusUpdate(WorldObject object)
	{
		_objectId = object.getObjectId();
		_isPlayable = object.isPlayable();
	}
	
	public void addUpdate(StatusUpdateType type, int level)
	{
		_updates.put(type, level);
		if (_isPlayable)
		{
			switch (type)
			{
				case CUR_HP:
				case CUR_MP:
				case CUR_CP:
				case CUR_DP:
				{
					_isVisible = true;
				}
			}
		}
	}
	
	public void addCaster(WorldObject object)
	{
		_casterObjectId = object.getObjectId();
	}
	
	public boolean hasUpdates()
	{
		return !_updates.isEmpty();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.STATUS_UPDATE.writeId(packet);
		packet.writeD(_objectId); // casterId
		packet.writeD(_isVisible ? _casterObjectId : 0);
		packet.writeC(_isVisible ? 1 : 0);
		packet.writeC(_updates.size());
		for (Entry<StatusUpdateType, Integer> entry : _updates.entrySet())
		{
			packet.writeC(entry.getKey().getClientId());
			packet.writeD(entry.getValue());
		}
		return true;
	}
}
