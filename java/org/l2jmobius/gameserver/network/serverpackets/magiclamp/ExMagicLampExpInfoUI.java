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
package org.l2jmobius.gameserver.network.serverpackets.magiclamp;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author L2CCCP
 */
public class ExMagicLampExpInfoUI implements IClientOutgoingPacket
{
	private final Player _player;
	
	public ExMagicLampExpInfoUI(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_MAGICLAMP_EXP_INFO.writeId(packet);
		packet.writeD(Config.ENABLE_MAGIC_LAMP ? 1 : 0); // IsOpen
		packet.writeD(Config.MAGIC_LAMP_MAX_LEVEL_EXP); // MaxMagicLampExp
		packet.writeD(_player.getLampExp()); // MagicLampExp
		packet.writeD(_player.getLampCount()); // MagicLampCount
		return true;
	}
}