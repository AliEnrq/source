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
package org.l2jmobius.gameserver.network.clientpackets.newhenna;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.HennaPatternPotentialData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.henna.DyePotential;
import org.l2jmobius.gameserver.model.item.henna.HennaPoten;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.newhenna.NewHennaPotenSelect;

/**
 * @author Index, Serenitty
 */
public class RequestNewHennaPotenSelect implements IClientIncomingPacket
{
	private int _slotId;
	private int _potenId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_slotId = packet.readC();
		_potenId = packet.readD();
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
		
		if ((_slotId < 1) || (_slotId > player.getHennaPotenList().length))
		{
			return;
		}
		
		final DyePotential potential = HennaPatternPotentialData.getInstance().getPotential(_potenId);
		final HennaPoten hennaPoten = player.getHennaPoten(_slotId);
		if ((potential == null) || (potential.getSlotId() != _slotId))
		{
			player.sendPacket(new NewHennaPotenSelect(_slotId, _potenId, hennaPoten.getActiveStep(), false));
			return;
		}
		
		hennaPoten.setPotenId(_potenId);
		player.sendPacket(new NewHennaPotenSelect(_slotId, _potenId, hennaPoten.getActiveStep(), true));
		player.applyDyePotenSkills();
	}
}
