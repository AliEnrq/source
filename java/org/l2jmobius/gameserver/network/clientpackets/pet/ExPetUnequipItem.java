package org.l2jmobius.gameserver.network.clientpackets.pet;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.ai.NextAction;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.pet.PetInfo;

/**
 * Written by Berezkin Nikolay, on 25.04.2021
 */
public class ExPetUnequipItem implements IClientIncomingPacket
{
	private int _objectId;
	private int _itemId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
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
		final Pet pet = player.getPet();
		if (pet == null)
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
		
		final Item item = pet.getInventory().getItemByObjectId(_objectId);
		
		// No UseItem is allowed while the player is in special conditions
		if (player.hasBlockActions() || player.isControlBlocked() || player.isAlikeDead())
		{
			return;
		}
		
		// Char cannot use item when dead
		if (player.isDead() || pet.isDead() || !player.getInventory().canManipulateWithItemId(item.getId()))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addItemName(item);
			player.sendPacket(sm);
			return;
		}
		
		if (!item.isEquipable())
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
		
		player.onActionRequest();
		
		if (item.isEquipable())
		{
			if (pet.getInventory().isItemSlotBlocked(item.getTemplate().getBodyPart()))
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
			}
			else if (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_DECO)
			{
				if (!item.isEquipped() && (player.getInventory().getTalismanSlots() == 0))
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
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
					player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
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
				player.getAI().setNextAction(new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, () ->
				{
					pet.transferItem("UnequipFromPet", item.getObjectId(), 1, player.getInventory(), player, null);
				}));
			}
			else if (player.isAttackingNow())
			{
				// Equip or unEquip.
				pet.transferItem("UnequipFromPet", item.getObjectId(), 1, player.getInventory(), player, null);
			}
			else
			{
				pet.transferItem("UnequipFromPet", item.getObjectId(), 1, player.getInventory(), player, null);
			}
			pet.getStat().recalculateStats(true);
			pet.getOwner().sendPacket(new PetInfo(pet, 1));
		}
	}
}
