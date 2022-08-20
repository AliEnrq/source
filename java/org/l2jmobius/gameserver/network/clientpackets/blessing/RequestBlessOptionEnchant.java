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
package org.l2jmobius.gameserver.network.clientpackets.blessing;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ItemSkillType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.request.BlessingItemRequest;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.EnchantResult;
import org.l2jmobius.gameserver.network.serverpackets.ExItemAnnounce;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.blessing.ExBlessOptionEnchant;
import org.l2jmobius.gameserver.network.serverpackets.blessing.ExBlessOptionPutItem;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.Util;

/**
 * Written by Horus, on 17.04.2021
 */
public class RequestBlessOptionEnchant implements IClientIncomingPacket
{
	private int _itemObjId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_itemObjId = packet.readD();
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
		
		final Item targetInstance = player.getInventory().getItemByObjectId(_itemObjId);
		if (targetInstance == null)
		{
			player.sendPacket(new ExBlessOptionEnchant(EnchantResult.ERROR));
			return;
		}
		
		final BlessingItemRequest request = player.getRequest(BlessingItemRequest.class);
		if ((request == null) || request.isProcessing())
		{
			player.sendPacket(new ExBlessOptionEnchant(EnchantResult.ERROR));
			return;
		}
		request.setProcessing(true);
		request.setTimestamp(System.currentTimeMillis());
		
		if (!player.isOnline() || client.isDetached())
		{
			return;
		}
		
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendPacket(new ExBlessOptionEnchant(EnchantResult.ERROR));
			return;
		}
		
		final Item item = player.getInventory().getItemByObjectId(_itemObjId);
		if (item == null)
		{
			player.sendPacket(new ExBlessOptionEnchant(EnchantResult.ERROR));
			return;
		}
		
		// first validation check - also over enchant check
		if (item.isBlessed())
		{
			client.sendPacket(SystemMessageId.AUGMENTATION_REQUIREMENTS_ARE_NOT_FULFILLED);
			player.sendPacket(new ExBlessOptionPutItem(0));
			return;
		}
		
		final Item targetScroll = player.getInventory().getItemByItemId(request.getBlessScrollId());
		if (targetScroll == null)
		{
			player.sendPacket(new ExBlessOptionEnchant(EnchantResult.ERROR));
			return;
		}
		
		// attempting to destroy scroll
		if (player.getInventory().destroyItem("Blessing", targetScroll.getObjectId(), 1, player, item) == null)
		{
			client.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			Util.handleIllegalPlayerAction(player, player + " tried to bless with a scroll he doesn't have", Config.DEFAULT_PUNISH);
			player.sendPacket(new ExBlessOptionEnchant(EnchantResult.ERROR));
			return;
		}
		
		if (Rnd.get(100) < Config.BLESSING_CHANCE) // Success
		{
			final ItemTemplate it = item.getTemplate();
			// Increase enchant level only if scroll's base template has chance, some armors can success over +20 but they shouldn't have increased.
			item.setBlessed(true);
			item.updateDatabase();
			player.sendPacket(new ExBlessOptionEnchant(1));
			// Announce the success.
			if ((item.getEnchantLevel() >= (item.isArmor() ? Config.MIN_ARMOR_ENCHANT_ANNOUNCE : Config.MIN_WEAPON_ENCHANT_ANNOUNCE)) //
				&& (item.getEnchantLevel() <= (item.isArmor() ? Config.MAX_ARMOR_ENCHANT_ANNOUNCE : Config.MAX_WEAPON_ENCHANT_ANNOUNCE)))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3);
				sm.addString(player.getName());
				sm.addInt(item.getEnchantLevel());
				sm.addItemName(item);
				player.broadcastPacket(sm);
				Broadcast.toAllOnlinePlayers(new ExItemAnnounce(player, item, ExItemAnnounce.ENCHANT));
				
				final Skill skill = CommonSkill.FIREWORK.getSkill();
				if (skill != null)
				{
					player.broadcastPacket(new MagicSkillUse(player, player, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
				}
			}
			if (item.isEquipped())
			{
				if (item.isArmor())
				{
					it.forEachSkill(ItemSkillType.ON_BLESSING, holder ->
					{
						player.addSkill(holder.getSkill(), false);
						player.sendSkillList();
					});
				}
				player.broadcastUserInfo();
			}
		}
		else // Failure.
		{
			player.sendPacket(new ExBlessOptionEnchant(0));
		}
		
		request.setProcessing(false);
		player.sendItemList();
		player.broadcastUserInfo();
	}
}
