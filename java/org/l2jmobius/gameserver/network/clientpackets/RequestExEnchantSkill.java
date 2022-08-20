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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.EnchantSkillGroupsData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.enums.SkillEnchantType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.EnchantSkillHolder;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillResult;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden-
 */
public class RequestExEnchantSkill implements IClientIncomingPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestExEnchantSkill.class.getName());
	private static final Logger LOGGER_ENCHANT = Logger.getLogger("enchant.skills");
	
	private SkillEnchantType _type;
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		final int type = packet.readD();
		if ((type < 0) || (type >= SkillEnchantType.values().length))
		{
			LOGGER.log(Level.WARNING, "Client: " + client + " send incorrect type " + type + " on packet: " + getClass().getSimpleName());
			return false;
		}
		
		_type = SkillEnchantType.values()[type];
		_skillId = packet.readD();
		_skillLevel = packet.readH();
		_skillSubLevel = packet.readH();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!client.getFloodProtectors().canPerformPlayerAction())
		{
			return;
		}
		
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
		if (!player.isAllowedToEnchantSkills())
		{
			return;
		}
		
		if (player.isSellingBuffs())
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			return;
		}
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			return;
		}
		
		Skill skill = player.getKnownSkill(_skillId);
		if (skill == null)
		{
			return;
		}
		
		if (!skill.isEnchantable())
		{
			return;
		}
		
		if (skill.getLevel() != _skillLevel)
		{
			return;
		}
		
		if (skill.getSubLevel() > 0)
		{
			if (_type == SkillEnchantType.CHANGE)
			{
				final int group1 = (_skillSubLevel % 1000);
				final int group2 = (skill.getSubLevel() % 1000);
				if (group1 != group2)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Client: " + client + " send incorrect sub level group: " + group1 + " expected: " + group2 + " for skill " + _skillId);
					return;
				}
			}
			else if ((skill.getSubLevel() + 1) != _skillSubLevel)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Client: " + client + " send incorrect sub level: " + _skillSubLevel + " expected: " + (skill.getSubLevel() + 1) + " for skill " + _skillId);
				return;
			}
		}
		
		final EnchantSkillHolder enchantSkillHolder = EnchantSkillGroupsData.getInstance().getEnchantSkillHolder(_skillSubLevel % 1000);
		
		// Verify if player has all the ingredients
		for (ItemHolder holder : enchantSkillHolder.getRequiredItems(_type))
		{
			if (player.getInventory().getInventoryItemCount(holder.getId(), 0) < holder.getCount())
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
		}
		
		// Consume all ingredients
		for (ItemHolder holder : enchantSkillHolder.getRequiredItems(_type))
		{
			if (!player.destroyItemByItemId("Skill enchanting", holder.getId(), holder.getCount(), player, true))
			{
				return;
			}
		}
		
		if (player.getSp() < enchantSkillHolder.getSp(_type))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		player.getStat().removeExpAndSp(0, enchantSkillHolder.getSp(_type), false);
		
		switch (_type)
		{
			case BLESSED:
			case NORMAL:
			case IMMORTAL:
			{
				if (Rnd.get(100) <= enchantSkillHolder.getChance(_type))
				{
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLevel, _skillSubLevel);
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.log(Level.INFO, "Success, Character:" + player.getName() + " [" + player.getObjectId() + "] Account:" + player.getAccountName() + " IP:" + player.getIPAddress() + ", +" + enchantedSkill.getLevel() + " " + enchantedSkill.getSubLevel() + " - " + enchantedSkill.getName() + " (" + enchantedSkill.getId() + "), " + enchantSkillHolder.getChance(_type));
					}
					player.addSkill(enchantedSkill, true);
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
					
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_TRUE);
				}
				else
				{
					final int newSubLevel = ((skill.getSubLevel() > 0) && (enchantSkillHolder.getEnchantFailLevel() > 0)) ? ((skill.getSubLevel() - (skill.getSubLevel() % 1000)) + enchantSkillHolder.getEnchantFailLevel()) : 0;
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLevel, _type == SkillEnchantType.NORMAL ? newSubLevel : skill.getSubLevel());
					if (_type == SkillEnchantType.NORMAL)
					{
						player.addSkill(enchantedSkill, true);
						player.sendPacket(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
					}
					else if (_type == SkillEnchantType.BLESSED)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_CURRENT_LEVEL_OF_ENCHANT_SKILL_S1_WILL_REMAIN_UNCHANGED).addSkillName(skill));
					}
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_FALSE);
					
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.log(Level.INFO, "Failed, Character:" + player.getName() + " [" + player.getObjectId() + "] Account:" + player.getAccountName() + " IP:" + player.getIPAddress() + ", +" + enchantedSkill.getLevel() + " " + enchantedSkill.getSubLevel() + " - " + enchantedSkill.getName() + " (" + enchantedSkill.getId() + "), " + enchantSkillHolder.getChance(_type));
					}
				}
				break;
			}
			case CHANGE:
			{
				if (Rnd.get(100) <= enchantSkillHolder.getChance(_type))
				{
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLevel, _skillSubLevel);
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.info("Success, Character:" + player.getName() + " [" + player.getObjectId() + "] Account:" + player.getAccountName() + " IP:" + player.getIPAddress() + ", +" + enchantedSkill.getLevel() + " " + enchantedSkill.getSubLevel() + " - " + enchantedSkill.getName() + " (" + enchantedSkill.getId() + "), " + enchantSkillHolder.getChance(_type));
					}
					player.addSkill(enchantedSkill, true);
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_WILL_REMAIN);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
					
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_TRUE);
				}
				else
				{
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLevel, enchantSkillHolder.getEnchantFailLevel());
					player.addSkill(enchantedSkill, true);
					player.sendPacket(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_FALSE);
					
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.log(Level.INFO, "Failed, Character:" + player.getName() + " [" + player.getObjectId() + "] Account:" + player.getAccountName() + " IP:" + player.getIPAddress() + ", +" + enchantedSkill.getLevel() + " " + enchantedSkill.getSubLevel() + " - " + enchantedSkill.getName() + " (" + enchantedSkill.getId() + "), " + enchantSkillHolder.getChance(_type));
					}
				}
				break;
			}
		}
		
		player.broadcastUserInfo();
		player.sendSkillList();
		
		skill = player.getKnownSkill(_skillId);
		player.sendPacket(new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), skill.getSubLevel(), skill.getSubLevel()));
		player.sendPacket(new ExEnchantSkillInfoDetail(_type, skill.getId(), skill.getLevel(), Math.min(skill.getSubLevel() + 1, EnchantSkillGroupsData.MAX_ENCHANT_LEVEL), player));
		player.updateShortCuts(skill.getId(), skill.getLevel(), skill.getSubLevel());
	}
}
