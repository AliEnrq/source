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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.ai.NextAction;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.VariationData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ItemSkillType;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.handler.AdminCommandHandler;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.holders.ItemSkillHolder;
import org.l2jmobius.gameserver.model.item.EtcItem;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.ActionType;
import org.l2jmobius.gameserver.model.item.type.ArmorType;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import org.l2jmobius.gameserver.network.serverpackets.ExUseSharedGroupItem;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class UseItem implements IClientIncomingPacket
{
	private int _objectId;
	private boolean _ctrlPressed;
	private int _itemId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		_ctrlPressed = packet.readD() != 0;
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
		
		// Flood protect UseItem
		if (!client.getFloodProtectors().canUseItem())
		{
			return;
		}
		
		if (player.isInsideZone(ZoneId.JAIL))
		{
			player.sendMessage("You cannot use items while jailed.");
			return;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.cancelActiveTrade();
		}
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Item item = player.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			// GM can use other player item
			if (player.isGM())
			{
				final WorldObject obj = World.getInstance().findObject(_objectId);
				if (obj.isItem())
				{
					AdminCommandHandler.getInstance().useAdminCommand(player, "admin_use_item " + _objectId, true);
				}
			}
			return;
		}
		
		if (item.isQuestItem() && (item.getTemplate().getDefaultAction() != ActionType.NONE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_QUEST_ITEMS);
			return;
		}
		
		// No UseItem is allowed while the player is in special conditions
		if (player.hasBlockActions() || player.isControlBlocked() || player.isAlikeDead())
		{
			return;
		}
		
		// Char cannot use item when dead
		if (player.isDead() || !player.getInventory().canManipulateWithItemId(item.getId()))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addItemName(item);
			player.sendPacket(sm);
			return;
		}
		
		if (!item.isEquipped() && !item.getTemplate().checkCondition(player, player, true))
		{
			return;
		}
		
		_itemId = item.getId();
		if (player.isFishing() && ((_itemId < 6535) || (_itemId > 6540)))
		{
			// You cannot do anything else while fishing
			player.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING_3);
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (player.getReputation() < 0))
		{
			final List<ItemSkillHolder> skills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
			if ((skills != null) && skills.stream().anyMatch(holder -> holder.getSkill().hasEffectType(EffectType.TELEPORT)))
			{
				return;
			}
		}
		
		// If the item has reuse time and it has not passed.
		// Message from reuse delay must come from item.
		final int reuseDelay = item.getReuseDelay();
		final int sharedReuseGroup = item.getSharedReuseGroup();
		if (reuseDelay > 0)
		{
			final long reuse = player.getItemRemainingReuseTime(item.getObjectId());
			if (reuse > 0)
			{
				reuseData(player, item, reuse);
				sendSharedGroupUpdate(player, sharedReuseGroup, reuse, reuseDelay);
				return;
			}
			
			final long reuseOnGroup = player.getReuseDelayOnGroup(sharedReuseGroup);
			if (reuseOnGroup > 0)
			{
				reuseData(player, item, reuseOnGroup);
				sendSharedGroupUpdate(player, sharedReuseGroup, reuseOnGroup, reuseDelay);
				return;
			}
		}
		
		player.onActionRequest();
		
		if (item.isEquipable())
		{
			// Don't allow to put formal wear while a cursed weapon is equipped.
			if (player.isCursedWeaponEquipped() && (_itemId == 6408))
			{
				return;
			}
			
			// Equip or unEquip
			if (FortSiegeManager.getInstance().isCombat(_itemId))
			{
				return; // no message
			}
			
			if (player.isCombatFlagEquipped())
			{
				return;
			}
			
			if (player.getInventory().isItemSlotBlocked(item.getTemplate().getBodyPart()))
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
				return;
			}
			
			// Prevent equip shields for Death Knight players.
			if (item.isArmor() && (item.getArmorItem().getItemType() == ArmorType.SHIELD) && CategoryData.getInstance().isInCategory(CategoryType.DEATH_KNIGHT_ALL_CLASS, player.getClassId().getId()))
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
				return;
			}
			
			// Prevent equip pistols for non Sylph players.
			if (item.isWeapon() && (item.getWeaponItem().getItemType() == WeaponType.PISTOLS) && (player.getRace() != Race.SYLPH))
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
				return;
			}
			
			// Prevent players to equip weapon while wearing combat flag
			// Don't allow weapon/shield equipment if a cursed weapon is equipped.
			if ((item.getTemplate().getBodyPart() == ItemTemplate.SLOT_LR_HAND) || (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_L_HAND) || (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_R_HAND))
			{
				if ((player.getActiveWeaponItem() != null) && (player.getActiveWeaponItem().getId() == 9819))
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
					return;
				}
				if (player.isMounted() || player.isDisarmed())
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
					return;
				}
				if (player.isCursedWeaponEquipped())
				{
					return;
				}
			}
			else if (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_DECO)
			{
				if (!item.isEquipped() && (player.getInventory().getTalismanSlots() == 0))
				{
					player.sendPacket(SystemMessageId.NO_EQUIPMENT_SLOT_AVAILABLE);
					return;
				}
			}
			else if (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_BROOCH_JEWEL)
			{
				if (!item.isEquipped() && (player.getInventory().getBroochJewelSlots() == 0))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANNOT_EQUIP_S1_WITHOUT_EQUIPPING_A_BROOCH);
					sm.addItemName(item);
					player.sendPacket(sm);
					return;
				}
			}
			else if (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_AGATHION)
			{
				if (!item.isEquipped() && (player.getInventory().getAgathionSlots() == 0))
				{
					player.sendPacket(SystemMessageId.NO_EQUIPMENT_SLOT_AVAILABLE);
					return;
				}
			}
			else if (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_ARTIFACT)
			{
				if (!item.isEquipped() && (player.getInventory().getArtifactSlots() == 0))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
					sm.addItemName(item);
					player.sendPacket(sm);
					return;
				}
			}
			if (player.isCastingNow())
			{
				// Create and Bind the next action to the AI
				player.getAI().setNextAction(new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, () -> player.useEquippableItem(item, true)));
			}
			else if (player.isAttackingNow())
			{
				// Equip or unEquip.
				ThreadPool.schedule(() -> player.useEquippableItem(item, false), player.getAttackEndTime() - TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()));
			}
			else
			{
				player.useEquippableItem(item, true);
			}
		}
		else
		{
			final EtcItem etcItem = item.getEtcItem();
			final IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
			if (handler == null)
			{
				if ((etcItem != null) && (etcItem.getHandlerName() != null))
				{
					PacketLogger.warning("Unmanaged Item handler: " + etcItem.getHandlerName() + " for Item Id: " + _itemId + "!");
				}
			}
			else if (handler.useItem(player, item, _ctrlPressed))
			{
				// Item reuse time should be added if the item is successfully used.
				// Skill reuse delay is done at handlers.itemhandlers.ItemSkillsTemplate;
				if (reuseDelay > 0)
				{
					player.addTimeStampItem(item, reuseDelay);
					sendSharedGroupUpdate(player, sharedReuseGroup, reuseDelay, reuseDelay);
				}
			}
			// TODO: New item handler for minerals?
			if (VariationData.getInstance().getVariation(_itemId) != null)
			{
				player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
			}
		}
	}
	
	private void reuseData(Player player, Item item, long remainingTime)
	{
		final int hours = (int) (remainingTime / 3600000);
		final int minutes = (int) (remainingTime % 3600000) / 60000;
		final int seconds = (int) ((remainingTime / 1000) % 60);
		final SystemMessage sm;
		if (hours > 0)
		{
			sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_H_S3_MIN_S4_SEC);
			sm.addItemName(item);
			sm.addInt(hours);
			sm.addInt(minutes);
		}
		else if (minutes > 0)
		{
			sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_MIN_S3_SEC);
			sm.addItemName(item);
			sm.addInt(minutes);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_AGAIN_IN_S2_SEC);
			sm.addItemName(item);
		}
		sm.addInt(seconds);
		player.sendPacket(sm);
	}
	
	private void sendSharedGroupUpdate(Player player, int group, long remaining, int reuse)
	{
		if (group > 0)
		{
			player.sendPacket(new ExUseSharedGroupItem(_itemId, group, remaining, reuse));
		}
	}
}
