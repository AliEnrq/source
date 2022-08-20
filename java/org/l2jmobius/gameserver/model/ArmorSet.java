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
package org.l2jmobius.gameserver.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ArmorsetSkillHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.model.stats.BaseStat;

/**
 * @author UnAfraid
 */
public class ArmorSet
{
	private final int _id;
	private final int _minimumPieces;
	private final boolean _isVisual;
	
	private final int[] _requiredItems;
	private final int[] _optionalItems;
	
	private final List<ArmorsetSkillHolder> _skills;
	private final Map<BaseStat, Double> _stats;
	
	private static final int[] ARMORSET_SLOTS = new int[]
	{
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_FEET
	};
	private static final int[] ARTIFACT_1_SLOTS = new int[]
	{
		Inventory.PAPERDOLL_ARTIFACT1,
		Inventory.PAPERDOLL_ARTIFACT2,
		Inventory.PAPERDOLL_ARTIFACT3,
		Inventory.PAPERDOLL_ARTIFACT4,
		Inventory.PAPERDOLL_ARTIFACT13,
		Inventory.PAPERDOLL_ARTIFACT16,
		Inventory.PAPERDOLL_ARTIFACT19,
	
	};
	private static final int[] ARTIFACT_2_SLOTS = new int[]
	{
		Inventory.PAPERDOLL_ARTIFACT5,
		Inventory.PAPERDOLL_ARTIFACT6,
		Inventory.PAPERDOLL_ARTIFACT7,
		Inventory.PAPERDOLL_ARTIFACT8,
		Inventory.PAPERDOLL_ARTIFACT14,
		Inventory.PAPERDOLL_ARTIFACT17,
		Inventory.PAPERDOLL_ARTIFACT20,
	
	};
	private static final int[] ARTIFACT_3_SLOTS = new int[]
	{
		Inventory.PAPERDOLL_ARTIFACT9,
		Inventory.PAPERDOLL_ARTIFACT10,
		Inventory.PAPERDOLL_ARTIFACT11,
		Inventory.PAPERDOLL_ARTIFACT12,
		Inventory.PAPERDOLL_ARTIFACT15,
		Inventory.PAPERDOLL_ARTIFACT18,
		Inventory.PAPERDOLL_ARTIFACT21,
	
	};
	
	public ArmorSet(int id, int minimumPieces, boolean isVisual, Set<Integer> requiredItems, Set<Integer> optionalItems, List<ArmorsetSkillHolder> skills, Map<BaseStat, Double> stats)
	{
		_id = id;
		_minimumPieces = minimumPieces;
		_isVisual = isVisual;
		_requiredItems = requiredItems.stream().mapToInt(x -> x).toArray();
		_optionalItems = optionalItems.stream().mapToInt(x -> x).toArray();
		_skills = skills;
		_stats = stats;
	}
	
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the minimum amount of pieces equipped to form a set
	 */
	public int getMinimumPieces()
	{
		return _minimumPieces;
	}
	
	/**
	 * @return {@code true} if the set is visual only, {@code} otherwise
	 */
	public boolean isVisual()
	{
		return _isVisual;
	}
	
	/**
	 * @return the set of items that can form a set
	 */
	public int[] getRequiredItems()
	{
		return _requiredItems;
	}
	
	/**
	 * @return the set of shields
	 */
	public int[] getOptionalItems()
	{
		return _optionalItems;
	}
	
	/**
	 * The list of skills that are activated when set reaches it's minimal equipped items condition
	 * @return
	 */
	public List<ArmorsetSkillHolder> getSkills()
	{
		return _skills;
	}
	
	/**
	 * @param stat
	 * @return the stats bonus value or 0 if doesn't exists
	 */
	public double getStatsBonus(BaseStat stat)
	{
		return _stats.getOrDefault(stat, 0d);
	}
	
	/**
	 * @param shieldId
	 * @return {@code true} if player has the shield of this set equipped, {@code false} in case set doesn't have a shield or player doesn't
	 */
	public boolean containOptionalItem(int shieldId)
	{
		return CommonUtil.contains(_optionalItems, shieldId);
	}
	
	/**
	 * @param player
	 * @return true if all parts of set are enchanted to +6 or more
	 */
	public int getLowestSetEnchant(Player player)
	{
		// Player don't have full set
		if (getPiecesCountById(player) < _minimumPieces)
		{
			return 0;
		}
		
		final PlayerInventory inv = player.getInventory();
		int enchantLevel = Byte.MAX_VALUE;
		for (int armorSlot : ARMORSET_SLOTS)
		{
			final Item itemPart = inv.getPaperdollItem(armorSlot);
			if ((itemPart != null) && CommonUtil.contains(_requiredItems, itemPart.getId()) && (enchantLevel > itemPart.getEnchantLevel()))
			{
				enchantLevel = itemPart.getEnchantLevel();
			}
		}
		if (enchantLevel == Byte.MAX_VALUE)
		{
			enchantLevel = 0;
		}
		return enchantLevel;
	}
	
	/**
	 * Condition for 3 Lv. Set Effect Applied Skill
	 * @param player
	 * @param bookSlot
	 * @return total paperdoll(busy) count for 1 of 3 artifact book slots
	 */
	public int getArtifactSlotMask(Player player, int bookSlot)
	{
		final PlayerInventory inv = player.getInventory();
		int slotMask = 0;
		switch (bookSlot)
		{
			case 1:
			{
				for (int artifactSlot : ARTIFACT_1_SLOTS)
				{
					final Item itemPart = inv.getPaperdollItem(artifactSlot);
					if ((itemPart != null) && CommonUtil.contains(_requiredItems, itemPart.getId()))
					{
						slotMask += artifactSlot;
					}
				}
				break;
			}
			case 2:
			{
				for (int artifactSlot : ARTIFACT_2_SLOTS)
				{
					final Item itemPart = inv.getPaperdollItem(artifactSlot);
					if ((itemPart != null) && CommonUtil.contains(_requiredItems, itemPart.getId()))
					{
						slotMask += artifactSlot;
					}
				}
				break;
			}
			case 3:
			{
				for (int artifactSlot : ARTIFACT_3_SLOTS)
				{
					final Item itemPart = inv.getPaperdollItem(artifactSlot);
					if ((itemPart != null) && CommonUtil.contains(_requiredItems, itemPart.getId()))
					{
						slotMask += artifactSlot;
					}
				}
				break;
			}
		}
		return slotMask;
	}
	
	public boolean hasOptionalEquipped(Player player, Function<Item, Integer> idProvider)
	{
		return player.getInventory().getPaperdollItems().stream().anyMatch(item -> CommonUtil.contains(_optionalItems, idProvider.apply(item)));
	}
	
	/**
	 * @param player
	 * @param idProvider
	 * @return the amount of set visual items that player has equipped
	 */
	public long getPiecesCount(Player player, Function<Item, Integer> idProvider)
	{
		return player.getInventory().getPaperdollItemCount(item -> CommonUtil.contains(_requiredItems, idProvider.apply(item)));
	}
	
	public long getPiecesCountById(Player player)
	{
		return player.getInventory().getPaperdollItemCount(item -> CommonUtil.contains(_requiredItems, item.getId()));
	}
}
