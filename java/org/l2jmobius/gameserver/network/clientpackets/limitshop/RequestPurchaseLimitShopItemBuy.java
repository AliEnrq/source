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
package org.l2jmobius.gameserver.network.clientpackets.limitshop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.LimitShopClanData;
import org.l2jmobius.gameserver.data.xml.LimitShopCraftData;
import org.l2jmobius.gameserver.data.xml.LimitShopData;
import org.l2jmobius.gameserver.enums.ExBrProductReplyType;
import org.l2jmobius.gameserver.enums.SpecialItemType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.request.PrimeShopRequest;
import org.l2jmobius.gameserver.model.holders.LimitShopProductHolder;
import org.l2jmobius.gameserver.model.holders.LimitShopRandomCraftReward;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.limitshop.ExPurchaseLimitShopItemResult;
import org.l2jmobius.gameserver.network.serverpackets.primeshop.ExBRBuyProduct;

/**
 * @author Mobius
 */
public class RequestPurchaseLimitShopItemBuy implements IClientIncomingPacket
{
	private int _productId;
	private int _amount;
	private LimitShopProductHolder _product;
	private int _shopIndex;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_shopIndex = packet.readC(); // 3 Lcoin Store, 4 Special Craft, 100 Clan Shop
		_productId = packet.readD();
		_amount = packet.readD();
		
		switch (_shopIndex)
		{
			case 3: // Normal Lcoin Shop
			{
				_product = LimitShopData.getInstance().getProduct(_productId);
				break;
			}
			case 4: // Lcoin Special Craft
			{
				_product = LimitShopCraftData.getInstance().getProduct(_productId);
				break;
			}
			case 100: // Clan Shop
			{
				_product = LimitShopClanData.getInstance().getProduct(_productId);
				break;
			}
			default:
			{
				_product = null;
			}
		}
		
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
		
		if (_amount < 1)
		{
			return;
		}
		
		if (_product == null)
		{
			return;
		}
		
