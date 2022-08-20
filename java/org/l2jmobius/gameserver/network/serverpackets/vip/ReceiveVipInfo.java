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
package org.l2jmobius.gameserver.network.serverpackets.vip;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.vip.VipManager;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

public class ReceiveVipInfo implements IClientOutgoingPacket
{
	private final Player _player;
	
	public ReceiveVipInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (!Config.VIP_SYSTEM_ENABLED)
		{
			return false;
		}
		
		final VipManager vipManager = VipManager.getInstance();
		final byte vipTier = _player.getVipTier();
		final int vipDuration = (int) ChronoUnit.SECONDS.between(Instant.now(), Instant.ofEpochMilli(_player.getVipTierExpiration()));
		
		OutgoingPackets.RECIVE_VIP_INFO.writeId(packet);
		packet.writeC(vipTier);
		packet.writeQ(_player.getVipPoints());
		packet.writeD(vipDuration);
		packet.writeQ(vipManager.getPointsToLevel((byte) (vipTier + 1)));
		packet.writeQ(vipManager.getPointsDepreciatedOnLevel(vipTier));
		packet.writeC(vipTier);
		packet.writeQ(vipManager.getPointsToLevel(vipTier));
		return true;
	}
}