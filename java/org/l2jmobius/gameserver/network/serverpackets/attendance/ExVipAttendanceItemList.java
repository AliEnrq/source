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
package org.l2jmobius.gameserver.network.serverpackets.attendance;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.AttendanceRewardData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.AttendanceInfoHolder;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExVipAttendanceItemList implements IClientOutgoingPacket
{
	boolean _available;
	int _index;
	
	public ExVipAttendanceItemList(Player player)
	{
		final AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
		_available = attendanceInfo.isRewardAvailable();
		_index = attendanceInfo.getRewardIndex();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_VIP_ATTENDANCE_ITEM_LIST.writeId(packet);
		packet.writeC(_available ? _index + 1 : _index); // index to receive?
		packet.writeC(_index); // last received index?
		packet.writeD(0);
		packet.writeD(0);
		packet.writeC(1);
		packet.writeC(_available ? 1 : 0); // player can receive reward today?
		packet.writeC(250);
		packet.writeC(AttendanceRewardData.getInstance().getRewardsCount()); // reward size
		int rewardCounter = 0;
		for (ItemHolder reward : AttendanceRewardData.getInstance().getRewards())
		{
			rewardCounter++;
			packet.writeD(reward.getId());
			packet.writeQ(reward.getCount());
			packet.writeC(1); // is unknown?
			packet.writeC((rewardCounter % 7) == 0 ? 1 : 0); // is last in row?
		}
		packet.writeC(0);
		packet.writeD(0);
		return true;
	}
}
