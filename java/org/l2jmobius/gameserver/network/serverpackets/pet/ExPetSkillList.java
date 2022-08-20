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
package org.l2jmobius.gameserver.network.serverpackets.pet;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 26.04.2021
 */
public class ExPetSkillList implements IClientOutgoingPacket
{
	private final boolean _onEnter;
	private final Summon _pet;
	
	public ExPetSkillList(boolean onEnter, Summon pet)
	{
		_onEnter = onEnter;
		_pet = pet;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PET_SKILL_LIST.writeId(packet);
		packet.writeC(_onEnter ? 1 : 0);
		packet.writeD(_pet.getAllSkills().size());
		for (Skill sk : _pet.getAllSkills())
		{
			packet.writeD(sk.getDisplayId());
			packet.writeD(sk.getDisplayLevel());
			packet.writeD(sk.getReuseDelayGroup());
			packet.writeC(0);
			packet.writeC(0);
		}
		return true;
	}
}
