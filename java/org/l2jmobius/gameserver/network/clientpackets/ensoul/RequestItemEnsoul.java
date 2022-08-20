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
package org.l2jmobius.gameserver.network.clientpackets.ensoul;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.EnsoulData;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.ensoul.EnsoulOption;
import org.l2jmobius.gameserver.model.ensoul.EnsoulStone;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.AbnormalType;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ensoul.ExEnsoulResult;
import org.l2jmobius.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * @author UnAfraid
 */
public class RequestItemEnsoul implements IClientIncomingPacket
{
	private int _itemObjectId;
	private EnsoulItemOption[] _options;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_itemObjectId = packet.readD();
		final int options = packet.readC();
		if ((options > 0) && (options <= 3))
		{
			_options = new EnsoulItemOption[options];
			for (int i = 0; i < options; i++)
			{
				final int type = packet.readC(); // 1 = normal ; 2 = mystic
				final int position = packet.readC();
				final int soulCrystalObjectId = packet.readD();
				final int soulCrystalOption = packet.readD();
				if ((position > 0) && (position < 3) && ((type == 1) || (type == 2)))
				{
					_options[i] = new EnsoulItemOption(type, position, soulCrystalObjectId, soulCrystalOption);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_WHEN_PRIVATE_STORE_AND_WORKSHOP_ARE_OPENED);
			return;
		}
		if (player.hasAbnormalType(AbnormalType.FREEZING))
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_WHILE_IN_FROZEN_STATE);
		}
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_IF_THE_CHARACTER_IS_DEAD);
			return;
		}
		if ((player.getActiveTradeList() != null) || player.hasItemRequest())
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_DURING_EXCHANGE);
			return;
		}
		if (player.hasAbnormalType(AbnormalType.PARALYZE))
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_WHILE_THE_CHARACTER_IS_PETRIFIED);
			return;
		}
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_DURING_FISHING);
			return;
		}
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_WHILE_SITTING);
			return;
		}
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_INSERTION_IS_IMPOSSIBLE_DURING_COMBAT);
			return;
		}
		
		final Item item = player.getInventory().getItemByObjectId(_itemObjectId);
		if (item == null)
		{
			PacketLogger.warning("Player: " + player + " attempting to ensoul item without having it!");
			return;
		}
		if (!item.isEquipable())
		{
			PacketLogger.warning("Player: " + player + " attempting to ensoul non equippable item: " + item + "!");
			return;
		}
		if (!item.isWeapon())
		{
			PacketLogger.warning("Player: " + player + " attempting to ensoul item that's not a weapon: " + item + "!");
			return;
		}
		if (item.isCommonItem())
		{
			PacketLogger.warning("Player: " + player + " attempting to ensoul common item: " + item + "!");
			return;
		}
		if (item.isShadowItem())
		{
			PacketLogger.warning("Player: " + player + " attempting to ensoul shadow item: " + item + "!");
			return;
		}
		if (item.isHeroItem())
		{
			PacketLogger.warning("Player: " + player + " attempting to ensoul hero item: " + item + "!");
			return;
		}
		if ((_options == null) || (_options.length == 0))
		{
			PacketLogger.warning("Player: " + player + " attempting to ensoul item without any special ability declared!");
			return;
		}
		
		int success = 0;
		final InventoryUpdate iu = new InventoryUpdate();
		for (EnsoulItemOption itemOption : _options)
		{
			final int position = itemOption.getPosition() - 1;
			final Item soulCrystal = player.getInventory().getItemByObjectId(itemOption.getSoulCrystalObjectId());
			if (soulCrystal == null)
			{
				player.sendPacket(SystemMessageId.INVALID_SOUL_CRYSTAL);
				continue;
			}
			
			final EnsoulStone stone = EnsoulData.getInstance().getStone(soulCrystal.getId());
			if (stone == null)
			{
				continue;
			}
			
			if (!stone.getOptions().contains(itemOption.getSoulCrystalOption()))
			{
				PacketLogger.warning("Player: " + player + " attempting to ensoul item option that stone doesn't contains!");
				continue;
			}
			
			final EnsoulOption option = EnsoulData.getInstance().getOption(itemOption.getSoulCrystalOption());
			if (option == null)
			{
				PacketLogger.warning("Player: " + player + " attempting to ensoul item option that doesn't exists!");
				continue;
			}
			
			ItemHolder fee;
			if (itemOption.getType() == 1)
			{
				// Normal Soul Crystal
				fee = EnsoulData.getInstance().getEnsoulFee(item.getTemplate().getCrystalType(), position);
				if (((itemOption.getPosition() == 1) || (itemOption.getPosition() == 2)) && (item.getSpecialAbility(position) != null))
				{
					fee = EnsoulData.getInstance().getResoulFee(item.getTemplate().getCrystalType(), position);
				}
			}
			else if (itemOption.getType() == 2)
			{
				// Mystic Soul Crystal
				fee = EnsoulData.getInstance().getEnsoulFee(item.getTemplate().getCrystalType(), position + 2); // Client Special type position = 0
				if ((itemOption.getPosition() == 1) && (item.getAdditionalSpecialAbility(position) != null))
				{
					fee = EnsoulData.getInstance().getResoulFee(item.getTemplate().getCrystalType(), position + 2); // Client Special type position = 0
				}
			}
			else
			{
				PacketLogger.warning("Player: " + player + " attempting to ensoul item option with unhandled type: " + itemOption.getType() + "!");
				continue;
			}
			
			if (fee == null)
			{
				PacketLogger.warning("Player: " + player + " attempting to ensoul item option that doesn't exists! (unknown fee)");
				continue;
			}
			
			final Item gemStones = player.getInventory().getItemByItemId(fee.getId());
			if ((gemStones == null) || (gemStones.getCount() < fee.getCount()))
			{
				continue;
			}
			
			if (player.destroyItem("EnsoulOption", soulCrystal, 1, player, true) && player.destroyItem("EnsoulOption", gemStones, fee.getCount(), player, true))
			{
				item.addSpecialAbility(option, position, stone.getSlotType(), true);
				success = 1;
			}
			
			if (soulCrystal.isStackable() && (soulCrystal.getCount() > 0))
			{
				iu.addModifiedItem(soulCrystal);
			}
			else
			{
				iu.addRemovedItem(soulCrystal);
			}
			if (gemStones.isStackable() && (gemStones.getCount() > 0))
			{
				iu.addModifiedItem(gemStones);
			}
			else
			{
				iu.addRemovedItem(gemStones);
			}
			iu.addModifiedItem(item);
		}
		player.sendInventoryUpdate(iu);
		if (item.isEquipped())
		{
			item.applySpecialAbilities();
		}
		player.sendPacket(new ExEnsoulResult(success, item));
		item.updateDatabase(true);
	}
	
	static class EnsoulItemOption
	{
		private final int _type;
		private final int _position;
		private final int _soulCrystalObjectId;
		private final int _soulCrystalOption;
		
		EnsoulItemOption(int type, int position, int soulCrystalObjectId, int soulCrystalOption)
		{
			_type = type;
			_position = position;
			_soulCrystalObjectId = soulCrystalObjectId;
			_soulCrystalOption = soulCrystalOption;
		}
		
		public int getType()
		{
			return _type;
		}
		
		public int getPosition()
		{
			return _position;
		}
		
		public int getSoulCrystalObjectId()
		{
			return _soulCrystalObjectId;
		}
		
		public int getSoulCrystalOption()
		{
			return _soulCrystalOption;
		}
	}
}
