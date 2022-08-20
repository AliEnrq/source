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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw, Mobius
 */
public class AcquireSkillList implements IClientOutgoingPacket
{
	private Player _player;
	private Collection<SkillLearn> _learnable;
	
	public AcquireSkillList(Player player)
	{
		if (!player.isSubclassLocked()) // Changing class.
		{
			_player = player;
			_learnable = SkillTreeData.getInstance().getAvailableSkills(player, player.getClassId(), false, false);
			_learnable.addAll(SkillTreeData.getInstance().getNextAvailableSkills(player, player.getClassId(), false, false));
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_player == null)
		{
			return false;
		}
		
		OutgoingPackets.ACQUIRE_SKILL_LIST.writeId(packet);
		packet.writeH(_learnable.size());
		for (SkillLearn skill : _learnable)
		{
			packet.writeD(skill.getSkillId());
			packet.writeH(skill.getSkillLevel());
			packet.writeQ(skill.getLevelUpSp());
			packet.writeC(skill.getGetLevel());
			packet.writeC(0); // Skill dual class level.
			packet.writeC(_player.getKnownSkill(skill.getSkillId()) != null ? 0 : 1);
			packet.writeC(skill.getRequiredItems().size());
			for (ItemHolder item : skill.getRequiredItems())
			{
				packet.writeD(item.getId());
				packet.writeQ(item.getCount());
			}
			final Collection<Skill> removeSkills = skill.getRemoveSkills().stream().map(_player::getKnownSkill).filter(Objects::nonNull).collect(Collectors.toList());
			packet.writeC(removeSkills.size());
			for (Skill removed : removeSkills)
			{
				packet.writeD(removed.getId());
				packet.writeD(removed.getLevel());
			}
		}
		return true;
	}
}
