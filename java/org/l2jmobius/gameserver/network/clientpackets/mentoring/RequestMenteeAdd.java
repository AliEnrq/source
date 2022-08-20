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
package org.l2jmobius.gameserver.network.clientpackets.mentoring;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.mentoring.ExMentorAdd;

/**
 * @author Gnacik, UnAfraid
 */
public class RequestMenteeAdd implements IClientIncomingPacket
{
	private String _target;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_target = packet.readS();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player mentor = client.getPlayer();
		if (mentor == null)
		{
			return;
		}
		
		final Player mentee = World.getInstance().getPlayer(_target);
		if (mentee == null)
		{
			return;
		}
		
		if (ConfirmMenteeAdd.validate(mentor, mentee))
		{
			mentor.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_OFFERED_TO_BECOME_S1_S_MENTOR).addString(mentee.getName()));
			mentee.sendPacket(new ExMentorAdd(mentor));
		}
	}
}