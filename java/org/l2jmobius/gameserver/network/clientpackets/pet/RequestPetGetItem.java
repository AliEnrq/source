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
package org.l2jmobius.gameserver.network.clientpackets.pet;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import org.l2jmobius.gameserver.instancemanager.SiegeGuardManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class RequestPetGetItem implements IClientIncomingPacket
{
	private int _objectId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final World world = World.getInstance();
		final Item item = (Item) world.findObject(_objectId);
		if ((item == null) || (client.getPlayer() == null) || !client.getPlayer().hasPet())
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(item);
		if ((castle != null) && (SiegeGuardManager.getInstance().getSiegeGuardByItem(castle.getResidenceId(), item.getId()) != null))
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (FortSiegeManager.getInstance().isCombat(item.getId()))
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Pet pet = client.getPlayer().getPet();
		if (pet.isDead() || pet.isControlBlocked())
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (pet.isUncontrollable())
		{
			client.sendPacket(SystemMessageId.WHEN_YOUR_PET_S_SATIETY_REACHES_0_YOU_CANNOT_CONTROL_IT);
			return;
		}
		
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}
}
