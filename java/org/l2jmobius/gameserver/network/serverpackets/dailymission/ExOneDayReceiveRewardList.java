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
package org.l2jmobius.gameserver.network.serverpackets.dailymission;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.time.SchedulingPattern;
import org.l2jmobius.gameserver.data.xml.DailyMissionData;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Sdw
 */
public class ExOneDayReceiveRewardList implements IClientOutgoingPacket
{
	private static final SchedulingPattern DAILY_REUSE_PATTERN = new SchedulingPattern("30 6 * * *");
	private static final SchedulingPattern WEEKLY_REUSE_PATTERN = new SchedulingPattern("30 6 * * 1");
	private static final SchedulingPattern MONTHLY_REUSE_PATTERN = new SchedulingPattern("30 6 1 * *");
	
	private final Player _player;
	private final Collection<DailyMissionDataHolder> _rewards;
	private final int _dayRemainTime;
	private final int _weekRemainTime;
	private final int _monthRemainTime;
	
	public ExOneDayReceiveRewardList(Player player, boolean sendRewards)
	{
		_player = player;
		_rewards = sendRewards ? DailyMissionData.getInstance().getDailyMissionData(player) : Collections.emptyList();
		_dayRemainTime = (int) ((DAILY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_weekRemainTime = (int) ((WEEKLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
		_monthRemainTime = (int) ((MONTHLY_REUSE_PATTERN.next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (!DailyMissionData.getInstance().isAvailable())
		{
			return true;
		}
		
		OutgoingPackets.EX_ONE_DAY_RECEIVE_REWARD_LIST.writeId(packet);
		packet.writeD(_dayRemainTime);
		packet.writeD(_weekRemainTime);
		packet.writeD(_monthRemainTime);
		packet.writeC(0x17);
		packet.writeD(_player.getClassId().getId());
		packet.writeD(LocalDate.now().getDayOfWeek().ordinal()); // Day of week
		packet.writeD(_rewards.size());
		for (DailyMissionDataHolder reward : _rewards)
		{
			packet.writeH(reward.getId());
			final int status = reward.getStatus(_player);
			packet.writeC(status);
			packet.writeC(reward.getRequiredCompletions() > 1 ? 1 : 0);
			packet.writeD(reward.getParams().getInt("level", -1) == -1 ? (status == 1 ? 0 : reward.getProgress(_player)) : _player.getLevel());
			packet.writeD(reward.getRequiredCompletions());
		}
		return true;
	}
}
