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

package org.l2jmobius.gameserver.network.serverpackets.newhenna;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.henna.Henna;
import org.l2jmobius.gameserver.model.item.henna.HennaPoten;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Index, Serenitty
 */
public class NewHennaList implements IClientOutgoingPacket
{
	private final HennaPoten[] _hennaId;
	private final int _activeslots;
	private final int _dailyStep;
	private final int _dailyCount;
	@SuppressWarnings("unused")
	private final int _enchantExp;
	
	public NewHennaList(Player player)
	{
		_dailyStep = player.getDyePotentialDailyStep();
		_dailyCount = player.getDyePotentialDailyCount();
		_hennaId = player.getHennaPotenList();
		_activeslots = player.getAvailableHennaSlots();
		_enchantExp = player.getDyePotentialDailyExp();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_NEW_HENNA_LIST.writeId(packet);
		packet.writeH(_dailyStep);
		packet.writeH(_dailyCount);
		packet.writeD(_hennaId.length);
		for (int i = 1; i <= _hennaId.length; i++)
		{
			final HennaPoten hennaPoten = _hennaId[i - 1];
			final Henna henna = _hennaId[i - 1].getHenna();
			int isactiveStep = hennaPoten.getActiveStep();
			packet.writeD(henna != null ? henna.getDyeId() : 0x00);
			packet.writeD(isactiveStep == 0 ? 0 : hennaPoten.getPotenId());
			packet.writeC(_activeslots);
			packet.writeH(hennaPoten.getEnchantLevel());
			packet.writeD(hennaPoten.getEnchantExp());
			packet.writeH(isactiveStep);
		}
		return true;
	}
}
