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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.sql.CharNameTable;
import org.l2jmobius.gameserver.instancemanager.MentorManager;
import org.l2jmobius.gameserver.model.Mentee;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerMenteeLeft;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerMenteeRemove;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 */
public class RequestMentorCancel implements IClientIncomingPacket
{
	private int _confirmed;
	private String _name;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_confirmed = packet.readD();
		_name = packet.readS();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (_confirmed != 1)
		{
			return;
		}
		
		final Player player = client.getPlayer();
		final int objectId = CharNameTable.getInstance().getIdByName(_name);
		if (player != null)
		{
			if (player.isMentor())
			{
				final Mentee mentee = MentorManager.getInstance().getMentee(player.getObjectId(), objectId);
				if (mentee != null)
				{
					MentorManager.getInstance().cancelAllMentoringBuffs(mentee.getPlayer());
					
					if (MentorManager.getInstance().isAllMenteesOffline(player.getObjectId(), mentee.getObjectId()))
					{
						MentorManager.getInstance().cancelAllMentoringBuffs(player);
					}
					
					player.sendPacket(new SystemMessage(SystemMessageId.S1_S_MENTORING_CONTRACT_IS_CANCELLED_THE_MENTOR_CANNOT_BOND_WITH_ANOTHER_MENTEE_FOR_2_DAYS).addString(_name));
					MentorManager.getInstance().setPenalty(player.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(player.getObjectId(), mentee.getObjectId());
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeRemove(player, mentee), player);
				}
			}
			else if (player.isMentee())
			{
				final Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
				if ((mentor != null) && (mentor.getObjectId() == objectId))
				{
					MentorManager.getInstance().cancelAllMentoringBuffs(player);
					
					if (MentorManager.getInstance().isAllMenteesOffline(mentor.getObjectId(), player.getObjectId()))
					{
						MentorManager.getInstance().cancelAllMentoringBuffs(mentor.getPlayer());
					}
					
					MentorManager.getInstance().setPenalty(mentor.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(mentor.getObjectId(), player.getObjectId());
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeLeft(mentor, player), player);
					mentor.getPlayer().sendPacket(new SystemMessage(SystemMessageId.S1_S_MENTORING_CONTRACT_IS_CANCELLED_THE_MENTOR_CANNOT_BOND_WITH_ANOTHER_MENTEE_FOR_2_DAYS).addString(_name));
				}
			}
		}
	}
}
