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
package org.l2jmobius.gameserver.network.serverpackets.vip;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Gabriel Costa Souza
 */
public class ReceiveVipLuckyGameInfo implements IClientOutgoingPacket
{
	private final Player _player;
	
	public ReceiveVipLuckyGameInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.RECIVE_VIP_LUCKY_GAME_INFO.writeId(packet);
		packet.writeC(1); // enabled
		packet.writeH((int) _player.getAdena());
		final Item item = _player.getInventory().getItemByItemId(Inventory.LCOIN_ID);
		packet.writeH(item == null ? 0 : (int) item.getCount()); // L Coin count
		return true;
	}
}
