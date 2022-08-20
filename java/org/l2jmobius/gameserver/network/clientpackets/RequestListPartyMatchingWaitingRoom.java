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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

/**
 * @author Gnacik
 */
public class RequestListPartyMatchingWaitingRoom implements IClientIncomingPacket
{
	private int _page;
	private int _minLevel;
	private int _maxLevel;
	private List<ClassId> _classId; // 1 - waitlist 0 - room waitlist
	private String _query;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_page = packet.readD();
		_minLevel = packet.readD();
		_maxLevel = packet.readD();
		final int size = packet.readD();
		if ((size > 0) && (size < 128))
		{
			_classId = new LinkedList<>();
			for (int i = 0; i < size; i++)
			{
				_classId.add(ClassId.getClassId(packet.readD()));
			}
		}
		if (packet.getReadableBytes() > 0)
		{
			_query = packet.readS();
		}
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		player.sendPacket(new ExListPartyMatchingWaitingRoom(_page, _minLevel, _maxLevel, _classId, _query));
	}
}