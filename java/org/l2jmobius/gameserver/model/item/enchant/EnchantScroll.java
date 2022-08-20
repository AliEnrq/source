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
package org.l2jmobius.gameserver.model.item.enchant;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.EnchantItemData;
import org.l2jmobius.gameserver.data.xml.EnchantItemGroupsData;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.CrystalType;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.item.type.ItemType;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author UnAfraid
 */
public class EnchantScroll extends AbstractEnchantItem
{
	private final boolean _isWeapon;
	private final boolean _isBlessed;
	private final boolean _isBlessedDown;
	private final boolean _isSafe;
	private final boolean _isGiant;
	private final boolean _isCursed;
	private final int _scrollGroupId;
	private Set<Integer> _items;
	
	public EnchantScroll(StatSet set)
	{
		super(set);
		_scrollGroupId = set.getInt("scrollGroupId", 0);
		
		final ItemType type = getItem().getItemType();
		_isWeapon = (type == EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_WP) || (type == EtcItemType.BLESS_ENCHT_WP) || (type == EtcItemType.ENCHT_WP) || ((type == EtcItemType.GIANT_ENCHT_WP) || (type == EtcItemType.CURSED_ENCHT_WP));
		_isBlessed = (type == EtcItemType.BLESS_ENCHT_AM) || (type == EtcItemType.BLESS_ENCHT_WP) || (type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_WP) || (type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_AM) || (type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM) || (type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP);
		_isBlessedDown = (type == EtcItemType.BLESS_ENCHT_AM_DOWN);
		_isSafe = (type == EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_AM) || (type == EtcItemType.ENCHT_ATTR_ANCIENT_CRYSTAL_ENCHANT_WP) || (type == EtcItemType.ENCHT_ATTR_CRYSTAL_ENCHANT_AM) || (type == EtcItemType.ENCHT_ATTR_CRYSTAL_ENCHANT_WP);
		_isGiant = (type == EtcItemType.GIANT_ENCHT_AM) || (type == EtcItemType.GIANT_ENCHT_WP);
		_isCursed = (type == EtcItemType.CURSED_ENCHT_AM) || (type == EtcItemType.CURSED_ENCHT_WP);
	}
	
	@Override
	public boolean isWeapon()
	{
		return _isWeapon;
	}
	
	/**
	 * @return {@code true} for blessed scrolls (enchanted item will remain on failure and enchant value will reset to 0), {@code false} otherwise
	 */
	public boolean isBlessed()
	{
		return _isBlessed;
	}
	
	/**
	 * @return {@code true} for blessed scrolls (enchanted item will remain on failure and enchant value will go down by 1), {@code false} otherwise
	 */
	public boolean isBlessedDown()
	{
		return _isBlessedDown;
	}
	
	/**
	 * @return {@code true} for safe-enchant scrolls (enchant level will remain on failure), {@code false} otherwise
	 */
	public boolean isSafe()
	{
		return _isSafe;
	}
	
	public boolean isGiant()
	{
		return _isGiant;
	}
	
	public boolean isCursed()
	{
		return _isCursed;
	}
	
	/**
	 * @return id of scroll group that should be used
	 */
	public int getScrollGroupId()
	{
		return _scrollGroupId;
	}
	
	/**
	 * Enforces current scroll to use only those items as possible items to enchant
	 * @param itemId
	 */
	public void addItem(int itemId)
	{
		if (_items == null)
		{
			_items = new HashSet<>();
		}
		_items.add(itemId);
	}
	
	public Set<Integer> getItems()
	{
		return _items;
	}
	
	/**
	 * @param itemToEnchant the item to be enchanted
	 * @param supportItem the support item used when enchanting (can be null)
	 * @return {@code true} if this scroll can be used with the specified support item and the item to be enchanted, {@code false} otherwise
	 */
	@Override
	public boolean isValid(Item itemToEnchant, EnchantSupportItem supportItem)
	{
		if ((_items != null) && !_items.contains(itemToEnchant.getId()))
		{
			return false;
		}
		else if ((supportItem != null))
		{
			if ((isBlessed() && !supportItem.isBlessed()) || (!isBlessed() && supportItem.isBlessed()))
			{
				return false;
			}
			else if ((isBlessedDown() && !supportItem.isBlessed()) || (!isBlessedDown() && supportItem.isBlessed()))
			{
				return false;
			}
			else if ((isGiant() && !supportItem.isGiant()) || (!isGiant() && supportItem.isGiant()))
			{
				return false;
			}
			else if (!supportItem.isValid(itemToEnchant, supportItem))
			{
				return false;
			}
			else if (supportItem.isWeapon() != isWeapon())
			{
				return false;
			}
		}
		if (_items == null)
		{
			if (isActionBlessed() && itemToEnchant.isWeapon() && (itemToEnchant.getTemplate().getCrystalType() == getGrade()))
			{
				return true;
			}
			for (EnchantScroll scroll : EnchantItemData.getInstance().getScrolls())
			{
				if (scroll.getId() == getId())
				{
					continue;
				}
				final Set<Integer> scrollItems = scroll.getItems();
				if ((scrollItems != null) && scrollItems.contains(itemToEnchant.getId()))
				{
					return false;
				}
			}
		}
		return super.isValid(itemToEnchant, supportItem);
	}
	
	/**
	 * @param player
	 * @param enchantItem
	 * @return the chance of current scroll's group.
	 */
	public double getChance(Player player, Item enchantItem)
	{
		if (EnchantItemGroupsData.getInstance().getScrollGroup(_scrollGroupId) == null)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Unexistent enchant scroll group specified for enchant scroll: " + getId());
			return -1;
		}
		
		final EnchantItemGroup group = EnchantItemGroupsData.getInstance().getItemGroup(enchantItem.getTemplate(), _scrollGroupId);
		if (group == null)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Couldn't find enchant item group for scroll: " + getId() + " requested by: " + player);
			return -1;
		}
		
		if ((getSafeEnchant() > 0) && (enchantItem.getEnchantLevel() < getSafeEnchant()))
		{
			return 100;
		}
		
		return group.getChance(enchantItem.getEnchantLevel());
	}
	
	/**
	 * @param player
	 * @param enchantItem
	 * @param supportItem
	 * @return the total chance for success rate of this scroll
	 */
	public EnchantResultType calculateSuccess(Player player, Item enchantItem, EnchantSupportItem supportItem)
	{
		if (!isValid(enchantItem, supportItem))
		{
			return EnchantResultType.ERROR;
		}
		
		final double chance = getChance(player, enchantItem);
		if (chance == -1)
		{
			return EnchantResultType.ERROR;
		}
		
		final int crystalLevel = enchantItem.getTemplate().getCrystalType().getLevel();
		final double enchantRateStat = (crystalLevel > CrystalType.NONE.getLevel()) && (crystalLevel < CrystalType.EVENT.getLevel()) ? player.getStat().getValue(Stat.ENCHANT_RATE) : 0;
		final double bonusRate = getBonusRate();
		final double supportBonusRate = (supportItem != null) ? supportItem.getBonusRate() : 0;
		final double finalChance = Math.min(chance + bonusRate + supportBonusRate + enchantRateStat, 100);
		final double random = 100 * Rnd.nextDouble();
		final boolean success = (random < finalChance);
		return success ? EnchantResultType.SUCCESS : EnchantResultType.FAILURE;
	}
}
