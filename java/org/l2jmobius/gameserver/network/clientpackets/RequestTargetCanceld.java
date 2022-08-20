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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.TargetUnselected;

/**
 * @author Mobius
 */
public class RequestTargetCanceld implements IClientIncomingPacket
{
	private boolean _targetLost;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetLost = packet.readH() != 0;
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
		
		if (player.isLockedTarget())
		{
			player.sendPacket(SystemMessageId.FAILED_TO_REMOVE_ENMITY);
			return;
		}
		
		if (player.getQueuedSkill() != null)
		{
			player.setQueuedSkill(null, null, false, false);
		}
		
		if (player.isCastingNow())
		{
			player.abortAllSkillCasters();
		}
		
		if (_targetLost)
		{
			player.setTarget(null);
		}
		
		if (player.isInAirShip())
		{
			player.broadcastPacket(new TargetUnselected(player));
		}
	}
}
