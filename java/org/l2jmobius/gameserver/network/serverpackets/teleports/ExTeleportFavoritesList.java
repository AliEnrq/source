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
package org.l2jmobius.gameserver.network.serverpackets.teleports;

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExTeleportFavoritesList implements IClientOutgoingPacket
{
	private final List<Integer> _teleports;
	private final boolean _enable;
	
	public ExTeleportFavoritesList(Player player, boolean enable)
	{
		_teleports = player.getVariables().getIntegerList(PlayerVariables.FAVORITE_TELEPORTS);
		_enable = enable;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_TELEPORT_FAVORITES_LIST.writeId(packet);
		packet.writeC(_enable ? 1 : 0);
		packet.writeD(_teleports.size());
		for (int id : _teleports)
		{
			packet.writeD(id);
		}
		return true;
	}
}
