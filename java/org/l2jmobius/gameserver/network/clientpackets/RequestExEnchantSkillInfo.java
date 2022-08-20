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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.Set;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.EnchantSkillGroupsData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillInfo;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill level
 * @author -Wooden-
 */
public class RequestExEnchantSkillInfo implements IClientIncomingPacket
{
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_skillId = packet.readD();
		_skillLevel = packet.readH();
		_skillSubLevel = packet.readH();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if ((_skillId <= 0) || (_skillLevel <= 0) || (_skillSubLevel < 0))
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// if (!player.isInCategory(CategoryType.SIXTH_CLASS_GROUP))
		// {
		// return;
		// }
		
		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLevel, _skillSubLevel);
		if ((skill == null) || (skill.getId() != _skillId))
		{
			return;
		}
		final Set<Integer> route = EnchantSkillGroupsData.getInstance().getRouteForSkill(_skillId, _skillLevel);
		if (route.isEmpty())
		{
			return;
		}
		
		final Skill playerSkill = player.getKnownSkill(_skillId);
		if ((playerSkill.getLevel() != _skillLevel) || (playerSkill.getSubLevel() != _skillSubLevel))
		{
			return;
		}
		
		player.sendPacket(new ExEnchantSkillInfo(_skillId, _skillLevel, _skillSubLevel, playerSkill.getSubLevel()));
		// ExEnchantSkillInfoDetail - not really necessary I think
		// cplayer.sendPacket(new ExEnchantSkillInfoDetail(SkillEnchantType.NORMAL, _skillId, _skillLevel, _skillSubLevel , activeChar));
	}
}