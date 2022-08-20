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
package org.l2jmobius.gameserver.network.serverpackets.appearance;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author UnAfraid
 */
public class ExShapeShiftingResult implements IClientOutgoingPacket
{
	public static final int RESULT_FAILED = 0;
	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_CLOSE = 2;
	
	public static final ExShapeShiftingResult FAILED = new ExShapeShiftingResult(RESULT_FAILED, 0, 0);
	public static final ExShapeShiftingResult CLOSE = new ExShapeShiftingResult(RESULT_CLOSE, 0, 0);
	
	private final int _result;
	private final int _targetItemId;
	private final int _extractItemId;
	
	public ExShapeShiftingResult(int result, int targetItemId, int extractItemId)
	{
		_result = result;
		_targetItemId = targetItemId;
		_extractItemId = extractItemId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHAPE_SHIFTING_RESULT.writeId(packet);
		packet.writeD(_result);
		packet.writeD(_targetItemId);
		packet.writeD(_extractItemId);
		return true;
	}
}
