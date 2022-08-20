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
package org.l2jmobius.gameserver.network.serverpackets.randomcraft;

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.RandomCraftRewardItemHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mode
 */
public class ExCraftRandomInfo implements IClientOutgoingPacket
{
	private final Player _player;
	
	public ExCraftRandomInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CRAFT_RANDOM_INFO.writeId(packet);
		final List<RandomCraftRewardItemHolder> rewards = _player.getRandomCraft().getRewards();
		int size = 5;
		packet.writeD(size); // size
		for (int i = 0; i < rewards.size(); i++)
		{
			final RandomCraftRewardItemHolder holder = rewards.get(i);
			if ((holder != null) && (holder.getItemId() != 0))
			{
				packet.writeC(holder.isLocked() ? 1 : 0); // Locked
				packet.writeD(holder.getLockLeft()); // Rolls it will stay locked
				packet.writeD(holder.getItemId()); // Item id
				packet.writeQ(holder.getItemCount()); // Item count
			}
			else
			{
				packet.writeC(0);
				packet.writeD(0);
				packet.writeD(0);
				packet.writeQ(0);
			}
			size--;
		}
		// Write missing
		for (int i = size; i > 0; i--)
		{
			packet.writeC(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeQ(0);
		}
		return true;
	}
}
