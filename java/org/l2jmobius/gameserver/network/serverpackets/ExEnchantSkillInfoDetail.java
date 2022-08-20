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

import java.util.Set;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.EnchantSkillGroupsData;
import org.l2jmobius.gameserver.enums.SkillEnchantType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.EnchantSkillHolder;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author KenM
 */
public class ExEnchantSkillInfoDetail implements IClientOutgoingPacket
{
	private final SkillEnchantType _type;
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillSubLevel;
	private final EnchantSkillHolder _enchantSkillHolder;
	
	public ExEnchantSkillInfoDetail(SkillEnchantType type, int skillId, int skillLevel, int skillSubLevel, Player player)
	{
		_type = type;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_skillSubLevel = skillSubLevel;
		_enchantSkillHolder = EnchantSkillGroupsData.getInstance().getEnchantSkillHolder(skillSubLevel % 1000);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENCHANT_SKILL_INFO_DETAIL.writeId(packet);
		packet.writeD(_type.ordinal());
		packet.writeD(_skillId);
		packet.writeH(_skillLevel);
		packet.writeH(_skillSubLevel);
		if (_enchantSkillHolder != null)
		{
			packet.writeQ(_enchantSkillHolder.getSp(_type));
			packet.writeD(_enchantSkillHolder.getChance(_type));
			final Set<ItemHolder> holders = _enchantSkillHolder.getRequiredItems(_type);
			packet.writeD(holders.size());
			holders.forEach(holder ->
			{
				packet.writeD(holder.getId());
				packet.writeD((int) holder.getCount());
			});
		}
		return _enchantSkillHolder != null;
	}
}
