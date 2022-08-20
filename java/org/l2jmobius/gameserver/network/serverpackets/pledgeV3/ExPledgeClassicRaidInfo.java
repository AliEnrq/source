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
package org.l2jmobius.gameserver.network.serverpackets.pledgeV3;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Index
 */
public class ExPledgeClassicRaidInfo implements IClientOutgoingPacket
{
	private final Clan _clan;
	
	public ExPledgeClassicRaidInfo(Player player)
	{
		_clan = player.getClan();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_CLASSIC_RAID_INFO.writeId(packet);
		if (_clan == null)
		{
			packet.writeD(0);
		}
		else
		{
			final int stage = GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + _clan.getId(), 0);
			packet.writeD(stage);
			// Skill rewards.
			packet.writeD(5);
			for (int i = 1; i <= 5; i++)
			{
				packet.writeD(1867);
				packet.writeD(i);
			}
		}
		return true;
	}
}