		if (!player.isInventoryUnder80(false))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVENTORY_OVERFLOW));
			return;
		}
		
		if ((player.getLevel() < _product.getMinLevel()) || (player.getLevel() > _product.getMaxLevel()))
		{
			player.sendPacket(SystemMessageId.YOUR_LEVEL_CANNOT_PURCHASE_THIS_ITEM);
			return;
		}
		
		if (player.hasItemRequest() || player.hasRequest(PrimeShopRequest.class))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER_STATE));
			return;
		}
		
		// Add request.
		player.addRequest(new PrimeShopRequest(player));
		
		// Check limits.
		if (_product.getAccountDailyLimit() > 0) // Sale period.
		{
			if (player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_DAILY_COUNT + _product.getProductionId(), 0) >= (_product.getAccountDailyLimit() * _amount))
			{
				player.sendMessage("You have reached your daily limit."); // TODO: Retail system message?
				player.removeRequest(PrimeShopRequest.class);
				return;
			}
		}
		else if (_product.getAccountBuyLimit() > 0) // Count limit.
		{
			if (player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + _product.getProductionId(), 0) >= (_product.getAccountBuyLimit() * _amount))
			{
				player.sendMessage("You cannot buy any more of this item."); // TODO: Retail system message?
				player.removeRequest(PrimeShopRequest.class);
				return;
			}
		}
		
		// Check existing items.
		for (int i = 0; i < _product.getIngredientIds().length; i++)
		{
			if (_product.getIngredientIds()[i] == 0)
			{
				continue;
			}
			if (_product.getIngredientIds()[i] == Inventory.ADENA_ID)
			{
				if (player.getAdena() < (_product.getIngredientQuantities()[i] * _amount))
				{
					player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
					player.removeRequest(PrimeShopRequest.class);
					return;
				}
			}
			else if (_product.getIngredientIds()[i] == SpecialItemType.HONOR_COINS.getClientId())
			{
				if (player.getHonorCoins() < (_product.getIngredientQuantities()[i] * _amount))
				{
					player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
					player.removeRequest(PrimeShopRequest.class);
					return;
				}
			}
			else if (player.getInventory().getInventoryItemCount(_product.getIngredientIds()[i], _product.getIngredientEnchants()[i] == 0 ? -1 : _product.getIngredientEnchants()[i], true) < (_product.getIngredientQuantities()[i] * _amount))
			{
				player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
				player.removeRequest(PrimeShopRequest.class);
				return;
			}
		}
		
		// Remove items.
		for (int i = 0; i < _product.getIngredientIds().length; i++)
		{
			if (_product.getIngredientIds()[i] == 0)
			{
				continue;
			}
			if (_product.getIngredientIds()[i] == Inventory.ADENA_ID)
			{
				player.reduceAdena("LCoinShop", _product.getIngredientQuantities()[i] * _amount, player, true);
			}
			else if (_product.getIngredientIds()[i] == SpecialItemType.HONOR_COINS.getClientId())
			{
				player.setHonorCoins(player.getHonorCoins() - (_product.getIngredientQuantities()[i] * _amount));
			}
			else
			{
				if (_product.getIngredientEnchants()[i] > 0)
				{
					int count = 0;
					final Collection<Item> items = player.getInventory().getAllItemsByItemId(_product.getIngredientIds()[i], _product.getIngredientEnchants()[i]);
					for (Item item : items)
					{
						if (count == _amount)
						{
							break;
						}
						count++;
						player.destroyItem("LCoinShop", item, player, true);
					}
				}
				else
				{
					player.destroyItemByItemId("LCoinShop", _product.getIngredientIds()[i], _product.getIngredientQuantities()[i] * _amount, player, true);
				}
			}
			if (Config.VIP_SYSTEM_L_SHOP_AFFECT)
			{
				player.updateVipPoints(_amount);
			}
		}
		
		// Reward.
		final List<LimitShopRandomCraftReward> rewards = new ArrayList<>();
		if (_product.getProductionId2() > 0)
		{
			for (int i = 0; i < _amount; i++)
			{
				if (Rnd.get(100) < _product.getChance())
				{
					rewards.add(new LimitShopRandomCraftReward(_product.getProductionId(), (int) _product.getCount(), 0));
					player.addItem("LCoinShop", _product.getProductionId(), _product.getCount(), player, true);
				}
				else if ((Rnd.get(100) < _product.getChance2()) || (_product.getProductionId3() == 0))
				{
					rewards.add(new LimitShopRandomCraftReward(_product.getProductionId2(), (int) _product.getCount2(), 1));
					player.addItem("LCoinShop", _product.getProductionId2(), _product.getCount2(), player, true);
				}
				else if ((Rnd.get(100) < _product.getChance3()) || (_product.getProductionId4() == 0))
				{
					rewards.add(new LimitShopRandomCraftReward(_product.getProductionId3(), (int) _product.getCount3(), 2));
					player.addItem("LCoinShop", _product.getProductionId3(), _product.getCount3(), player, true);
				}
				else if ((Rnd.get(100) < _product.getChance4()) || (_product.getProductionId5() == 0))
				{
					rewards.add(new LimitShopRandomCraftReward(_product.getProductionId4(), (int) _product.getCount4(), 3));
					player.addItem("LCoinShop", _product.getProductionId4(), _product.getCount4(), player, true);
				}
				else if (_product.getProductionId5() > 0)
				{
					rewards.add(new LimitShopRandomCraftReward(_product.getProductionId5(), (int) _product.getCount5(), 4));
					player.addItem("LCoinShop", _product.getProductionId5(), _product.getCount5(), player, true);
				}
			}
		}
		else if (Rnd.get(100) < _product.getChance())
		{
			final int amount = (int) (_product.getCount() * _amount);
			rewards.add(new LimitShopRandomCraftReward(_product.getProductionId(), amount, 0));
			player.addItem("LCoinShop", _product.getProductionId(), amount, player, true);
		}
		
		// Update account variables.
		if (_product.getAccountDailyLimit() > 0)
		{
			player.getAccountVariables().set(AccountVariables.LCOIN_SHOP_PRODUCT_DAILY_COUNT + _product.getProductionId(), player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_DAILY_COUNT + _product.getProductionId(), 0) + _amount);
		}
		else if (_product.getAccountBuyLimit() > 0)
		{
			player.getAccountVariables().set(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + _product.getProductionId(), player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + _product.getProductionId(), 0) + _amount);
		}
		
		player.sendPacket(new ExPurchaseLimitShopItemResult(true, _shopIndex, _productId, rewards));
		player.sendItemList();
		
		// Remove request.
		player.removeRequest(PrimeShopRequest.class);
	}
}
