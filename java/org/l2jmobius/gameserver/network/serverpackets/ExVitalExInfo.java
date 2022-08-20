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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Mode
 */
public class ExVitalExInfo implements IClientOutgoingPacket
{
	private final Player _player;
	
	public ExVitalExInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_VITAL_EX_INFO.writeId(packet);
		packet.writeD((int) (_player.getLimitedSayhaGraceEndTime() / 1000)); // currentmilis / 1000, when limited sayha ends
		packet.writeD((int) (_player.getSayhaGraceSupportEndTime() / 1000)); // currentmilis / 1000, when sayha grace suport ends
		packet.writeD((int) (Config.RATE_LIMITED_SAYHA_GRACE_EXP_MULTIPLIER * 100)); // Limited sayha bonus
		packet.writeD(0x82); // Limited sayha bonus adena (shown as 130%, actually 30%)
		return true;
	}
}
