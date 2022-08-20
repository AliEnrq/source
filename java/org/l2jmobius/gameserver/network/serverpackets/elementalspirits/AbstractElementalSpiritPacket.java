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
package org.l2jmobius.gameserver.network.serverpackets.elementalspirits;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author JoeAlisson
 */
abstract class AbstractElementalSpiritPacket implements IClientOutgoingPacket
{
	void writeSpiritInfo(PacketWriter packet, ElementalSpirit spirit)
	{
		packet.writeC(spirit.getStage());
		packet.writeD(spirit.getNpcId());
		packet.writeQ(spirit.getExperience());
		packet.writeQ(spirit.getExperienceToNextLevel());
		packet.writeQ(spirit.getExperienceToNextLevel());
		packet.writeD(spirit.getLevel());
		packet.writeD(spirit.getMaxLevel());
		packet.writeD(spirit.getAvailableCharacteristicsPoints());
		packet.writeD(spirit.getAttackPoints());
		packet.writeD(spirit.getDefensePoints());
		packet.writeD(spirit.getCriticalRatePoints());
		packet.writeD(spirit.getCriticalDamagePoints());
		packet.writeD(spirit.getMaxCharacteristics());
		packet.writeD(spirit.getMaxCharacteristics());
		packet.writeD(spirit.getMaxCharacteristics());
		packet.writeD(spirit.getMaxCharacteristics());
		packet.writeC(1); // unk
		for (int j = 0; j < 1; j++)
		{
			packet.writeH(2);
			packet.writeQ(100);
		}
	}
}
