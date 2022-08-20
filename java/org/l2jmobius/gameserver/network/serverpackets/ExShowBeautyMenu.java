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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw
 */
public class ExShowBeautyMenu implements IClientOutgoingPacket
{
	// TODO: Enum
	public static final int MODIFY_APPEARANCE = 0;
	public static final int RESTORE_APPEARANCE = 1;
	
	private final Player _player;
	private final int _type;
	
	public ExShowBeautyMenu(Player player, int type)
	{
		_player = player;
		_type = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_BEAUTY_MENU.writeId(packet);
		packet.writeD(_type);
		packet.writeD(_player.getVisualHair());
		packet.writeD(_player.getVisualHairColor());
		packet.writeD(_player.getVisualFace());
		return true;
	}
}