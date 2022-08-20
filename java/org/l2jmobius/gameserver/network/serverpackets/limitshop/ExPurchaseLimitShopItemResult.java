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
package org.l2jmobius.gameserver.network.serverpackets.limitshop;

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.LimitShopClanData;
import org.l2jmobius.gameserver.data.xml.LimitShopCraftData;
import org.l2jmobius.gameserver.data.xml.LimitShopData;
import org.l2jmobius.gameserver.model.holders.LimitShopProductHolder;
import org.l2jmobius.gameserver.model.holders.LimitShopRandomCraftReward;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Gustavo Fonseca
 */
public class ExPurchaseLimitShopItemResult implements IClientOutgoingPacket
{
	private final int _category, _productId;
	private final boolean _isSuccess;
	private final List<LimitShopRandomCraftReward> _rewards;
	private final LimitShopProductHolder _product;
	
	public ExPurchaseLimitShopItemResult(boolean isSuccess, int category, int productId, List<LimitShopRandomCraftReward> rewards)
	{
		_isSuccess = isSuccess;
		_category = category;
		_productId = productId;
		_rewards = rewards;
		switch (_category)
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
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PURCHASE_LIMIT_SHOP_ITEM_BUY.writeId(packet);
		if ((_product == null) || !_isSuccess)
		{
			packet.writeC(1);
			packet.writeC(_category);
			packet.writeD(_productId);
			packet.writeD(1);
			packet.writeC(1);
			packet.writeD(0);
			packet.writeQ(0);
		}
		else
		{
			packet.writeC(0); // success
			packet.writeC(_category);
			packet.writeD(_productId);
			packet.writeD(_rewards.size());
			int counter = 0;
			for (LimitShopRandomCraftReward entry : _rewards)
			{
				if (counter == _rewards.size())
				{
					break;
				}
				packet.writeC(entry.getRewardIndex());
				packet.writeD(0);
				packet.writeD(entry.getCount());
				counter++;
			}
		}
		return true;
	}
}